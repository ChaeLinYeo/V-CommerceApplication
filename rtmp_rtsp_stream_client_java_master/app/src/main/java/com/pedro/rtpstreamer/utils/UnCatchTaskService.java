package com.pedro.rtpstreamer.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.pedro.rtpstreamer.broadcaster.BroadcastManager;

@RequiresApi(api = Build.VERSION_CODES.P)
public class UnCatchTaskService extends Service {

    private BroadcastManager broadcastManager = BroadcastManager.getInstance();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) { //핸들링 하는 부분
        Log.e("Error","onTaskRemoved - " + rootIntent);

        broadcastManager.stopBroadcast();

        stopSelf(); //서비스도 같이 종료
    }
}
