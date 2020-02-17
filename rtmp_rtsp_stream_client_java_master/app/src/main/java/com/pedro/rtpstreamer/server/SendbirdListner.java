package com.pedro.rtpstreamer.server;

import com.sendbird.android.BaseMessage;

public abstract class SendbirdListner {

//    public interface ForBroadcaster {
//        public void getChannelComplete(boolean possible);
//
//        public void channelCreateComplete();
//
//        public void getUserListComplete(String peopleNum);
//
//        public void messageReceived(String customType, String data, long time);
//
//        public void metaCounterUpdated(int heart);
//    }
//
//    public interface ForPlayer {
//        public void getChannelComplete(boolean possible);
//    }

    public void getCtrlComplete(){}
    public void getChannelComplete(boolean success){}
    public void messageReceived(String customType, String data, long time){}
    public void metaCounterUpdated(int heart){}
    public void channelCreateComplete(){}
    public void getUserListComplete(String peopleNum){}
    public void getPlayChannelComplete(String coverUrl, String title, String operator){}
    public void onMessageReceived(String customType, String data){}
    public void loadInitialMessage(String type, String data){}
    public void onTitleChanged(String titleString){}
    public void userenter(String enterduser){}
    public void Imbanned(){}
    public void Imunbanned(){}
    }
