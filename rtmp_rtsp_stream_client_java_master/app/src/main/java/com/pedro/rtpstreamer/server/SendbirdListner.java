package com.pedro.rtpstreamer.server;

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
}
