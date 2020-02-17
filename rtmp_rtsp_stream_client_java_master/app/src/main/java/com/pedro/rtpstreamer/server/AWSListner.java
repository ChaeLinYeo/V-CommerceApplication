package com.pedro.rtpstreamer.server;

public abstract class AWSListner {
    public void downloadComplete(){}

    public void startUpload(){}
    public void uploadComplete(boolean success){}
}
