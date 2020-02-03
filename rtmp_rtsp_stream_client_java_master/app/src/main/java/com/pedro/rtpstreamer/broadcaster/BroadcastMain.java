package com.pedro.rtpstreamer.broadcaster;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.Button;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.ResultListener;
import com.amplifyframework.storage.result.StorageUploadFileResult;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;
import com.pedro.rtplibrary.view.OpenGlView;
import com.pedro.rtpstreamer.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BroadcastMain extends AppCompatActivity
        implements View.OnClickListener, View.OnTouchListener, SurfaceHolder.Callback, BroadcastListener{

    Context context;
    private BroadcastManager broadcastManager;

    private Button broadcastBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
//            @Override
//            public void onResult(UserStateDetails userStateDetails) {
//                try {
//                    Amplify.addPlugin(new AWSS3StoragePlugin());
//                    Amplify.configure(getApplicationContext());
//                    Log.i("StorageQuickstart", "All set and ready to go!");
//                } catch (Exception e) {
//                    Log.e("StorageQuickstart", e.getMessage());
//                }
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.e("StorageQuickstart", "Initialization error.", e);
//            }
//        });

        super.onCreate(savedInstanceState);
        context = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.broadcast_main);

        setVideoUI();
    }

    //setUI for video
    public void setVideoUI(){
        OpenGlView openGlView;
        openGlView = findViewById(R.id.backGroundVideo);
        openGlView.getHolder().addCallback(this);
        openGlView.setOnTouchListener(this);
        openGlView.ogvInit();
        broadcastManager = new BroadcastManager(context, getResources(), openGlView);

        findViewById(R.id.textButton).setOnClickListener(this);
        findViewById(R.id.imgButton).setOnClickListener(this);
        findViewById(R.id.uriButton).setOnClickListener(this);
        findViewById(R.id.switchButton).setOnClickListener(this);

        broadcastBtn = findViewById(R.id.b_start_stop);
        broadcastBtn.setOnClickListener(this);
    }

    ///////////////////////////////////////////////////////////////////////
    //View.onClickListener
    //For broadcastBtn
    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.textButton:
                broadcastManager.setTexture(0);
                break;

            case R.id.imgButton:
                broadcastManager.setTexture(1);
                break;

            case R.id.uriButton:
                broadcastManager.setTexture(2);
                break;

            case R.id.b_start_stop:
                broadcastManager.manageBroadcast(0);
                break;

            case R.id.switchButton:
                broadcastManager.manageBroadcast(1);
                break;
        }
    }
    ////////////////////////////////////////////////////////////////////////
    //View.OnTouchListener
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent){
        if(broadcastManager.touchSurface(view, motionEvent)) return true;
        view.performClick();
        return false;
    }

    ///////////////////////////////////////////////////////////////////////
    //SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        broadcastManager.surfaceChange();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        broadcastManager.surfaceDestroy();
    }

    /////////////////////////////////////////////////////
    @Override
    protected void onDestroy() {
        super.onDestroy();
        broadcastManager.destroy();
    }

    ////////
    @Override
    public void onUriLoading(){
        findViewById(R.id.tv_loading).setVisibility(View.VISIBLE);
    }

    @Override
    public void offUriLoading(){
        findViewById(R.id.tv_loading).setVisibility(View.GONE);
    }

    @Override
    public void connectionSuccess(){

    }

    @Override
    public void connectionFailed(String reason){
        runOnUiThread(()->{
            Toast.makeText(BroadcastMain.this, "Connection failed. " + reason, Toast.LENGTH_SHORT).show();
            broadcastManager.stopBroadcast();
            broadcastBtn.setText(R.string.start_button);
        });
    }

    @Override
    public void broadcastStart(){
        broadcastBtn.setText(R.string.stop_button);
    }

    @Override
    public void broadcastStop(){
        broadcastBtn.setText(R.string.start_button);
    }

    @Override
    public void setToast(String message){
        runOnUiThread(
                () -> Toast.makeText(BroadcastMain.this, message, Toast.LENGTH_SHORT).show()
        );
    }
}
