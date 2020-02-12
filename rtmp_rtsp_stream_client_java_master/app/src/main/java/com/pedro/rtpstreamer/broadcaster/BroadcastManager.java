package com.pedro.rtpstreamer.broadcaster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

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
import com.pedro.rtpstreamer.server.AWSConnection;
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
import java.util.List;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.P)
public class BroadcastManager
    implements ConnectCheckerRtmp {

    private static final BroadcastManager ourInstance = new BroadcastManager();

    public static BroadcastManager getInstance() {
        return ourInstance;
    }

    private BroadcastManager(){

    }

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
    private String broadcastName = "";

    private int cW = 640;
    private int cH = 480;

    public void setBroadcastManager(Context context, Resources resources, OpenGlView openGlView){
        pContext = context;
        pResources = resources;

        try {
            broadcastListener = (BroadcastListener) context;
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
        }

        this.rtmpCamera1 = new RtmpCamera1(openGlView, this);
        List<Camera.Size> s = rtmpCamera1.getResolutionsBack();
        DisplayMetrics dm = pContext.getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        Camera.Size ss = getPreviewSize(s, width, height);
        cW = ss.width;
        cH = ss.height;
        Log.d("resolution", ""+width+"/"+height+"/"+cW+"/"+cH);
    }

    public Camera.Size getPreviewSize(List<Camera.Size> sizes, int w, int h) {

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }
    ////////////////////////////////////////////////////////////////////////
    public void manageBroadcast(int i){
        switch (i){
            case 0:
                if (!rtmpCamera1.isStreaming()) startBroadcast();
                else {
                    uploadFile();
                    stopBroadcast();
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
        if(broadcastChannel == -1){
            broadcastListener.setToast("All broadcast channels are in use");
            return;
        }
        if(rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo(cW, cH)){
            rtmpCamera1.startStream(StaticVariable.bambuserDefaultUrl +""+ StaticVariable.broadcastUrl[broadcastChannel]);
            broadcastListener.broadcastStart();
        } else broadcastListener.setToast("Error preparing stream, This device cant do it");
    }

    public void stopBroadcast(){
        broadcastListener.broadcastStop();
        rtmpCamera1.stopStream();
        if(rtmpCamera1.isRecording()) {
            rtmpCamera1.stopRecord();
            currentDateAndTime = "";
        }
    }

    public void setBroadcastChannel(int i){
        broadcastChannel = i;
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
            broadcastName = "test/myUploadedFileName";
            rtmpCamera1.startRecord(broadcastPath);
            Log.d("rv","recording / "+folder.getAbsolutePath());
        } catch (IOException e) {
            stopBroadcast();
            broadcastListener.setToast(e.getMessage());
        }
    }

    private void uploadFile() {
        if(rtmpCamera1.isRecording()) {
            rtmpCamera1.stopRecord();
            currentDateAndTime = "";
            AWSConnection.uploadFile(broadcastName+".mp4", broadcastPath, pContext);
        }
    }

    public String getBroadcastName(){
        return broadcastName;
    }

    ///////////////////////////////////////////////////////////////////////////////
    public void setTexture(int i){
        switch(i){
            case 0:
                if(!onText) {
                    broadcastListener.setText();
                }
                else {
                    rtmpCamera1.getGlInterface().removeFilter(0);
                    spriteGestureControllerText.setBaseObjectFilterRender(null);
                    onText = false;
                }
                break;

            case 1:
                if(!onImage) {
//                    setImage();
                }
                else {
                    rtmpCamera1.getGlInterface().removeFilter(1);
                    spriteGestureControllerImg.setBaseObjectFilterRender(null);
                    onImage = false;
                }
                break;

            case 2:
                if(!onUri) {
//                    setUri();
                }
                else removeUri();
                break;
        }
    }

//    public boolean isImage(){return onImage;}

    public void setText(String text, int color){
        TextObjectFilterRender textObjectFilterRender = new TextObjectFilterRender();
        rtmpCamera1.getGlInterface().setFilterT(0, textObjectFilterRender);

        textObjectFilterRender.setText(text, 50, color);
        textObjectFilterRender.setDefaultScale(rtmpCamera1.getStreamWidth(), rtmpCamera1.getStreamHeight());
        textObjectFilterRender.setPosition(TranslateTo.CENTER);

        //move, scale
        spriteGestureControllerText.setBaseObjectFilterRender(textObjectFilterRender); //Optional
        spriteGestureControllerText.setPreventMoveOutside(false); //

        onText=true;
    }

    public void setImage(Bitmap bitmap){
        if(!onImage) {
            ImageObjectFilterRender imageObjectFilterRender = new ImageObjectFilterRender();
            rtmpCamera1.getGlInterface().setFilterT(1,imageObjectFilterRender);

            //set image and default setting
            imageObjectFilterRender.setImage(bitmap);
            imageObjectFilterRender.setDefaultScale(rtmpCamera1.getStreamWidth(), rtmpCamera1.getStreamHeight());
            imageObjectFilterRender.setScale(50f, 33.3f);
            imageObjectFilterRender.setPosition(TranslateTo.RIGHT);

            //move,scale
            spriteGestureControllerImg.setBaseObjectFilterRender(imageObjectFilterRender); //Optional
            spriteGestureControllerImg.setPreventMoveOutside(false); //

            onImage = true;
        }
        else {
            rtmpCamera1.getGlInterface().removeFilter(1);
            spriteGestureControllerImg.setBaseObjectFilterRender(null);
            onImage = false;
        }
    }

    public void setUri(Uri uri){
        Log.d("Uri", "setUri");
        onUri = true;
        broadcastListener.onUriLoading();
//        mUri = Uri.parse("https://s3.ap-northeast-2.amazonaws.com/asset.solmaru.co.kr/BRD/qRusKMnmVg.mp4");
        mUri = uri;
        surfaceFilterRender = new SurfaceFilterRender();
        rtmpCamera1.getGlInterface().setFilterT(2,surfaceFilterRender);
        handler.sendEmptyMessageDelayed(10, 100);
    }

    public boolean isUri(){return onUri;}

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
        rtmpCamera1.startPreview(cW, cH);
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
        recordVideo();
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

    public boolean isImage() {
        return onImage;
    }
}
