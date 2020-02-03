package com.pedro.rtpstreamer.replayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.utils.StaticVariable;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Replayer extends AppCompatActivity
    implements View.OnClickListener {

    Context context;

    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private Uri mUri;

    private Button playBtn;
    private SeekBar seekBar;

    private int mediaState=0;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.replayer);
        context = this;

        playBtn = findViewById(R.id.playBtn);
        seekBar = findViewById(R.id.seekBar);

        playBtn.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        setUri();
    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.playBtn:
                if(mediaState==0) setUri();
                else if(mediaState==1) mMediaPlayer.pause();
                else mMediaPlayer.play();
        }
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser && mediaState != 0){
                float pr = ((float) progress) / 1000f;
                mMediaPlayer.setPosition(pr);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public void setUri(){
        mUri = Uri.parse(StaticVariable.stoargeUrl+"test/myUploadedFileName.mp4");
        handler.sendEmptyMessageDelayed(10, 100);
    }

    public void removeUri(){
        Log.d("destroy", "destroy");
        try {
            mMediaPlayer.release();
            mLibVLC.release();
        } catch (Exception e) {

        }
    }

    //////////////////////////////////////////////
    //handler for play uri
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    try {
                        final ArrayList<String> args = new ArrayList<>();
                        args.add("-vvv");
                        args.add("-vvv");
                        mLibVLC = new LibVLC(context, args);

                        mMediaPlayer = new MediaPlayer(mLibVLC);
                        final Media media = new Media(mLibVLC, mUri);
                        SurfaceView surfaceView = findViewById(R.id.video_layout);
                        mMediaPlayer.getVLCVout().setVideoView(surfaceView);
                        mMediaPlayer.getVLCVout().setWindowSize(surfaceView.getWidth(),surfaceView.getHeight());
                        mMediaPlayer.getVLCVout().attachViews(null);
                        mMediaPlayer.setEventListener(event->{
                            switch (event.type) {
                                case MediaPlayer.Event.Playing:
                                    Log.d("mediaP","playing");
                                    playBtn.setText("stop");
                                    mediaState=1;
                                    break;

                                case MediaPlayer.Event.TimeChanged:
                                    int position = (int) (mMediaPlayer.getPosition()*1000);
                                    seekBar.setProgress(position);
                                    break;

                                case MediaPlayer.Event.Paused:
                                    playBtn.setText("start");
                                    mediaState=2;
                                    break;

                                case MediaPlayer.Event.Stopped:
                                    Log.d("mHandler","stop");
                                    playBtn.setText("start");
                                    mediaState=0;
                                    removeUri();
                                    break;
                            }
                        });
                        mMediaPlayer.setMedia(media);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Invalid asset folder");
                    }
                    mMediaPlayer.play();
                    break;

                default:
                    throw new IllegalStateException("Unexpected value: " + msg.what);
            }
        }
    };
}
