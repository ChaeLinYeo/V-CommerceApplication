package com.pedro.rtpstreamer.replayer;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.server.AWSConnection;
import com.pedro.rtpstreamer.server.AWSListner;
import com.pedro.rtpstreamer.server.AWSfileManager;
import com.pedro.rtpstreamer.server.Pair;
import com.pedro.rtpstreamer.utils.ExampleChatController;
import com.pedro.rtpstreamer.utils.StaticVariable;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;


@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Replayer extends AppCompatActivity
    implements View.OnClickListener, AWSListner {

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
    TextView heart;
    private ExampleChatController ECC;
    private LottieAnimationView songLikeAnimButton;
    private int nextIndex=0;
    ArrayList<Pair> CL;
    ArrayList<Pair> TL;

    private int completeFile = 0;
    private String finalHeartNum;
    private String finalViewNum;
    private String USERID;

    private ArrayList<String> timeLine = new ArrayList<>();

    private boolean byTimeLine = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.replayer);
        context = this;

        AWSConnection.setAwsListner(this);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        AWSConnection.downloadFile("myUploadedFileName", path+"/chatDown/chat.txt", context);
        AWSConnection.downloadFile("myUploadedFileName_timeLine", path+"/chatDown/timeline.txt", context);

        findViewById(R.id.timelineButton).setOnClickListener(this);

        songLikeAnimButton = findViewById(R.id.heartView);
        playBtn = findViewById(R.id.playBtn);
        seekBar = findViewById(R.id.seekBar);
        title = findViewById(R.id.replaytitle);
        streamer_nickname = findViewById(R.id.replaynickname);
        listView = findViewById(R.id.ChatListView);
        heart=findViewById(R.id.reheartnum);
        playBtn.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        ECC = new ExampleChatController(context, listView, R.layout.chatline, R.id.chat_line_textview, R.id.chat_line_timeview);
        ECC.show();
        ECC.add2("재방송 채팅입니다.");
    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.playBtn:
                if(mediaState==0) {
                    nextIndex = 0;
                    setUri();
                }
                else if(mediaState==1) mMediaPlayer.pause();
                else mMediaPlayer.play();
                break;

            case R.id.timelineButton:
                popTimeLine();
                break;
        }
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser && mediaState != 0){
                Log.d("PKRR","progresschanged");
                float pr = ((float) progress) / 1000f;
                mMediaPlayer.setPosition(pr);
                long d = mMediaPlayer.getTime();
                for(int i=0; i<CL.size(); i++){
                    if(CL.get(i).getTime() >= d) {
                        nextIndex = i;
                        break;
                    }
                }
                ECC.clear();
                for(int i=0; i<nextIndex; i++){
                    Pair cp = CL.get(i);
                    if(cp.getType().equals("chat")) {
                        ECC.add2(cp.getMsg());
                    }
                }
            } else if(byTimeLine){
                Log.d("PKRR","progresschanged byTimeLine");
                long d = mMediaPlayer.getTime();
                for(int i=0; i<CL.size(); i++){
                    if(CL.get(i).getTime() >= d) {
                        nextIndex = i;
                        break;
                    }
                }
                ECC.clear();
                for(int i=0; i<nextIndex; i++){
                    Pair cp = CL.get(i);
                    if(cp.getType().equals("chat")) {
                        ECC.add2(cp.getMsg());
                    }
                }
                byTimeLine = false;
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
        t.End();
        AWSfileManager c = new AWSfileManager("chat");
        try {
            ArrayList<String>temp = c.setTL();
            for(String tmp : temp){
                CL.add(LogParser(tmp));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        c.End();

        finalHeartNum = CL.get(CL.size()-3).getMsg();
        finalViewNum = CL.get(CL.size()-2).getMsg();
        USERID = CL.get(CL.size()-1).getMsg();
        streamer_nickname.setText(USERID);
        heart.setText(finalHeartNum);
        TextView p = findViewById(R.id.peoplenum);
        p.setText(finalViewNum);

        for(int i=0; i<TL.size(); i++){
            timeLine.add(TL.get(i).getType());
        }

        Log.d("PKRA","end setlog");
    }

    public void removeUri(){
        try {
            mMediaPlayer.release();
            mLibVLC.release();
        } catch (Exception e) {

        }
    }

    public Pair LogParser(String Log1){
        int index = Log1.indexOf("/");
        Pair p = new Pair(Long.parseLong(Log1.substring(0,index)), Log1.substring(index+1));// [0]시간 / [1]타입 / 내용 (chat제외 무시하는 값)
        return p;
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
                                    if(nextIndex == CL.size()-2) break;
                                    Pair cp = CL.get(nextIndex);
                                    Log.d("PKR2","time : "+cp.getTime()+"/"+d+" type : "+cp.getType() + "msg : "+cp.getMsg());
                                    if(cp.getTime() <= d){
                                        playChat(cp);
                                        nextIndex++;
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
                                    byTimeLine = false;
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

    private void playChat(Pair cp){
        switch (cp.getType()) {
            case "chat" :
                ECC.add2(cp.getMsg());
                break;

            case "title":
                title.setText(cp.getMsg());
                break;

            case "like" :
                heartAni();
                Log.d("PKR","play heart");
                break;
        }
    }

    private void heartAni(){
        // 애니메이션을 한번 실행시킨다.
        // Custom animation speed or duration.
        // ofFloat(시작 시간, 종료 시간).setDuration(지속시간)
        songLikeAnimButton.setVisibility(View.VISIBLE);
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 0.6f).setDuration(500);

        animator.addUpdateListener((ValueAnimator animation) -> {
                    float animatedValue = (Float) animation.getAnimatedValue();
                    songLikeAnimButton.setProgress(animatedValue);
                }
        );
        animator.start();
    }

    @Override
    public void downloadComplete(){
        completeFile++;
        if(completeFile == 2){
            CL = new ArrayList<>();
            TL = new ArrayList<>();
            setLog();

            setUri();
        }
    }

    public void popTimeLine(){
        View mView_c = getLayoutInflater().inflate(R.layout.popup_timeline, null);

        // ArrayAdapter 생성. 아이템 View를 선택(multiple choice)가능하도록 만듦.
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, timeLine) ;

        // listview 생성 및 adapter 지정.
        ListView listView = mView_c.findViewById(R.id.timelineList) ;
        listView.setAdapter(adapter1);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final AlertDialog.Builder alert05 = new AlertDialog.Builder(this);

        Button btn_Exit = mView_c.findViewById(R.id.timelineExit);

        alert05.setView(mView_c);

        final AlertDialog alertDialog = alert05.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_Exit.setOnClickListener((View view) -> alertDialog.dismiss());

        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                Log.d("PKRA","category time : "+TL.get(position).getTime());
                mMediaPlayer.setTime(TL.get(position).getTime());
                byTimeLine = true;
                int mediaPosition = (int) (mMediaPlayer.getPosition()*1000);
                seekBar.setProgress(mediaPosition);
                alertDialog.dismiss();
            }
        );

        alertDialog.show();
    }
}
