package com.pedro.rtpstreamer.server;

import java.util.StringTokenizer;

public class Pair {
    long time;
    String data;
    String type;
    String msg;

    public Pair(long mtime, String mdata){
        time = mtime;
        data = mdata;
        StringTokenizer st = new StringTokenizer(data, "/");
        type = st.nextToken();
        msg = st.nextToken();

    }

    public String getType(){
        return type;
    }

    public String getMsg(){
        return msg;
    }
    public String getData(){
        return data;
    }
    public long getTime(){
        return time;
    }
}
