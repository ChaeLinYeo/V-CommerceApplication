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
            "9478bfc4dwa346ca7321e735506d3c10f5felakdf",
            "9478bfc47a346c37321e735506d3c10f5felakdf",
            "94214adc47a346c37321e735506d3c10f5felakdf",
            "9478bfc47a346c3732alwekfjwehr329ds",
            "9478bfc47a346cdagshe735506d3c10f5f42dda8",
            "9478bsdkfjlfefwffjwehr329ds",
            "9478bfc47a346alsdkjawaehr329ds",
            "9478bfc4dalksdjflwf0f5felakdf",
            "94daljdflafwfc10f5felakdf"
    };

    public static final String[] broadcastUrl = new String[]{
            "9ps2NHuM3FlgvIfZQOBs4w",
            "JXVa1xwGa2sPOLFRGgqkYg",
            "xk87jakTO3ee6azCJZgbpw",
            "5dX10WBLqNvBZiLzrn9EaA",
            "tbGsBYNOjwPKOLFED3VH5Q",
            "yYnbg9oyYqY2FiY00bAmrw",
            "bDdg6d8EZoq47q4dEwLH2g",
            "m1AYUqSTCfEXkdIrV6zjdg",
            "vboOakEcKWdYE0K0wMLYMA",
            "JZPrP7OrLMjZyAn4mfYb4Q"
    };

    public static final String bambuserDefaultUrl = "rtmp://ingest.bambuser.io/b-fme/";

    public static final String stoargeUrl = "https://rtmprtspstreamclientec4408a7ba95486ebe7ba8f169495201-dev.s3.us-east-2.amazonaws.com/public/";

    public static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHAT";
}
