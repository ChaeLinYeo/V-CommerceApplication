package com.pedro.rtpstreamer.server;

public interface SendbirdListner {

    public interface ForBroadcaster {
        public void channelFounded(boolean possible);

        public void channelCreateComplete();

        public void getUserListComplete(String peopleNum);

        public void messageReceived(String customType, String data, long time);

        public void metaCounterUpdated(int heart);
    }

    public interface ForPlayer {
        public void getChannelComplete(boolean possible);
    }
}
