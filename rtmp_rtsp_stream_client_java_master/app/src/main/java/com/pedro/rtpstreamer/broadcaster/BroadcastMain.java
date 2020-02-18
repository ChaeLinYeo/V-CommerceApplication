package com.pedro.rtpstreamer.broadcaster;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import com.airbnb.lottie.LottieAnimationView;
import com.pedro.rtplibrary.view.OpenGlView;
import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.server.AWSConnection;
import com.pedro.rtpstreamer.server.AWSListner;
import com.pedro.rtpstreamer.server.LocalfileManager;
import com.pedro.rtpstreamer.server.SendbirdConnection;
import com.pedro.rtpstreamer.server.SendbirdListner;
import com.pedro.rtpstreamer.utils.ExampleChatController;
import com.pedro.rtpstreamer.utils.PopupManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.TedBottomSheetDialogFragment;


@RequiresApi(api = Build.VERSION_CODES.P)
public class BroadcastMain extends AppCompatActivity
        implements View.OnClickListener, View.OnTouchListener, SurfaceHolder.Callback,
                    BroadcastListener{

    Context context;
    private BroadcastManager broadcastManager = BroadcastManager.getInstance();
    private Button broadcastBtn;

    // 로티 애니메이션뷰 선언
    LottieAnimationView songLikeAnimButton;

    //제목 수정 팝업용 변수
    TextView title_text;
    TextView heart;
    TextView people;
    TextView broadcast_notice;
    TextView system_notice;

    //examplechatcontroller
    ExampleChatController mExampleChatController;
    PopupManager PM;
    boolean canStart = true;
    long systemtime;

    private LocalfileManager LM;
    private LocalfileManager LM_time;
    private int heart_final;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        startService(new Intent(this, UnCatchTaskService.class));
        context = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.broadcast_main);

        setupBroadcast();

        ///////////////////////
        SendbirdConnection.setupSendbird(this, true, sendbirdListner);

        AWSConnection.setAwsListner(awsListner);
        ///////////////////////

        mExampleChatController = new ExampleChatController(this, findViewById(R.id.ChatListView), R.layout.chatline, R.id.chat_line_textview, R.id.chat_line_timeview);
        mExampleChatController.show();
        PM = new PopupManager(context);
        // 로티 애니메이션뷰 리소스 아이디연결
        songLikeAnimButton = findViewById(R.id.button_song_like_animation);

        //제목 수정 팝업 후 적용됨
        title_text = findViewById(R.id.titleText);
        title_text.setOnClickListener(broadcastClickListner);
        system_notice = findViewById(R.id.system_notice);

        //공지 수정 팝업 후 적용됨
        broadcast_notice = findViewById(R.id.broadcast_notice);
        broadcast_notice.setOnClickListener(broadcastClickListner);

        //좋아요 개수
        heart = findViewById(R.id.heart_num);

        //시청인원
        people = findViewById(R.id.participant);
        people.setOnClickListener(broadcastClickListner);

        findViewById(R.id.categoryButton).setOnClickListener(this);
    }

    //setUI for video
    public void setupBroadcast(){
        OpenGlView openGlView;
        openGlView = findViewById(R.id.backGroundVideo);
        openGlView.getHolder().addCallback(this);
        openGlView.setOnTouchListener(this);
        openGlView.ogvInit();
        broadcastManager.setBroadcastManager(context, getResources(), openGlView);

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

            //이미지 띄우기 버튼
            case R.id.imgButton:
                if(!broadcastManager.isImage()) {
                    TedBottomPicker.with(BroadcastMain.this)
                            .setPeekHeight(1600)
                            .showTitle(false)
                            .setTitle("이미지 선택")
                            .setCompleteButtonText("Done")
                            .setEmptySelectionText("선택된 사진이 없습니다.")
                            .setSelectMaxCount(1)
                            .setSelectMinCount(0)
                            .showCameraTile(false)
                            .showGalleryTile(false)
                            .showMultiImage(new TedBottomSheetDialogFragment.OnMultiImageSelectedListener() {
                                @Override
                                public void onImagesSelected(List<Uri> uriList) {
                                    try {
                                        if(uriList.size()!= 0) {
                                            Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), uriList.get(0));
                                            broadcastManager.setImage(bm);
                                        }
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                } else{
                    broadcastManager.setTexture(1);
                }
                break;

            //동영상 띄우는 버튼
            case R.id.uriButton:
                if(!broadcastManager.isUri()) {
                    TedBottomPicker.with(BroadcastMain.this)
                            .setPeekHeight(1600)
                            .showTitle(false)
                            .setTitle("동영상 선택")
                            .setCompleteButtonText("Done")
                            .setEmptySelectionText("선택된 동영상이 없습니다.")
                            .setSelectMaxCount(1)
                            .setSelectMinCount(0)
                            .showCameraTile(false)
                            .showGalleryTile(false)
                            .showVideoMedia()
                            .showMultiImage(new TedBottomSheetDialogFragment.OnMultiImageSelectedListener() {
                                @Override
                                public void onImagesSelected(List<Uri> uriList) {
                                    if(uriList.size() != 0){
                                        broadcastManager.setUri(uriList.get(0));
                                    }
                                }
                            });
                } else{
                    broadcastManager.setTexture(2);
                }
                break;

            case R.id.b_start_stop:
                if(canStart){
                    SendbirdConnection.getCtrl();
                }else{
                    broadcastManager.manageBroadcast(0);
                }
                break;

            case R.id.switchButton:
                broadcastManager.manageBroadcast(1);
                break;

            case R.id.categoryButton:
                PM.btn_Category(getLayoutInflater(), LM_time,  systemtime);
                break;
        }
    }

    //For Broadcast info
    public View.OnClickListener broadcastClickListner = (View view) -> {
        switch(view.getId()){
            case R.id.titleText:
                PM.btn_showDialog(getLayoutInflater(), LM, systemtime, title_text);
                break;

            case R.id.participant:
                PM.btn_showPeople(getLayoutInflater());
                break;

            case R.id.eye:
                PM.btn_showPeople(getLayoutInflater());
                break;

            case R.id.broadcast_notice:
                PM.btn_showDialog2(getLayoutInflater(), broadcast_notice);
                break;

        }
    };

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
        systemtime = System.currentTimeMillis();
    }

    @Override
    public void broadcastStop(){
        Log.d("PKR","broadcast stop");
        broadcastBtn.setText(R.string.start_button);
        SendbirdConnection.broadcastfinish();
        canStart = true;
        LM.savefinal(systemtime,Integer.toString(heart_final), "heart");
        LM.savefinal(systemtime,Integer.toString(SendbirdConnection.getViewNum()),"count");
        LM.savefinal(systemtime,SendbirdConnection.getUserId(),"user_id");
        LM.LMEnd();
        LM_time.LMEnd();
        AWSConnection.uploadFile(broadcastManager.getBroadcastName()+".txt", LM.getFileName());
        AWSConnection.uploadFile(broadcastManager.getBroadcastName()+"_timeLine.txt", LM_time.getFileName());
        PM.clearCategoryI();
        PM.clearSCategory();
    }

    @Override
    public void setToast(String message){
        runOnUiThread(
            () -> Toast.makeText(BroadcastMain.this, message, Toast.LENGTH_SHORT).show()
        );
    }

    /////////////////////////////////////////////////////////////
    public void LikePlayer(int num){
        if(toggleSongLikeAnimButton())  {
            heart_final = num;
            heart.setText(Integer.toString(num));
        }
        if(num % 100 == 0)
            AlarmPlayer(num + "회 돌파~", 2);
    }

    public void AlarmPlayer(String data, int type) {
        switch (type) {
            case 1:
                system_notice.setText(data);
                system_notice.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_bright));
                break;
            case 2:
                system_notice.setText(data);
                system_notice.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_light));
                break;
            case 3:
                system_notice.setText(data);
                system_notice.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));
                break;
        }
    }

    // 좋아요 로띠 애니메이션을 실행 시키는 메소드
    private boolean toggleSongLikeAnimButton(){
        songLikeAnimButton.setVisibility(View.VISIBLE);
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 0.6f).setDuration(500);

        animator.addUpdateListener((ValueAnimator animation) -> {
                float animatedValue = (Float) animation.getAnimatedValue();
                songLikeAnimButton.setProgress(animatedValue);
            }
        );
        animator.start();
        return true;
    }

    @Override
    public void setText(){
        PM.btn_Text(getLayoutInflater(), broadcastManager);
    }

    private SendbirdListner sendbirdListner = new SendbirdListner() {
        @Override
        public void getCtrlComplete() {
            super.getCtrlComplete();
            SendbirdConnection.getBroadcastChannel();
        }
        @Override
        public void updateUser(int inout){
            int current = Integer.parseInt(people.getText().toString());
            people.setText(Integer.toString(current+inout));
        }
        @Override
        public void userenter(String enterduser) {
            super.userenter(enterduser);
            AlarmPlayer(enterduser + "님이 들어오셨습니다.", 1);
        }

        @Override
        public void getChannelComplete(boolean success) {
            super.getChannelComplete(success);
            if(success){
                LM = new LocalfileManager(SendbirdConnection.getUserId()+":"+systemtime+":"+SendbirdConnection.getBroadcastChannelNum()+".txt");
                PM.create_title(getLayoutInflater(), title_text, LM);
            } else{
                Toast.makeText(getApplicationContext(), "모든 방송 채널이 사용중입니다.", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void messageReceived(String customType, String data, long messagetime) {
            super.messageReceived(customType, data, messagetime);
            long time = messagetime - systemtime;
            switch(customType) {
                case "alarm":
                    AlarmPlayer(data, 3);
                    break;

                case "chat" :
                    mExampleChatController.add(data);
                    LM.savechat(time, data);
                    break;

                case "like":
                    LM.saveheart(time);
                    break;
            }
        }

        @Override
        public void metaCounterUpdated(int heart) {
            super.metaCounterUpdated(heart);
            LikePlayer(heart);
        }

        @Override
        public void channelCreateComplete() {
            super.channelCreateComplete();
            PM.PopupEnd();
            canStart = false;
            broadcastManager.setBroadcastChannel(SendbirdConnection.getBroadcastChannelNum());
            broadcastManager.manageBroadcast(0);
            LM_time = new LocalfileManager(SendbirdConnection.getUserId()+":"+systemtime+":"+SendbirdConnection.getBroadcastChannelNum()+"_timeline.txt");
            LM_time.savetimeline(0, "null\n");
            Log.d("channel complete",""+SendbirdConnection.getBroadcastChannelNum());
            PM.create_Category(getLayoutInflater());
        }

        @Override
        public void getUserListComplete(String peopleNum) {
            super.getUserListComplete(peopleNum);
            people.setText(peopleNum);
        }
    };

    int uploaded = 0;

    private AWSListner awsListner = new AWSListner() {
        @Override
        public void startUpload(){
            findViewById(R.id.BroadcastLoadingPanel).setVisibility(View.VISIBLE);
            uploaded = 0;
        }

        @Override
        public void uploadComplete(boolean success) {
            super.uploadComplete(success);
            if(success) {
                uploaded++;
                if(uploaded==3) {
                    findViewById(R.id.BroadcastLoadingPanel).setVisibility(View.GONE);
                    Toast.makeText(BroadcastMain.this, "정상적으로 저장되었습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                findViewById(R.id.BroadcastLoadingPanel).setVisibility(View.GONE);
                Toast.makeText(BroadcastMain.this, "저장에 실패하였습니다. \n관리자에게 문의해주세요", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
