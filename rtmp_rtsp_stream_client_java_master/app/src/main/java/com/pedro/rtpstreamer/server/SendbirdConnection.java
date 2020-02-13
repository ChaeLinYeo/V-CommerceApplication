package com.pedro.rtpstreamer.server;

import android.content.Context;
import android.util.Log;

import com.pedro.rtpstreamer.R;
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
    private static SendbirdListner.ForBroadcaster forBroadcaster;
    /////////////////////////////////////////////////////////////////////////////
    private static List<User> operator = new ArrayList<>();
    private static OpenChannel ctrl_channel;
    private static OpenChannel mOpenChannel;

    private static String CHANNEL_URL;
    private static int channelNum;

    private static List<User> UserList = new ArrayList<>();

    private static int viewNum=0;

    private static String[] channelUrl = new String[10];

    public static void setupSendbird(Context context, String USER_ID, boolean isOperator) {
        //////////////////////////////////////////////
        //try catch
        if(isOperator) forBroadcaster = (SendbirdListner.ForBroadcaster) context;
        //////////////////////////////////////////////

        SendBird.init(context.getString(R.string.sendbird_app_id), context);
        SendBird.connect(USER_ID,
            (User user, SendBirdException e) -> {
                if (e != null) {    // Error.
                    Log.d("connect error","connect : 1" );
                    return;
                }
                if(isOperator) operator.add(user);
                SendBird.updateCurrentUserInfo(USER_ID, null,
                    (SendBirdException ex) -> {
                        if (ex != null) Log.e("nickname",ex.getMessage()+" : "+ex.getCode());
                    }
                );
            }
        );

        viewNum=0;
    }

    public static void getCtrl(Context context){
        OpenChannel.getChannel(context.getString(R.string.sendbird_ctrlChannel),
            (OpenChannel openChannel, SendBirdException e) -> {
                if (e != null) {    // Error.
                    Log.d("getCtrl", ""+e.getMessage());
                    return;
                }
                ctrl_channel = openChannel;
                ////////////////////////////
                getBroadcastChannel();
            }
        );
    }

    public static void getBroadcastChannel(){
        ctrl_channel.getAllMetaData((Map<String, String> map, SendBirdException ex) -> {
            for(int i = 0; i < StaticVariable.numChannel; i++){
                if(map.get(Integer.toString(i)).equals("true")){
                    forBroadcaster.getChannelComplete(true);
                    channelNum = i;
                    return;
                }
            }
            forBroadcaster.getChannelComplete(false);
        });
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
                    updateMetaData(ctrl_channel, Integer.toString(channelNum), CHANNEL_URL);
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
                forBroadcaster.metaCounterUpdated(metaCounterMap.get("heart"));
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

    public static int getChannelNum(){
        return channelNum;
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
            UserList = setUserList(list);
            forBroadcaster.getUserListComplete(Integer.toString(UserList.size()));
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
        updateMetaData(ctrl_channel, Integer.toString(channelNum), "true");
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

    public static int getViewNum(){return viewNum;}
}
