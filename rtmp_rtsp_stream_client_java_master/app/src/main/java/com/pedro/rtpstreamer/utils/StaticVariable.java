package com.pedro.rtpstreamer.utils;

import android.Manifest;

public class StaticVariable {
    private static final StaticVariable ourInstance = new StaticVariable();

    public static StaticVariable getInstance() {
        return ourInstance;
    }

    private StaticVariable() {
    }

    /////////////////////////////////////////////////////////////////////////////////
    //권한 리스트
    //[ref] https://developer.android.com/reference/android/Manifest.permission.html#ACCEPT_HANDOVER
    public static final String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    /////////////////////////////////////////////////////////////////////////////////

    public static final int numChannel = 5;

    public static final String[] broadcastAuthor = new String[]{
            "9478bfc47a346c37321e735506d3c10f5f42dda8",
            "9478bfc47a346c37321e735506d3c10f5felakdf",
            "9478bfc4dwa346ca7321e735506d3c10f5felakdf",
            "94214adc47a346c37321e735506d3c10f5felakdf",
            "9478bfc47a346c3732alwekfjwehr329ds"
    };

    public static final String[] broadcastUrl = new String[]{
            "f91d39f8095317d3b01e139691b4aa91c8fc447a",
            "41c85459798f6ebab6546816a86711feb47fbfac",
            "e078de7f60088af4219f7da44ac936ad79365dda",
            "8de0286bbf8be3116d9006cf9dc5658b52b37367",
            "1faa82c517b3e5d6e82e31aba6b04397b92fb582"
    };

    public static final String defaultUrl = "rtmp://ingest.bambuser.io/b-fme/";

    public static final String stoargeUrl = "https://rtmprtspstreamclientec4408a7ba95486ebe7ba8f169495201-dev.s3.us-east-2.amazonaws.com/public/";
}
