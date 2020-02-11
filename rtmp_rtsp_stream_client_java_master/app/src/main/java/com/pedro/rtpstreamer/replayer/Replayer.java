package com.pedro.rtpstreamer.replayer;
/*
* 로그에 있는 채팅은 ECC.add
* like는 하트 애니메이션 play
*/
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.server.AWSConnection;
import com.pedro.rtpstreamer.server.AWSfileManager;
import com.pedro.rtpstreamer.server.Pair;
import com.pedro.rtpstreamer.utils.ExampleChatController;
import com.pedro.rtpstreamer.utils.StaticVariable;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Replayer extends AppCompatActivity
    implements View.OnClickListener {

    Context context;

    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private Uri mUri;

    private Button playBtn;
    private SeekBar seekBar;
    private ListView listView;

    private int mediaState=0;

    // 다시보기 화면 구성용
    TextView streamer_nickname;
    TextView title;
    private ExampleChatController ECC;
    private LottieAnimationView songLikeAnimButton;

    private long nexttime = 0;
    ArrayList<Pair> CL;
    ArrayList<Pair> SL;
    ArrayList<Pair> TL;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.replayer);
        context = this;

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        AWSConnection.downloadFile("myUploadFileName", path+"/chatDown/chat", context);
        AWSConnection.downloadFile("myUploadFileName_subinfo", path+"/chatDown/sub", context);
        AWSConnection.downloadFile("myUploadFileName_timeLine", path+"/chatDown/timeline", context);

        playBtn = findViewById(R.id.playBtn);
        seekBar = findViewById(R.id.seekBar);
        title = findViewById(R.id.replaytitle);
        streamer_nickname = findViewById(R.id.nickname);
        listView = findViewById(R.id.ChatListView);
        playBtn.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        ECC = new ExampleChatController(context, listView, R.layout.chatline, R.id.chat_line_textview, R.id.chat_line_timeview);
        ECC.show();
        ECC.add("재방송 채팅입니다.");

        setLog();
        CL = new ArrayList<>();
        TL = new ArrayList<>();
        SL = new ArrayList<>();
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
    public void setLog(){
        AWSfileManager t = new AWSfileManager("timeline");
        try {
            ArrayList<String>temp = t.setTL();
            for(String tmp : temp){
                TL.add(LogParser(tmp));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        AWSfileManager c = new AWSfileManager("chat");


        AWSfileManager s = new AWSfileManager("sub");



    }
    public void removeUri(){
        Log.d("destroy", "destroy");
        try {
            mMediaPlayer.release();
            mLibVLC.release();
        } catch (Exception e) {

        }
    }
    public Pair LogParser(String Log){
        StringTokenizer st = new StringTokenizer(Log, "/");
        Pair p = new Pair(Long.parseLong(st.nextToken()), st.nextToken());// [0]시간 / [1]타입 / 내용 (chat제외 무시하는 값)

        return p;
        /*
        if(data[1].equals("chat")){
            ECC.add2(data[0] + ":" +data[2]);
        }else if(data[1].equals("like")){
            //좋아요 애니메이션 실행
        }else{
            //Log.d("d","d");
            //Log.e("sthwrong",data[1]);
            return;
        }*/
    }

    public void SubParser(String subinfo){
        String[] data = new String[2];// [0]시간 / [1]바뀐 제목
        StringTokenizer st = new StringTokenizer(subinfo, "/");
        data[0] = st.nextToken();
        data[1] = st.nextToken();

    }

    public void TimeParser(String timeline){
        String[] data = new String[2];// [0]시간 / [1]방송중인 상품
        StringTokenizer st = new StringTokenizer(timeline, "/");
        data[0] = st.nextToken();
        data[1] = st.nextToken();

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
                                    long d = mMediaPlayer.getTime(); //ms
                                    if(d==nexttime) {//play chat
                                        //nexttime =
                                    }
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
