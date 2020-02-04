package com.pedro.rtpstreamer.broadcaster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.ResultListener;
import com.amplifyframework.storage.result.StorageUploadFileResult;
import com.pedro.encoder.input.gl.SpriteGestureController;
import com.pedro.encoder.input.gl.render.filters.object.ImageObjectFilterRender;
import com.pedro.encoder.input.gl.render.filters.object.SurfaceFilterRender;
import com.pedro.encoder.input.gl.render.filters.object.TextObjectFilterRender;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.encoder.utils.gl.TranslateTo;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;
import com.pedro.rtplibrary.view.OpenGlView;
import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.utils.StaticVariable;

import net.ossrs.rtmp.ConnectCheckerRtmp;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BroadcastManager
    implements ConnectCheckerRtmp {

    private Context pContext;
    private Resources pResources;

    private BroadcastListener broadcastListener;
    private RtmpCamera1 rtmpCamera1;

    private SpriteGestureController spriteGestureControllerText = new SpriteGestureController();
    private SpriteGestureController spriteGestureControllerImg = new SpriteGestureController();
    private SpriteGestureController spriteGestureControllerUri = new SpriteGestureController();

    private boolean onText=false;
    private boolean onImage=false;
    private boolean onUri=false;

    private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/rtmp-rtsp-stream-client-java");
    private String currentDateAndTime = "";

    //field for uri
    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private SurfaceFilterRender surfaceFilterRender;
    private Uri mUri;

    private int broadcastChannel = -1;
    private String broadcastPath = "";

    public BroadcastManager(Context context, Resources resources, OpenGlView openGlView){
        pContext = context;
        pResources = resources;

        try {
            broadcastListener = (BroadcastListener) context;
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
        }

        this.rtmpCamera1 = new RtmpCamera1(openGlView, this);
    }
    ////////////////////////////////////////////////////////////////////////
    public void manageBroadcast(int i){
        switch (i){
            case 0:
                if (!rtmpCamera1.isStreaming()) startBroadcast();
                else {
                    stopBroadcast();
                    //uploadFile();
                }
                break;

            case 1:
                try {
                    rtmpCamera1.switchCamera();
                } catch (CameraOpenException e) {
                    broadcastListener.setToast(e.getMessage());
                }
                break;
        }
    }

    public void startBroadcast(){
        broadcastChannel = getAvailableChannel();
        if(broadcastChannel == -1){
            broadcastListener.setToast("All broadcast channels are in use");
            return;
        }
        if(rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()){
            broadcastListener.broadcastStart();
            rtmpCamera1.startStream(StaticVariable.defaultUrl+""+ StaticVariable.broadcastUrl[broadcastChannel]);
            recordVideo();
        } else broadcastListener.setToast("Error preparing stream, This device cant do it");
    }

    public void stopBroadcast(){
        freeChannel(broadcastChannel);
        broadcastChannel = -1;
        broadcastListener.broadcastStop();
        rtmpCamera1.stopStream();
        if(rtmpCamera1.isRecording()) {
            rtmpCamera1.stopRecord();
            currentDateAndTime = "";
        }
    }

    ////////////////구현 필요 @민아
    //리턴 값은 사용할 채널번호
    //해당 채널 번호는 리턴 하기 전에 이 함수 내에서 사용중으로 상태를 변경해야 함
    //만약 사용 가능한 채널이 없으면(모든 채널이 사용 중이면) -1을 리턴함
    public int getAvailableChannel(){

        return 2;
    }

    ///////////////구현 필요 @민아
    //channelNum은 사용이 끝난 방송 채널번호
    //해당 채널을 사용 가능한 상태로 변경
    public void freeChannel(int channelNum){

    }

    public void recordVideo(){
        try {
            if (!folder.exists()) {
                Log.d("rv","make folder");
                folder.mkdir();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            currentDateAndTime = sdf.format(new Date());
            broadcastPath=folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4";
            rtmpCamera1.startRecord(broadcastPath);
            Log.d("rv","recording / "+folder.getAbsolutePath());
        } catch (IOException e) {
            stopBroadcast();
            broadcastListener.setToast(e.getMessage());
        }
    }

    private void uploadFile() {
        Amplify.Storage.uploadFile(
                "test/myUploadedFileName.mp4",
                broadcastPath,
                new ResultListener<StorageUploadFileResult>() {
                    @Override
                    public void onResult(StorageUploadFileResult result) {
                        Log.i("StorageQuickStart", "Successfully uploaded: " + result.getKey());
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e("StorageQuickstart", "Upload error.", error);
                    }
                }
        );
    }

    ///////////////////////////////////////////////////////////////////////////////
    public void setTexture(int i){
        switch(i){
            case 0:
                if(!onText) setText();
                else {
                    rtmpCamera1.getGlInterface().removeFilter(0);
                    spriteGestureControllerText.setBaseObjectFilterRender(null);
                    onText = false;
                }
                break;

            case 1:
                if(!onImage) setImage();
                else {
                    rtmpCamera1.getGlInterface().removeFilter(1);
                    spriteGestureControllerImg.setBaseObjectFilterRender(null);
                    onImage = false;
                }
                break;

            case 2:
                if(!onUri) setUri();
                else removeUri();
                break;
        }
    }

    public void setText(){
        TextObjectFilterRender textObjectFilterRender = new TextObjectFilterRender();
        rtmpCamera1.getGlInterface().setFilterT(0, textObjectFilterRender);

        textObjectFilterRender.setText("Hello world", 30, Color.BLUE);
        textObjectFilterRender.setDefaultScale(rtmpCamera1.getStreamWidth(), rtmpCamera1.getStreamHeight());
        textObjectFilterRender.setPosition(TranslateTo.CENTER);

        //move, scale
        spriteGestureControllerText.setBaseObjectFilterRender(textObjectFilterRender); //Optional
        spriteGestureControllerText.setPreventMoveOutside(false); //

        onText=true;
    }

    public void setImage(){
        ImageObjectFilterRender imageObjectFilterRender = new ImageObjectFilterRender();
        rtmpCamera1.getGlInterface().setFilterT(1,imageObjectFilterRender);

        //set image and default setting
        imageObjectFilterRender.setImage(BitmapFactory.decodeResource(pResources, R.mipmap.homiimg));
        imageObjectFilterRender.setDefaultScale(rtmpCamera1.getStreamWidth(), rtmpCamera1.getStreamHeight());
        imageObjectFilterRender.setScale(50f, 33.3f);
        imageObjectFilterRender.setPosition(TranslateTo.RIGHT);

        //move,scale
        spriteGestureControllerImg.setBaseObjectFilterRender(imageObjectFilterRender); //Optional
        spriteGestureControllerImg.setPreventMoveOutside(false); //

        onImage = true;
    }

    public void setUri(){
        Log.d("Uri", "setUri");
        onUri = true;
        broadcastListener.onUriLoading();
        mUri = Uri.parse("https://s3.ap-northeast-2.amazonaws.com/asset.solmaru.co.kr/BRD/qRusKMnmVg.mp4");
        surfaceFilterRender = new SurfaceFilterRender();
        rtmpCamera1.getGlInterface().setFilterT(2,surfaceFilterRender);
        handler.sendEmptyMessageDelayed(10, 100);
    }

    public void removeUri(){
        Log.d("destroy", "destroy");
        try {
            mMediaPlayer.release();
            mLibVLC.release();
        } catch (Exception e) {

        }
        rtmpCamera1.getGlInterface().removeFilter(2);
        spriteGestureControllerUri.setBaseObjectFilterRender(null);
        onUri = false;
    }

    //////////////////////////////////////////////
    //handler for play uri
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    if (surfaceFilterRender.getSurface() == null) {
                        handler.sendEmptyMessageDelayed(10, 10);
                    }
                    broadcastListener.offUriLoading();

                    try {
                        final ArrayList<String> args = new ArrayList<>();
                        args.add("-vvv");
                        args.add("-vvv");
                        mLibVLC = new LibVLC(pContext, args);

                        mMediaPlayer = new MediaPlayer(mLibVLC);
                        final Media media = new Media(mLibVLC, mUri);
                        mMediaPlayer.getVLCVout().setVideoSurface(surfaceFilterRender.getSurface(), null);
                        mMediaPlayer.getVLCVout().attachViews(null);
                        mMediaPlayer.setEventListener(event->{
                            switch (event.type) {
                                case MediaPlayer.Event.Buffering:
                                    Log.d("mediaP","buffering");
                                    break;

                                case MediaPlayer.Event.EncounteredError:
                                    Log.d("mediaP","encounteredE");
                                    break;

                                case MediaPlayer.Event.EndReached:
                                    Log.d("mediaP","endReached");
                                    break;

                                case MediaPlayer.Event.Stopped:
                                    removeUri();
                                    break;
                            }
                        });
                        mMediaPlayer.setMedia(media);
                        //Video is 360x240 so select a percent to keep aspect ratio (50% x 33.3% screen)
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Invalid asset folder");
                    }

                    mMediaPlayer.play();
                    if (mUri.toString().contains("test009.mp4")) surfaceFilterRender.setScale(100f, 100f);
                    else surfaceFilterRender.setScale(50f, 33.3f);
                    spriteGestureControllerUri.setBaseObjectFilterRender(surfaceFilterRender); //Optional

                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + msg.what);
            }
        }
    };
    ///////////////////////////////////////////////////////////////////////
    public boolean touchSurface(View view, MotionEvent motionEvent){
        if (spriteGestureControllerText.spriteTouched(view, motionEvent)) {
            spriteGestureControllerText.moveSprite(view, motionEvent);
            spriteGestureControllerText.scaleSprite(motionEvent);
            return true;
        }
        if (spriteGestureControllerImg.spriteTouched(view, motionEvent)) {
            spriteGestureControllerImg.moveSprite(view, motionEvent);
            spriteGestureControllerImg.scaleSprite(motionEvent);
            return true;
        }
        if (spriteGestureControllerUri.spriteTouched(view, motionEvent)) {
            spriteGestureControllerUri.moveSprite(view, motionEvent);
            spriteGestureControllerUri.scaleSprite(motionEvent);
            return true;
        }
        return false;
    }

    public void surfaceChange(){
        rtmpCamera1.startPreview();
    }

    public void surfaceDestroy(){
        Log.d("destroy", "destroy");
        if (rtmpCamera1.isStreaming()) {
            stopBroadcast();
            broadcastListener.broadcastStop();
        }
        rtmpCamera1.stopPreview();
    }

    public void destroy(){
        Log.d("destroy", "destroy");
        try{
            mMediaPlayer.release();
            mLibVLC.release();
        } catch (Exception e){
            Log.d("bm","error in destroy");
        }
    }

    ///////////////////////////////////////////////////////////////////////
    //ConnectCheckerRtmp
    @Override
    public void onConnectionSuccessRtmp() {
        broadcastListener.setToast("Connection success");
    }

    @Override
    public void onConnectionFailedRtmp(final String reason) {
        broadcastListener.connectionFailed(reason);
    }

    @Override
    public void onNewBitrateRtmp(long bitrate) {

    }

    @Override
    public void onDisconnectRtmp() {
        broadcastListener.setToast("Disconnected");
    }

    @Override
    public void onAuthErrorRtmp() {
        broadcastListener.setToast("Auth error");
    }

    @Override
    public void onAuthSuccessRtmp() {
        broadcastListener.setToast("Auth success");
    }

}
