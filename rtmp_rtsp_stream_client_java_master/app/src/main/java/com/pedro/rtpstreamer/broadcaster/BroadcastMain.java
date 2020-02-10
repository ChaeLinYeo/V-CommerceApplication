package com.pedro.rtpstreamer.broadcaster;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;

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
import com.pedro.rtpstreamer.server.LocalfileManager;
import com.pedro.rtpstreamer.server.SendbirdConnection;
import com.pedro.rtpstreamer.server.SendbirdListner;
import com.pedro.rtpstreamer.utils.ExampleChatController;
import com.pedro.rtpstreamer.utils.PopupManager;
import com.pedro.rtpstreamer.utils.UnCatchTaskService;
import com.sendbird.android.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


@RequiresApi(api = Build.VERSION_CODES.P)
public class BroadcastMain extends AppCompatActivity
        implements View.OnClickListener, View.OnTouchListener, SurfaceHolder.Callback,
                    BroadcastListener, SendbirdListner.ForBroadcaster {

    Context context;
    private BroadcastManager broadcastManager = BroadcastManager.getInstance();
    private Button broadcastBtn;

    ////////////////////////////////////////////////
    //sendbird USER_ID
    private Random r = new Random();
    private int usernumber = r.nextInt(10000);
    private String USER_ID = "broadcaster_"+usernumber;

    //init title
    String init_t = null;

    // 로티 애니메이션뷰 선언
    LottieAnimationView songLikeAnimButton;

    //제목 수정 팝업용 변수
    TextView title_text;

    TextView heart;
    TextView people;

    //공지 수정 팝업용 변수
    TextView broadcast_notice;
    TextView system_notice;

    //이벤트 쿠폰 팝업용 변수
    public String e_n = "";
    public String e_a="";
    public int e_t_h=0;
    public int e_t_m=0;
    public int e_t_s=0;
    public int save_time_before = 0;
    NumberPicker numberpicker_h;
    NumberPicker numberpicker_m;
    NumberPicker numberpicker_s;
    public int save_time = 0;

    //카테고리용 변수
    ArrayList<String> category_items = new ArrayList<>();
    ArrayAdapter<String> adapter1;

    //examplechatcontroller
    ExampleChatController mExampleChatController;
    PopupManager PM;
    boolean canStart = true;

    ////////////////////////////////////////////////
    private SendbirdConnection sendbirdConnection;
    private LocalfileManager LM;
    private LocalfileManager LM_time;
    private LocalfileManager LM_subinfo;
    private int heart_final;
    private AlertDialog alertDialog;

    //갤러리에서 이미지 선택용 변수
    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        startService(new Intent(this, UnCatchTaskService.class));
        context = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.broadcast_main);

        setupBroadcast();

        ///////////////////////
        sendbirdConnection = SendbirdConnection.getInstance();
        sendbirdConnection.setupSendbird(this, USER_ID, 0);
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
        broadcast_notice =findViewById(R.id.broadcast_notice);
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

            case R.id.imgButton:
                //핸드폰 갤러리 열음
                Intent intent = new Intent();
                //백그라운드 서비스 실행
                //startService(intent);
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                break;

            case R.id.uriButton:
                broadcastManager.setTexture(2);
                break;

            case R.id.b_start_stop:
                if(canStart){
                    sendbirdConnection.getCtrl(this);
                }else{
                    broadcastManager.manageBroadcast(0);
                }
                break;

            case R.id.switchButton:
                broadcastManager.manageBroadcast(1);
                break;

            case R.id.categoryButton:
                sendbirdConnection.getAllCategory(PM);
                PM.btn_Category(getLayoutInflater(), LM_time);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));
                broadcastManager.setImage(bitmap);
                //broadcastManager.setTexture(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //For Broadcast info
    public View.OnClickListener broadcastClickListner = (View view) -> {
        switch(view.getId()){
            case R.id.titleText:
                btn_showDialog();
                break;

            case R.id.participant:
                btn_showPeople();
                break;

            case R.id.eye:
                btn_showPeople();
                break;

            case R.id.broadcast_notice:
                PM.btn_showDialog2(getLayoutInflater(), broadcast_notice);
                break;

            case R.id.custom_event:
                btn_editPopUp();
                break;

            case R.id.show_event:
                btn_showPopUp();
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
        sendbirdConnection.broadcastfinish();
        canStart = true;
        LM_subinfo.saveheartfinal(heart_final);
        AWSConnection.uploadFile(broadcastManager.getBroadcastName()+".txt", LM.getFileName(), this);
        AWSConnection.uploadFile(broadcastManager.getBroadcastName()+"_timeLine.txt", LM_time.getFileName(), this);
        AWSConnection.uploadFile(broadcastManager.getBroadcastName()+"_subinfo.txt", LM_subinfo.getFileName(), this);
        LM.LMEnd();
        LM_time.LMEnd();
        LM_subinfo.LMEnd();
        category_items.clear();
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
    }

    public void AlarmPlayer(String data){
        system_notice.setText(data);
    }
    public void NotiPlayer(String data){
        broadcast_notice.setText(data);
    }
    // 좋아요 로띠 애니메이션을 실행 시키는 메소드
    private boolean toggleSongLikeAnimButton(){
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
        return true;
    }

    //방송 시작 시 카테고리 설정하는 팝업창
    public void create_Category() {
        View mView_c = getLayoutInflater().inflate(R.layout.popup_category, null);

        // ArrayAdapter 생성. 아이템 View를 선택(multiple choice)가능하도록 만듦.
        adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, category_items) ;

        // listview 생성 및 adapter 지정.
        ListView listView = mView_c.findViewById(R.id.listView) ;
        listView.setAdapter(adapter1);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final AlertDialog.Builder alert05 = new AlertDialog.Builder(BroadcastMain.this);

        EditText editText = mView_c.findViewById(R.id.editText);
        Button btn_Add = mView_c.findViewById(R.id.btnAdd);
        Button btn_Del = mView_c.findViewById(R.id.btnDel);
        Button btn_Exit = mView_c.findViewById(R.id.btnExit);
        Button btn_Select = mView_c.findViewById(R.id.btnSelect);

        alert05.setView(mView_c);

        final AlertDialog alertDialog = alert05.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_Add.setOnClickListener((View view) -> {
                String text = editText.getText().toString();
                if(text.length()!=0){
                    category_items.add(text);
                    editText.setText("");
                    adapter1.notifyDataSetChanged();
                }
            }
        );

        btn_Del.setOnClickListener((View view) -> {
                int pos;
                pos = listView.getCheckedItemPosition();
                if(pos != ListView.INVALID_POSITION){
                    category_items.remove(pos);
                    listView.clearChoices();
                    adapter1.notifyDataSetChanged();
                }
            }
        );

        btn_Exit.setOnClickListener((View view) -> {
            for (String item : category_items){
                sendbirdConnection.addCategory(item);
            }
            adapter1.notifyDataSetChanged();
            alertDialog.dismiss();
        });

        btn_Select.setOnClickListener((View view) ->
            Toast.makeText(getApplicationContext(), "방송 시작 후 해당 상품을 판매할 때 눌러주세요.", Toast.LENGTH_LONG).show()
        );

        Toast.makeText(getApplicationContext(), "방송 시작 전, 판매할 상품의 카테고리를 기입해주세요.", Toast.LENGTH_LONG).show();
        alertDialog.show();
    }

    // 방송 시작 첫 제목 설정
    public void create_title() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(BroadcastMain.this);
        View mView = getLayoutInflater().inflate(R.layout.init_channel, null);
        LM_subinfo = new LocalfileManager(USER_ID+":"+System.currentTimeMillis()+":"+sendbirdConnection.getChannelNum()+"_subinfo.txt");

        LM_subinfo = new LocalfileManager(USER_ID+":"+System.currentTimeMillis()+":"+sendbirdConnection.getChannelNum()+"_subinfo.txt");

        final EditText newtitle = mView.findViewById(R.id.init_title);
        Button btn_cancel = mView.findViewById(R.id.init_cancel);
        Button btn_ok = mView.findViewById(R.id.init_ok);

        alert.setView(mView);

        alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_cancel.setOnClickListener((View lView) -> alertDialog.dismiss());

        btn_ok.setOnClickListener(
            (View view) -> {
                init_t = newtitle.getText().toString();
                sendbirdConnection.createChannel(init_t);
                title_text.setText(init_t);
                LM_subinfo.savetitle(init_t);
            }
        );
        Toast.makeText(getApplicationContext(), "방송 시작 전, 방송의 제목을 입력해주세요.", Toast.LENGTH_LONG).show();
        alertDialog.show();
    }

    //제목 수정 팝업창
    public void btn_showDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(BroadcastMain.this);
        View mView = getLayoutInflater().inflate(R.layout.custom_dialog, null);

        final EditText txt_inputText = mView.findViewById(R.id.init_title);
        Button btn_cancel = mView.findViewById(R.id.btn_cancel);
        Button btn_ok = mView.findViewById(R.id.btn_ok);

        alert.setView(mView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_cancel.setOnClickListener((View view) -> alertDialog.dismiss());

        btn_ok.setOnClickListener((View view) -> {
                init_t = txt_inputText.getText().toString();
                title_text.setText(init_t);
                sendbirdConnection.updateTitle(init_t);
                LM_subinfo.savetitle(init_t);
                alertDialog.dismiss();
            }
        );
        alertDialog.show();
    }

    //쿠폰 이벤트 설정 팝업창
    public void btn_editPopUp() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(BroadcastMain.this);
        View mView = getLayoutInflater().inflate(R.layout.popup_custom_dialog, null);

        final EditText txt_coupon_name = mView.findViewById(R.id.blabla01);
        final EditText txt_coupon_ect = mView.findViewById(R.id.blabla02);
        Button coupon_btn_ok_02 = mView.findViewById(R.id.coupon_btn_ok_02);
        Button coupon_btn_cancel_02 = mView.findViewById(R.id.coupon_btn_cancel_02);

        numberpicker_h = mView.findViewById(R.id.hour);
        numberpicker_m = mView.findViewById(R.id.minute);
        numberpicker_s = mView.findViewById(R.id.seconds);

        alert.setView(mView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        //시
        numberpicker_h.setMinValue(0);
        numberpicker_h.setMaxValue(23);
        numberpicker_h.setValue(0);
        //분
        numberpicker_m.setMinValue(0);
        numberpicker_m.setMaxValue(59);
        numberpicker_m.setValue(0);
        //초
        numberpicker_s.setMinValue(0);
        numberpicker_s.setMaxValue(59);
        numberpicker_s.setValue(0);

        coupon_btn_cancel_02.setOnClickListener((View view) -> alertDialog.dismiss());

        coupon_btn_ok_02.setOnClickListener((View view) -> {
                e_n=txt_coupon_name.getText().toString();
                e_a=txt_coupon_ect.getText().toString();
                e_t_h = numberpicker_h.getValue();
                e_t_m = numberpicker_m.getValue();
                e_t_s = numberpicker_s.getValue();
                alertDialog.dismiss();
            }
        );

        alertDialog.show();
    }

    //쿠폰 이벤트 생성 팝업창
    public void btn_showPopUp (){
        final AlertDialog.Builder alert01 = new AlertDialog.Builder(BroadcastMain.this);
        View mView01 = getLayoutInflater().inflate(R.layout.popup_coupon, null);
        final EditText coupon_name_txt = mView01.findViewById(R.id.blabla011);
        final EditText coupon_ect_txt = mView01.findViewById(R.id.blabla022);
        final EditText coupon_time_txt = mView01.findViewById(R.id.blabla033);  //n초뒤 사라짐 이라고 띄우는 부분
        Button coupon_btn_cancel_01 = mView01.findViewById(R.id.coupon_btn_cancel_01);
        Button coupon_btn_ok_01 = mView01.findViewById(R.id.coupon_btn_ok_01);

        String result  = "cn="+e_n+"\nci="+e_a+"\nTimeLimit="+e_t_h+":"+e_t_m+":"+e_t_s;
        sendbirdConnection.sendUserMessage(result, "event");

        coupon_name_txt.setText(e_n);
        coupon_ect_txt.setText(e_a);

        //초*1000
        //분*1000*60
        //시*1000*60*60
        save_time_before = (e_t_h*1000*60*60) + (e_t_m*1000*60) + (e_t_s*1000); //시간 int로 저장
        save_time = e_t_h + e_t_m + e_t_s;
        alert01.setView(mView01);

        final AlertDialog alertDialog = alert01.create();
        alertDialog.setCanceledOnTouchOutside(false);

        Thread thread = new Thread(() -> {
                String timeText= save_time + "초 뒤 사라짐";
                coupon_time_txt.setText(timeText);
                // n초가 지나면 다이얼로그 닫기
                TimerTask task = new TimerTask(){
                    @Override
                    public void run() {
                        alertDialog.dismiss();
                    }
                };

                Timer timer = new Timer();
                timer.schedule(task, save_time_before);
            }
        );
        thread.start();

        coupon_btn_cancel_01.setOnClickListener((View view) -> alertDialog.dismiss());

        coupon_btn_ok_01.setOnClickListener((View view) -> alertDialog.dismiss());
        e_n = null; e_a = null; e_t_h = 0; e_t_m = 0; e_t_s = 0;
        alertDialog.show();
    }


    //시청자 목록 보는 팝업창
    public void btn_showPeople() {
        View mView = getLayoutInflater().inflate(R.layout.popup_people, null);
        mView.findViewById(R.id.custom_event).setOnClickListener(broadcastClickListner);
        mView.findViewById(R.id.show_event).setOnClickListener(broadcastClickListner);
        //User만을 담은 유저리스트 생성
        List<User> userList = sendbirdConnection.getUserList(true);

        //리스트뷰에 보여주기 위한 리스트 생성
        ArrayList<String> ShowList = new ArrayList<>();
        for(User user : userList){
            ShowList.add(user.getUserId() + "(" + user.getNickname() + ")");
        }
        // listview 생성 및 adapter 지정.
        final ListView listview = mView.findViewById(R.id.listview1);
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, ShowList) ;

        listview.setAdapter(adapter) ;

        final AlertDialog.Builder alert04 = new AlertDialog.Builder(BroadcastMain.this);

        Button btn_cancel = mView.findViewById(R.id.popup_cancel);
        Button selectAllButton = mView.findViewById(R.id.select_all);
        Button ban = mView.findViewById(R.id.ben);
        EditText search = mView.findViewById(R.id.searchPeople);
        SwipeRefreshLayout mSwipeRefreshLayout = mView.findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
                mSwipeRefreshLayout.setRefreshing(true);
                new Handler().postDelayed(() -> {
                        List<User> reuserList = sendbirdConnection.getUserList(true);
                        for(User user : reuserList){
                            ShowList.add(user.getUserId() + "(" + user.getNickname() + ")");
                        }
                        listview.setAdapter(adapter);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                ,500);
            }
        );

        //색상지정
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        alert04.setView(mView);

        final AlertDialog alertDialog = alert04.create();
        alertDialog.setCanceledOnTouchOutside(false);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {  }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = search.getText().toString();
                searching(text, adapter);
            }
        });

        btn_cancel.setOnClickListener((View view) -> alertDialog.dismiss());

        //사용자 벤
        ban.setOnClickListener((View v) -> {
                SparseBooleanArray checkedItems = listview.getCheckedItemPositions();
                int count = adapter.getCount() ;
                for (int i = count-1; i >= 0; i--) {
                    if (checkedItems.get(i)) {
                        sendbirdConnection.banUser(i);
                    }
                }
                // 모든 선택 상태 초기화.
                listview.clearChoices() ;
                adapter.notifyDataSetChanged();
            }
        );

        //"Select All" Button 클릭 시 모든 아이템 선택.
        selectAllButton.setOnClickListener((View v) -> {
                int count = adapter.getCount() ;
                for (int i=0; i<count; i++) {
                    listview.setItemChecked(i, true) ;
                }
            }
        );

        alertDialog.show();
    }

    public void searching(String charText, ArrayAdapter A) {
        List<String> searchlist = new ArrayList<>(); //????
        List<String> alllist = new ArrayList<>();
        ///////////////////////////////////////////////
        List<User> userList = sendbirdConnection.getUserList(false);
        ///////////////////////////////////////////////
        for(User user : userList){
            alllist.add(user.getUserId() + "(" + user.getNickname() + ")");
        }
        // 문자 입력시마다 리스트를 지우고 새로 뿌려준다.
        searchlist.clear();

        // 문자 입력이 없을때는 모든 데이터를 보여준다.
        if (charText.length() == 0) {
            searchlist.addAll(alllist);
        }else {
            for(int i = 0;i < alllist.size(); i++){
                if (alllist.get(i).toLowerCase().contains(charText)) {
                    // 검색된 데이터를 리스트에 추가한다.
                    searchlist.add(alllist.get(i));
                }
            }
        }
        // 리스트 데이터가 변경되었으므로 아답터를 갱신하여 검색된 데이터를 화면에 보여준다.
        A.notifyDataSetChanged();
    }

//    @Override
//    public void getCate(){
//        PM.btn_Category(getLayoutInflater(), sendbirdConnection, LM_time);
//    }
    @Override
    public void setText(){
        PM.btn_Text(getLayoutInflater(), broadcastManager);
    }

    @Override
    public void setNoti(){
        PM.btn_showDialog2(getLayoutInflater(),broadcast_notice);
    }

    @Override
    public void channelFounded(boolean possible){
        if(possible){
            create_title();

        } else{
            Toast.makeText(getApplicationContext(), "모든 방송 채널이 사용중입니다.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void channelCreateComplete(){
        alertDialog.dismiss();
        canStart = false;
        broadcastManager.setBroadcastChannel(sendbirdConnection.getChannelNum());
        broadcastManager.manageBroadcast(0);
        LM = new LocalfileManager(USER_ID+":"+System.currentTimeMillis()+":"+sendbirdConnection.getChannelNum()+".txt");
        LM_time = new LocalfileManager(USER_ID+":"+System.currentTimeMillis()+":"+sendbirdConnection.getChannelNum()+"_timeline.txt");
        Log.d("channel complete",""+sendbirdConnection.getChannelNum());
        create_Category();
    }

    @Override
    public void getUserListComplete(String peopleNum){
        people.setText(peopleNum);
    }

    @Override
    public void messageReceived(String customType, String data){
        switch(customType) {
            case "alarm":
                AlarmPlayer(data);
                LM.savealarm(data);
                break;

            case "chat" :
                mExampleChatController.add(data);
                LM.savechat(data);
                break;

            case "like":
                LM.saveheart();
                break;
        }
    }

    @Override
    public void metaCounterUpdated(int heart){
        LikePlayer(heart);
    }
}
