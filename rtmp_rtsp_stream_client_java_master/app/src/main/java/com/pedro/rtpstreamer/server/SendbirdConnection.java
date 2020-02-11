package com.pedro.rtpstreamer.server;

import android.content.Context;
import android.util.Log;

import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.utils.PopupManager;
import com.pedro.rtpstreamer.utils.StaticVariable;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;
import com.sendbird.android.UserMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendbirdConnection {

    private static final SendbirdConnection ourInstance = new SendbirdConnection();

    public static SendbirdConnection getInstance() {
        return ourInstance;
    }

    private SendbirdConnection(){}

    /////////////////////////////////////////////////////////////////////////////
    private SendbirdListner.ForBroadcaster forBroadcaster;
    private SendbirdListner.ForPlayer forPlayer;
//    private SendbirdListner.
    /////////////////////////////////////////////////////////////////////////////
    private List<User> operator = new ArrayList<>();
    private OpenChannel ctrl_channel;
    private OpenChannel mOpenChannel;

    private String CHANNEL_URL;
    private int channelNum;

    private List<User> UserList = new ArrayList<>();

    public void setupSendbird(Context context, String USER_ID, int type) {
        //////////////////////////////////////////////
        //try catch
        if(type == 0) forBroadcaster = (SendbirdListner.ForBroadcaster) context;
        else if(type == 1) forPlayer = (SendbirdListner.ForPlayer) context;
        //////////////////////////////////////////////
        SendBird.init(context.getString(R.string.sendbird_app_id), context);
        SendBird.connect(USER_ID,
                (User user, SendBirdException e) -> {
                    if (e != null) {    // Error.
                        Log.d("connect error","connect : 1" );
                        return;
                    }
                    operator.add(user);
                    updateCurrentUserInfo(USER_ID);
                }
        );
    }

    private void updateCurrentUserInfo(final String userNickname) {
        SendBird.updateCurrentUserInfo(userNickname, null,
                (SendBirdException e) -> {
                    if (e != null) {
                        Log.e("nickname",e.getMessage()+" : "+e.getCode());
                    }
                }
        );
    }

    public void getCtrl(Context context){
        OpenChannel.getChannel(context.getString(R.string.sendbird_ctrlChannel),
            (OpenChannel openChannel, SendBirdException e) -> {
                if (e != null) {    // Error.
                    Log.d("getCtrl", ""+e.getMessage());
                    return;
                }
                ctrl_channel = openChannel;
                ////////////////////////////
                openChannel.getAllMetaData((Map<String, String> map, SendBirdException ex) -> {
                        for(int i = 0; i < 10; i++){
                            if(map.get(Integer.toString(i)).equals("true")){
                                forBroadcaster.channelFounded(true);
                                channelNum = i;
                                return;
                            }
                        }
                    forBroadcaster.channelFounded(false);
                    }
                );
            }
        );
    }

    public void getBroadcastChannel(){
        ctrl_channel.getAllMetaData(
            (Map<String, String> map, SendBirdException ex) -> {
                for(int i = 0; i < 10; i++){
                    if(map.get(Integer.toString(i)).equals("true")){
                        forBroadcaster.channelFounded(true);
                        channelNum = i;
                        return;
                    }
                }
                forBroadcaster.channelFounded(false);
            }
        );
    }

    public void getBroadcastChannelList(){

    }

    public void createChannel(String title){
        OpenChannel.createChannel(title, null, null, null, operator,
                (OpenChannel openChannel, SendBirdException e) -> {
                    if (e != null) {
                        Log.e("createChannel",""+e.getMessage());
                        return;
                    }
                    CHANNEL_URL = openChannel.getUrl();
                    /////////////////////////////////////////////////////////////////////
                    //ctrl channel의 메타 데이터 업데이트
                    HashMap<String,String> map1 = new HashMap<>();
                    map1.put(Integer.toString(channelNum),CHANNEL_URL);
                    ctrl_channel.updateMetaData(map1, (Map<String, String> map, SendBirdException ex) -> {
                            if(e!=null){
                                Log.e("createChannel",""+ex.getMessage());
                            }
                        }
                    );
                    //////////////////////////////////////////
                    mOpenChannel = openChannel;
                    HashMap<String, Integer> map = new HashMap<>();
                    map.put("heart", 0);
                    mOpenChannel.createMetaCounters(map,
                        (Map<String, Integer> hMap, SendBirdException ex) -> {
                            if( ex != null) {
                                Log.e("createChannel", ""+ex.getMessage());
                                return;
                            }
                        }
                    );
                    getChannel(CHANNEL_URL);
                    getUserList(true);
                    forBroadcaster.channelCreateComplete();
                }
        );
        SendBird.addChannelHandler(StaticVariable.CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                forBroadcaster.messageReceived(baseMessage.getCustomType(), baseMessage.getData(), baseMessage.getCreatedAt());
            }

            @Override
            public void onUserEntered(OpenChannel channel, User user) {
                super.onUserEntered(channel, user);
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
                forBroadcaster.metaCounterUpdated(metaCounterMap.get("heart"));
            }
        });
    }

    private void getChannel(String channelUrl){
        OpenChannel.getChannel(channelUrl,
            (OpenChannel openChannel, SendBirdException e) -> {
                if (e != null) {    // Error.
                    Log.e("getChannel", ""+e.getMessage());
                    return;
                }
                openChannel.enter((SendBirdException ex) -> {
                            if (ex != null) {    // Error.
                                Log.e("getChannel",""+ex.getMessage());
                            }
                        }
                );
            }
        );
    }

    public int getChannelNum(){
        return channelNum;
    }

    //////////////////////////////////////////////////////////////////////

    public void sendUserMessage(String text, String type) {
        if(mOpenChannel == null) {
            Log.e("sendUserMessage", "channel is null");
            return;
        }
        mOpenChannel.sendUserMessage(text, text, type,
                (UserMessage userMessage, SendBirdException e) -> {
                    if (e != null) {
                        // Error!
                       Log.e("sendUserMessage",""+e.getMessage());
                    }
                }
        );
    }

    private void getUserListFromServer() {
        UserListQuery userListQuery = mOpenChannel.createParticipantListQuery();
        userListQuery.next((List<User> list, SendBirdException e) -> {
                    if (e != null) return;
                    UserList = setUserList(list);
            forBroadcaster.getUserListComplete(Integer.toString(UserList.size()));
                }
        );
    }

    private List<User> setUserList(List<User> userList) {
        List<User> sortedUserList = new ArrayList<>();
        for (User participant : userList) {
            if (!(participant.getUserId().equals(operator.get(0).getUserId()))) {
                sortedUserList.add(participant);
            }
        }
        return sortedUserList;
    }

    public List<User> getUserList(boolean renewal) {{
        if(renewal) getUserListFromServer();
        return UserList;
    }}

    public void banUser(int position){
        mOpenChannel.muteUserWithUserId(UserList.get(position).getUserId(), "ban by operater", 10,
                (SendBirdException e) -> {
                    if(e!=null) Log.e("banUser", ""+e.getMessage()+e.getCode());
                }
        );
    }

    public void addCategory(String item){
        HashMap<String, String> map = new HashMap<>();
        map.put(item, item);
        updateCategory(map);
    }

    public void removeCategory(String item){
        HashMap<String, String> map = new HashMap<>();
        map.put(item, "empty");
        updateCategory(map);
    }

    public void updateCategory(HashMap<String, String> map){
        mOpenChannel.updateMetaData(map, (Map<String, String> pMap, SendBirdException e) -> {
            if(e!= null) Log.e("updateCategory", e.getMessage() + e.getCode());
        });
    }

    public void getAllCategory(PopupManager PM){
        PM.clearCategoryI();
        mOpenChannel.getAllMetaData(new BaseChannel.MetaDataHandler() {
            @Override
            public void onResult(Map<String, String> map, SendBirdException e) {
                for(Map.Entry<String, String> entry : map.entrySet()){
                    if(!entry.getKey().equals("empty")){
                        Log.d("allcate", entry.getKey());
                        PM.addCategoryI(entry.getKey());
                    }
                }
            }
        });
    }

    public void updateTitle(String title){
        String coverUrl = mOpenChannel.getCoverUrl();
        mOpenChannel.updateChannel(title, coverUrl, "null",
                (OpenChannel openChannel, SendBirdException e) -> { }
        );
    }

    public void broadcastfinish(){
        HashMap<String, String> map = new HashMap<>();
        map.put(Integer.toString(channelNum), "true");
        ctrl_channel.updateMetaData(map, (Map<String, String> pMap, SendBirdException e) -> {
                if(e!=null) Log.d("finishBro",""+e.getMessage());
            }
        );
        mOpenChannel.delete((SendBirdException e) -> { });
        SendBird.removeChannelHandler(StaticVariable.CHANNEL_HANDLER_ID);
    }

    //앱을 종료시 센드버드에서 로그아웃 시켜주는 메소드 아직은 사용 X
//    private void disconnect() {
//        SendBird.unregisterPushTokenAllForCurrentUser(
//            (SendBirdException e) -> {
//                if (e != null) {
//                    // Error!
//                    Log.d(" ","onunregister");
//                    e.printStackTrace();
//                    // Don't return because we still need to disconnect.
//                }
//                ConnectionManager.logout(
//                    () -> {
//                        try {
//                            PreferenceUtils.setConnected(false);
//                        }catch (Exception ex) {
//                            Log.d("logout", "");
//                            ex.printStackTrace();
//                        }
//                        Log.d("","connect : onDisconnected : " );
//                    }
//                );
//            }
//        );
//    }
}
