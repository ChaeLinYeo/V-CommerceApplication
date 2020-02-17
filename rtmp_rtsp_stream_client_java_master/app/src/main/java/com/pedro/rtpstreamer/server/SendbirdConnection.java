package com.pedro.rtpstreamer.server;

import android.content.Context;
import android.util.Log;

import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.utils.PopupManager;
import com.pedro.rtpstreamer.utils.StaticVariable;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.PreviousMessageListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;
import com.sendbird.android.UserMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.pedro.rtpstreamer.utils.StaticVariable.CHANNEL_HANDLER_ID;

public class SendbirdConnection {

    private static final SendbirdConnection ourInstance = new SendbirdConnection();

    public static SendbirdConnection getInstance() {
        return ourInstance;
    }

    private SendbirdConnection(){}

    /////////////////////////////////////////////////////////////////////////////
    private static SendbirdListner sendbirdListner;
    /////////////////////////////////////////////////////////////////////////////
    private static List<User> operator = new ArrayList<>();
    private static OpenChannel ctrl_channel;
    private static OpenChannel mOpenChannel;

    private static String CHANNEL_URL;
    private static int broadcastChannelNum;

    private static List<User> UserList = new ArrayList<>();

    private static int viewNum=0;

    private static String[] channelUrl = new String[10];
    private static ArrayList<Integer> liveChannelList = new ArrayList<>();

    private static String USER_ID="";
    private static boolean isOperator;

    public static void setupSendbird(Context context, boolean isOperator1, SendbirdListner sendbirdListner1) {

        setUserId();

        sendbirdListner = sendbirdListner1;
        isOperator = isOperator1;

        SendBird.init(context.getString(R.string.sendbird_app_id), context);
        SendBird.connect(USER_ID,
            (User user, SendBirdException e) -> {
                if (e != null) {    // Error.
                    Log.d("connect error","connect : 1" );
                    return;
                }
                if(isOperator) operator.add(user);
                updateCurrentUserInfo(USER_ID);
            }
        );

        viewNum=0;
    }

    public static void setSendbirdListner(SendbirdListner sendbirdListner1){
        sendbirdListner = sendbirdListner1;
    }

    private static void updateCurrentUserInfo(String user_id){
        SendBird.updateCurrentUserInfo(user_id, null,
                (SendBirdException ex) -> {
                    if (ex != null) Log.e("nickname",ex.getMessage()+" : "+ex.getCode());
                }
        );
    }

    private static void setUserId(){
        USER_ID="";
        Random r = new Random();
        for(int i = 0; i < 3 ; i++ ) {
            int f = r.nextInt(26);
            USER_ID+= Character.toString((char) (f + 65));
        }

        int num = r.nextInt(10000) + r.nextInt(10000);

        USER_ID += num;
    }

    public static String getUserId(){ return USER_ID; }

    public static void getCtrl(){
        OpenChannel.getChannel(StaticVariable.sendbirdCtrlChannel,
            (OpenChannel openChannel, SendBirdException e) -> {
                if (e != null) {    // Error.
                    Log.d("getCtrl", ""+e.getMessage());
                    return;
                }
                ctrl_channel = openChannel;
                sendbirdListner.getCtrlComplete();
            }
        );
    }

    public static void getBroadcastChannel(){
        ctrl_channel.getAllMetaData((Map<String, String> map, SendBirdException ex) -> {
            for(int i = 0; i < StaticVariable.numChannel; i++){
                if(map.get(Integer.toString(i)).equals("true")){
                    sendbirdListner.getChannelComplete(true);
                    broadcastChannelNum = i;
                    return;
                }
            }
            sendbirdListner.getChannelComplete(false);
        });
    }

    public static void getPlayChannel(int channelNum){
        String mChannelUrl = channelUrl[channelNum];
        OpenChannel.getChannel(mChannelUrl, new OpenChannel.OpenChannelGetHandler() {
            @Override
            public void onResult(final OpenChannel openChannel, SendBirdException e) {
                if (e != null) {    // Error.
                    Log.d("getchannel",""+e.getMessage());
                    return;
                }
                openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
                    @Override
                    public void onResult(SendBirdException e) {
                        if (e != null) {    // Error.
                            Log.d("getc","enter error");
                            e.printStackTrace();
                            return;
                        }
                        mOpenChannel = openChannel;
                        getUserList(true);
                        loadInitialMessageList(20);
                        sendbirdListner.getPlayChannelComplete(mOpenChannel.getCoverUrl(), mOpenChannel.getName(), mOpenChannel.getOperators().get(0).getNickname());
                    }
                });
            }
        });

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                if (baseChannel.getUrl().equals(mChannelUrl)) {
                    sendbirdListner.onMessageReceived(baseMessage.getCustomType(),baseMessage.getData());
                }
            }

            @Override
            public void onMetaCountersUpdated(BaseChannel channel, Map<String, Integer> metaCounterMap) {
                super.onMetaCountersUpdated(channel, metaCounterMap);
                sendbirdListner.metaCounterUpdated(metaCounterMap.get("heart"));
            }

            @Override
            public void onMetaDataCreated(BaseChannel channel, Map<String, String> metaDataMap) {
                super.onMetaDataCreated(channel, metaDataMap);
                setCategory(metaDataMap);
            }

            @Override
            public void onMetaDataUpdated(BaseChannel channel, Map<String, String> metaDataMap) {
                super.onMetaDataUpdated(channel, metaDataMap);
                setCategory(metaDataMap);
            }

            @Override
            public void onMetaDataDeleted(BaseChannel channel, List<String> keys) {
                super.onMetaDataDeleted(channel, keys);
                PopupManager.setCategory(keys);
            }

            @Override
            public void onChannelChanged(BaseChannel channel) {
                super.onChannelChanged(channel);
                sendbirdListner.onTitleChanged(channel.getName());
            }
        });
    }

    public static void increaseMetaCounters(){
        HashMap<String, Integer> counters = new HashMap<String, Integer>();
        counters.put("heart", 1);
        mOpenChannel.increaseMetaCounters(counters, new BaseChannel.MetaCounterHandler() {
            @Override
            public void onResult(Map<String, Integer> map, SendBirdException e) {
                if (e != null) {    // Error.
                    return;
                }
                sendbirdListner.metaCounterUpdated(map.get("heart"));
            }
        });
    }

    public static void getLiveChannelUrlList(){
        liveChannelList.clear();
        ctrl_channel.getAllMetaData((Map<String, String> map, SendBirdException ex) -> {
            for(String key : map.keySet()) {
                if (!map.get(key).equals("true")) {
                    channelUrl[Integer.parseInt(key)] = map.get(key);
                    liveChannelList.add(Integer.parseInt(key));
                }
                else channelUrl[Integer.parseInt(key)] = null;
            }
            sendbirdListner.getChannelComplete(true);
        });
    }

    public static boolean isLive(int channelNum){
        return channelUrl[channelNum] != null;
    }

    public static int getLiveChannelNum(int i){
        return liveChannelList.get(i);
    }

    public static String getPlayChannelUrl(int i){
        return channelUrl[i];
    }

    public static void createChannel(String title){
        OpenChannel.createChannel(title, null, null, null, operator,
                (OpenChannel openChannel, SendBirdException e) -> {
                    if (e != null) {
                        Log.e("createChannel",""+e.getMessage());
                        return;
                    }
                    CHANNEL_URL = openChannel.getUrl();
                    /////////////////////////////////////////////////////////////////////
                    //ctrl channel의 메타 데이터 업데이트
                    updateMetaData(ctrl_channel, Integer.toString(broadcastChannelNum), CHANNEL_URL);
                    //////////////////////////////////////////
                    mOpenChannel = openChannel;
                    HashMap<String, Integer> map = new HashMap<>();
                    map.put("heart", 0);
                    mOpenChannel.createMetaCounters(map,
                        (Map<String, Integer> hMap, SendBirdException ex) -> {
                            if(ex != null) Log.e("createChannel", ""+ex.getMessage());
                        }
                    );
                    getChannel(CHANNEL_URL);
                    getUserList(true);
                    sendbirdListner.channelCreateComplete();
                }
        );

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                sendbirdListner.messageReceived(baseMessage.getCustomType(), baseMessage.getData(), baseMessage.getCreatedAt());
            }

            @Override
            public void onUserEntered(OpenChannel channel, User user) {
                super.onUserEntered(channel, user);
                viewNum++;
                getUserList(true);
            }

            @Override
            public void onUserExited(OpenChannel channel, User user) {
                super.onUserExited(channel, user);
                getUserList(true);
            }

            @Override
            public void onMetaCountersUpdated(BaseChannel channel, Map<String, Integer> metaCounterMap) {
                super.onMetaCountersUpdated(channel, metaCounterMap);
                sendbirdListner.metaCounterUpdated(metaCounterMap.get("heart"));
            }
        });
    }

    private static void getChannel(String channelUrl){
        OpenChannel.getChannel(channelUrl,
            (OpenChannel openChannel, SendBirdException e) -> {
                if (e != null) {    // Error.
                    Log.e("getChannel", ""+e.getMessage());
                    return;
                }
                openChannel.enter((SendBirdException ex) -> {
                    if (ex != null) Log.e("getChannel",""+ex.getMessage());
                });
            }
        );
    }

    public static int getBroadcastChannelNum(){
        return broadcastChannelNum;
    }

    //////////////////////////////////////////////////////////////////////

    public static void sendUserMessage(String text, String type) {
        if(mOpenChannel == null) {
            Log.e("sendUserMessage", "channel is null");
            return;
        }
        mOpenChannel.sendUserMessage(text, text, type,
            (UserMessage userMessage, SendBirdException e) -> {
                if (e != null) Log.e("sendUserMessage",""+e.getMessage());
            }
        );
    }

    private static void getUserListFromServer() {
        UserListQuery userListQuery = mOpenChannel.createParticipantListQuery();
        userListQuery.next((List<User> list, SendBirdException e) -> {
            if (e != null) return;
            if(isOperator) {UserList = setUserList(list);
                sendbirdListner.getUserListComplete(Integer.toString(UserList.size()));}
            else {
                UserList = list;
                sendbirdListner.getUserListComplete(Integer.toString(UserList.size()-1));
            }

        });
    }

    private static List<User> setUserList(List<User> userList) {
        List<User> sortedUserList = new ArrayList<>();
        for (User participant : userList) {
            if (!(participant.getUserId().equals(operator.get(0).getUserId()))) {
                sortedUserList.add(participant);
            }
        }
        return sortedUserList;
    }

    public static List<User> getUserList(boolean renewal) {{
        if(renewal) getUserListFromServer();
        return UserList;
    }}

    public static void banUser(int position){
        mOpenChannel.muteUserWithUserId(UserList.get(position).getUserId(), "ban by operater", 10,
                (SendBirdException e) -> {
                    if(e!=null) Log.e("banUser", ""+e.getMessage()+e.getCode());
                }
        );
    }

    ///////////////CATEGORY///////////////
    public static void addCategory(String item){
        updateMetaData(mOpenChannel, item, item);
    }

    public static void removeCategory(String item){
        mOpenChannel.deleteMetaData(item, (SendBirdException e) -> {});
    }

    public static void selectCategory(String item){
        updateMetaData(mOpenChannel, item, "select");
    }
    ///////////////////////////////////////

    public static void getAllMetaData(){
        mOpenChannel.getAllMetaData(new BaseChannel.MetaDataHandler() {
            @Override
            public void onResult(Map<String, String> map, SendBirdException e) {
                setCategory(map);
            }
        });
    }

    private static void setCategory(Map<String, String> map){
        PopupManager.clearCategoryI();
        PopupManager.clearSCategory();
        for(Map.Entry<String, String> entry : map.entrySet()){
            if(entry.getValue().equals("select")){
                PopupManager.addSCategory(entry.getKey());
            }else {
                PopupManager.addCategoryI(entry.getKey());
            }
        }
        PopupManager.setCC();
    }

    private static void loadInitialMessageList(int numMessages){
        PreviousMessageListQuery mPrevMessageListQuery = mOpenChannel.createPreviousMessageListQuery();
        mPrevMessageListQuery.load(numMessages, true,
           (List<BaseMessage> list, SendBirdException e) -> {
            if (e != null) {
                // Error!
                e.printStackTrace();
                return;
            }
            for(BaseMessage b : list){
                sendbirdListner.loadInitialMessage(b.getCustomType(), b.getData());
            }
        });
    }

    private static void updateMetaData(OpenChannel openChannel, String key, String value){
        HashMap<String, String> map = new HashMap<>();
        map.put(key, value);
        openChannel.updateMetaData(map, (Map<String, String> pMap, SendBirdException e) -> {
            if(e!= null) Log.e("updateMetaData", e.getMessage() + e.getCode());
        });
    }

    public static void updateTitle(String title){
        String coverUrl = mOpenChannel.getCoverUrl();
        mOpenChannel.updateChannel(title, coverUrl, "null",
                (OpenChannel openChannel, SendBirdException e) -> { }
        );
    }

    public static void broadcastfinish(){
        updateMetaData(ctrl_channel, Integer.toString(broadcastChannelNum), "true");
        mOpenChannel.delete((SendBirdException e) -> { });
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
    }

    private void disconnect() {
        SendBird.unregisterPushTokenAllForCurrentUser(
            (SendBirdException e) -> {
                if (e != null) {
                    // Error!
                    Log.d(" ","onunregister");
                    e.printStackTrace();
                    // Don't return because we still need to disconnect.
                }
                SendBird.disconnect(new SendBird.DisconnectHandler() {
                    @Override
                    public void onDisconnected() {
                        // A current user is disconnected from SendBird server.
                    }
                });
            }
        );
    }

    public static int getViewNum(){return viewNum;}
}
