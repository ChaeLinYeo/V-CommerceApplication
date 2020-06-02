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
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    /////////////////////////////////////////////////////////////////////////////////

    public static final int numChannel = 10;

    public static final String[] broadcastAuthor = new String[]{
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "10"
    };

    public static final String[] broadcastUrl = new String[]{
            "qOgnezRYeSLAzn0Pij29XA",
            "QweU4yQDmXub7Tq0MzI0Eg",
            "JjG6KPVr57cC2SroNF07xw",
            "C8yyx20S2zJq72Jchw8S3A",
            "ulCz6tYY438IF1ZBVg9RiQ",
            "X6haZHegy5Bs8ptMKXYxmg",
            "ksIcSWFKEc0gbQQ9UMUHuQ",
            "927uVWfmaGxqcQ0DrDlpRQ",
            "9araKWtthfNDlbk5Z9AtUA",
            "wo4nQdvMhVxp8RieR544aQ"
    };

    public static final String bambuserDefaultUrl = "rtmp://ingest.bambuser.io/b-fme/";

    public static final String stoargeUrl = "https://rtmprtspstreamclientec4408a7ba95486ebe7ba8f169495201-dev.s3.us-east-2.amazonaws.com/public/";

    public static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHAT";

    public static final String sendbirdCtrlChannel = "sendbird_Ctrl";
}
