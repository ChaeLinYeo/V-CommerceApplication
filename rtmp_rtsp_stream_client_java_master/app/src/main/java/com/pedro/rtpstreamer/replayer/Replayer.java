package com.pedro.rtpstreamer.replayer;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
<<<<<<< HEAD
=======
import android.widget.FrameLayout;
import android.widget.ImageButton;
>>>>>>> 7cf3b82359f4f38f7ff968f960e71f50fbdaa459
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.server.AWSConnection;
import com.pedro.rtpstreamer.server.AWSListner;
import com.pedro.rtpstreamer.server.AWSfileManager;
import com.pedro.rtpstreamer.server.Pair;
import com.pedro.rtpstreamer.utils.ExampleChatController;
import com.pedro.rtpstreamer.utils.PopupManager;
import com.pedro.rtpstreamer.utils.StaticVariable;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


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
    TextView heart;
    private ExampleChatController ECC;
    private LottieAnimationView songLikeAnimButton;
    private int nextIndex=0;
    private int nextTimeline=1;
    ArrayList<Pair> CL;
    ArrayList<Pair> TL;

    private int completeFile = 0;
    private String finalHeartNum;
    private String finalViewNum;
    private String USERID;

    private RelativeLayout loadingPanel;

    private ArrayList<String> timeLine = new ArrayList<>();

    private boolean byTimeLine = false;

    private TextView currTimeline;
    private int onoff = 1;
    private int back_onoff = 1;

    private boolean is_follow = false;
    private Button FollowButton, redeclare;
    private TextView people;
    private ImageButton soundbtn;
    private ImageView heartbtn;
    private SurfaceView surfaceView;

    private TextView currentPlayTime;
    private TextView maxPlayTime;

    private int soundonoff = 1;
    private Context mContext;
    private RelativeLayout background, titleEtc;
    boolean savedStreamMuted = false;

    private ImageView hearticon;

    PopupManager PM;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.replayer);
        context = this;

        AWSConnection.setAwsListner(awsListner);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        AWSConnection.downloadFile("myUploadedFileName", path+"/chatDown/chat.txt");
        AWSConnection.downloadFile("myUploadedFileName_timeLine", path+"/chatDown/timeline.txt");

        findViewById(R.id.timelineButton).setOnClickListener(this);

        songLikeAnimButton = findViewById(R.id.heartView);
        playBtn = findViewById(R.id.playBtn);
        seekBar = findViewById(R.id.seekBar);
        title = findViewById(R.id.replaytitle);
        streamer_nickname = findViewById(R.id.replaynickname);
        listView = findViewById(R.id.ChatListView);
        heart=findViewById(R.id.reheartnum);
        heartbtn=findViewById(R.id.HeartIcon);
        soundbtn = findViewById(R.id.rebtn_sound);
        soundbtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        loadingPanel = findViewById(R.id.ReplayLoadingPanel);
        currTimeline = findViewById(R.id.curr_category);
        FollowButton = findViewById(R.id.refollowButton);
        FollowButton.setOnClickListener(this);
        redeclare = findViewById(R.id.redeclare);
        redeclare.setOnClickListener(this);
        hearticon = findViewById(R.id.reHeartIcon);
        hearticon.setOnClickListener(this);


        surfaceView = findViewById(R.id.video_layout);
        currentPlayTime = findViewById(R.id.currentPlayTime);
        maxPlayTime = findViewById(R.id.maxPlayTime);
        background = findViewById(R.id.re_rl_Live);
        titleEtc = findViewById(R.id.titleEtc);

        PM = new PopupManager(context);

        ECC = new ExampleChatController(context, listView, R.layout.chatline, R.id.chat_line_textview, R.id.chat_line_timeview);
        ECC.show();
        ECC.add2("재방송 채팅입니다.");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (!notificationManager.isNotificationPolicyAccessGranted()) {

            context.startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
        }
<<<<<<< HEAD
//        audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);


=======
        heartbtn.setOnClickListener((View view) -> {
            heartAni();
            int newheart = Integer.parseInt(heart.getText().toString()) + 1;
            heart.setText(Integer.toString(newheart));
        });
        
>>>>>>> 7cf3b82359f4f38f7ff968f960e71f50fbdaa459
        title.setOnClickListener((View view) -> {
            if(onoff == 1){
                background.setVisibility(View.GONE);
                ViewGroup.LayoutParams params = title.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                title.setLayoutParams(params);
                onoff = 0;
            }
            else if(onoff == 0){
                background.setVisibility(View.VISIBLE);
                //title.setHeight(30);
                onoff = 1;
            }
        });


        surfaceView.setOnClickListener((View view) -> {
            if(back_onoff == 1){
                titleEtc.setVisibility(View.GONE);
                background.setVisibility(View.GONE);
                back_onoff = 0;
            }
            else if(back_onoff == 0){
                titleEtc.setVisibility(View.VISIBLE);
                background.setVisibility(View.VISIBLE);
                back_onoff = 1;
            }
        });

    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.playBtn:
                if(mediaState == 0) {
                    nextIndex = 0;
                    nextTimeline=1;
                    setUri();
                }
                else if(mediaState==1) mMediaPlayer.pause();
                else mMediaPlayer.play();
                break;

            case R.id.timelineButton:
                popTimeLine();
                break;

            case R.id.refollowButton:
                Log.d("replayer", "followclick");
                btn_follow();
                break;

            case R.id.redeclare:
                Log.d("replayer", "declareclick");
                PM.select_Declare(getLayoutInflater());
                break;
            case R.id.rebtn_sound:
                Log.d("replayer", "soundclick");
                SoundOnOff();

                break;
        }
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser && mediaState != 0){
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

                for(int i=1; i<TL.size();i++){
                    if(TL.get(i).getTime() >= d){
                        nextTimeline = i;
                        if(i>0) currTimeline.setText("현재 "+TL.get(i-1).getType()+"을(를) 판매 중입니다");
                        break;
                    }
                }
            } else if(byTimeLine){
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
        people = findViewById(R.id.peoplenum);
        people.setText(finalViewNum);

        for(int i=1; i<TL.size(); i++){
            timeLine.add(TL.get(i).getType());
        }

    }

    public void removeUri(){
        try {
            mMediaPlayer.release();
            mLibVLC.release();
            mMediaPlayer = null;
            mLibVLC = null;
            mediaState = 0;
        } catch (Exception e) {

        }
    }

    public Pair LogParser(String Log1){
        int index = Log1.indexOf("/");
        Pair p = new Pair(Long.parseLong(Log1.substring(0,index)), Log1.substring(index+1));// [0]시간 / [1]타입 / 내용 (chat제외 무시하는 값)
        return p;
    }

    public String getStringForTime(long time){
        int s = (int) time/1000;
        int m = s>60 ? s/60 : 0;
        if(m>0) s = s%60;

        String mm = m<10 ? "0"+m : ""+m;
        String ss = s<10 ? "0"+s : ""+s;

        return mm+":"+ss;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        removeUri();
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
                        mMediaPlayer.getVLCVout().setVideoView(surfaceView);
                        mMediaPlayer.getVLCVout().setWindowSize(surfaceView.getWidth(),surfaceView.getHeight());
                        mMediaPlayer.getVLCVout().attachViews(null);
                        mMediaPlayer.setEventListener(event->{
                            switch (event.type) {
                                case MediaPlayer.Event.Buffering:
                                    if(event.getBuffering()<100) loadingPanel.setVisibility(VISIBLE);
                                    else loadingPanel.setVisibility(GONE);
                                    break;

                                case MediaPlayer.Event.Playing:
                                    playBtn.setText("stop");
                                    mediaState=1;
                                    long dd = mMediaPlayer.getLength();
                                    maxPlayTime.setText(getStringForTime(dd));
                                    break;

                                case MediaPlayer.Event.TimeChanged:
                                    long d = mMediaPlayer.getTime(); //ms
                                    currentPlayTime.setText(getStringForTime(d));

                                    if(nextIndex < CL.size()-2) {
                                        Pair cp = CL.get(nextIndex);
                                        if (cp.getTime() <= d) {
                                            playChat(cp);
                                            nextIndex++;
                                        }
                                    }
                                    if(nextTimeline < TL.size()) {
                                        if (TL.get(nextTimeline).getTime() <= d) {
                                            currTimeline.setText("현재 "+TL.get(nextTimeline).getType()+"을(를) 판매 중입니다");
                                            nextTimeline++;
                                        }
                                    }
                                    int position = (int) (mMediaPlayer.getPosition()*1000);
                                    seekBar.setProgress(position);
                                    break;

                                case MediaPlayer.Event.Paused:
                                    playBtn.setText("start");
                                    mediaState=2;
                                    break;

                                case MediaPlayer.Event.EndReached:
                                    mMediaPlayer.setMedia(media);
                                    playBtn.setText("start");
                                    mMediaPlayer.setTime(0);
                                    seekBar.setProgress(0);
                                    mediaState=3;
                                    byTimeLine = false;
                                    break;

                                case MediaPlayer.Event.Stopped:
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
    public void MuteAudio(){
        AudioManager mAlramMAnager = (AudioManager)getSystemService(context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);
        } else {
            mAlramMAnager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_ALARM, true);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_RING, true);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }
    }

    public void UnMuteAudio(){
        AudioManager mAlramMAnager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE,0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0);
        } else {
            mAlramMAnager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_ALARM, false);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_RING, false);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
        }
    }
    private void SoundOnOff(){
        audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        if (audioManager.isStreamMute(AudioManager.STREAM_MUSIC)) {
            Toast.makeText(this, "Music is muted (isStreamMute)", Toast.LENGTH_SHORT).show();
        }
        if(soundonoff==1){
<<<<<<< HEAD
//            if (AudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
//                // 벨소리 모드일 경우
//                AudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
//            }
//            else if (AudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
//                // 진동 모드일 경우
//                AudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);    // 무음 모드로 변경
//            }
//            else if (AudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
//                // 무음 모드일 경우
//                AudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);    // 무음 모드로 변경
//            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!audioManager.isStreamMute(AudioManager.STREAM_SYSTEM)) {
                    savedStreamMuted = true;
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                    audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);
                    audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
                    audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
                    audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }
            } else {
                audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
            }

=======
            MuteAudio();
>>>>>>> 7cf3b82359f4f38f7ff968f960e71f50fbdaa459
            soundonoff=0;
            soundbtn.setImageDrawable(getResources().getDrawable(R.drawable.soundoff_icon));
        }
        else if(soundonoff==0){
<<<<<<< HEAD
//            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
//                // 벨소리 모드일 경우
//                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 1);    // 벨소리 모드로 변경
//            }
//            else if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
//                // 진동 모드일 경우
//                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 1);    // 벨소리 모드로 변경
//            }
//            else if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
//                // 무음 모드일 경우
//                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 1);    // 벨소리 모드로 변경
//            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (savedStreamMuted) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
                    audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0);
                    audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0);
                    audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0);
                    audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0);
                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    savedStreamMuted = false;
                }
            } else {
                // Note that this must be the same instance of audioManager that mutes
                // http://stackoverflow.com/questions/7908962/setstreammute-never-unmutes?rq=1
                audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
            }

=======
            UnMuteAudio();
>>>>>>> 7cf3b82359f4f38f7ff968f960e71f50fbdaa459
            soundonoff=1;
            soundbtn.setImageDrawable(getResources().getDrawable(R.drawable.soundon_icon));
        }
    }

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
                break;
        }
    }

    private void heartAni(){
        songLikeAnimButton.setVisibility(VISIBLE);
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 0.6f).setDuration(500);

        animator.addUpdateListener((ValueAnimator animation) -> {
                    float animatedValue = (Float) animation.getAnimatedValue();
                    songLikeAnimButton.setProgress(animatedValue);
                }
        );
        animator.start();
    }

    AWSListner awsListner = new AWSListner() {
        @Override
        public void downloadComplete() {
            super.downloadComplete();
            completeFile++;
            if(completeFile == 2){
                CL = new ArrayList<>();
                TL = new ArrayList<>();
                setLog();
                setUri();
            }
        }
    };

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
                nextTimeline = position+2;
                mMediaPlayer.setTime(TL.get(position+1).getTime());
                byTimeLine = true;
                int mediaPosition = (int) (mMediaPlayer.getPosition()*1000);
                seekBar.setProgress(mediaPosition);
                currTimeline.setText("현재 "+TL.get(position+1).getType()+"을(를) 판매 중입니다");
                alertDialog.dismiss();
            }
        );
        alertDialog.show();
    }

    public void btn_follow(){
        if(!is_follow){//팔로우 안한 상태에서 클릭하면
            FollowButton.setText("팔로우 취소");
        }
        else{//팔로우 한 상태에서 클릭하면
            FollowButton.setText("팔로우");
        }
        is_follow = !is_follow;//상태 바꿈
    }

}
