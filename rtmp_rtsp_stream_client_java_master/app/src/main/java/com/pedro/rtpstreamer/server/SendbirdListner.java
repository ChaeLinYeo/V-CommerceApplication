package com.pedro.rtpstreamer.server;

public interface SendbirdListner {
    public void channelFounded(boolean possible);
    public void channelCreateComplete();

    public void getUserListComplete(String peopleNum);
    public void messageReceived(String customType, String data);
    public void metaCounterUpdated(int heart);
}
