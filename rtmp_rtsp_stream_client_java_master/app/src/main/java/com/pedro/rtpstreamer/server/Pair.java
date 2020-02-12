package com.pedro.rtpstreamer.server;

import android.util.Log;

import java.util.StringTokenizer;

public class Pair {
    long time;
    String data;
    String type;
    String msg;

    public Pair(long mtime, String mdata){
        Log.d("PKR1","time : " +mtime+" data : "+mdata);
        time = mtime;
        data = mdata;
        //StringTokenizer st = new StringTokenizer(data, "/");
        int delimindex = data.indexOf("/"); // 맨처음 :이 나타나는 index
        if(delimindex > 0) {
            this.type = data.substring(0, delimindex);//.nextToken();
            Log.d("PKR1","delimindex : "+type);
            if (!data.substring(delimindex + 1).equals("")) {
                this.msg = data.substring(delimindex + 1);
                Log.d("PKR1", "token " + msg);
            }
        } else{
            type = data;
        }
    }

    public String getType(){
        return type;
    }

    public String getMsg(){
        return msg;
    }
    public long getTime(){
        return time;
    }
}
