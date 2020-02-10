package com.pedro.rtpstreamer.broadcaster;

public interface BroadcastListener {
    public void onUriLoading();
    public void offUriLoading();

    public void connectionSuccess();
    public void connectionFailed(String reason);

    public void broadcastStart();
    public void broadcastStop();

    public void setToast(String message);
    public void setText();

    public void setNoti();
}
