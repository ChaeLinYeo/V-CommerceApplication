package com.pedro.rtpstreamer.broadcaster;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

/*import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.ResultListener;
import com.amplifyframework.storage.result.StorageUploadFileResult;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;*/
import com.airbnb.lottie.LottieAnimationView;
import com.pedro.rtplibrary.view.OpenGlView;
import com.pedro.rtpstreamer.R;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;
import com.sendbird.android.UserMessage;

/*import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;*/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BroadcastMain extends AppCompatActivity
        implements View.OnClickListener, View.OnTouchListener, SurfaceHolder.Callback, BroadcastListener{

    Context context;
    private BroadcastManager broadcastManager;

    private Button broadcastBtn;

    //sendbird APP ID
    private String APP_ID = "2651701A-6EE0-4519-A94D-F2286E7AAB01";
    // for sendbird connect
    private String USER_ID = "broadcaster";
    String CHANNEL_URL;
    //String MAIN_URL = "sendbird_Ctrl";
    OpenChannel mOpenChannel;
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHAT";
    //private static final String MAIN_CHANNEL_HANDLER_ID = "MAIN_CHANNEL_HANDLER";

    //init title
    String init_t = null;
    // 송출자이므로 항상 방송 operator!
    List<User> operator = new ArrayList<>();

    // 로티 애니메이션뷰 선언
    LottieAnimationView songLikeAnimButton;

    //제목 수정 팝업용 변수
    TextView txt_dummy;

    //이벤트 쿠폰 팝업용 변수
    TextView coupon_name_dummy;
    TextView coupon_ect_dummy;
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

    //공지 수정 팝업용 변수
    TextView txt_dummy2;
    TextView heart;
    TextView people;
    TextView alarm;

    //카테고리용 변수
    ArrayList<String> category_items = new ArrayList<String>();
    ArrayAdapter<String> adapter1;

    //examplechatcontroller
    ExampleChatController mExampleChatController;
    boolean canStart = true;

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

        SendBird.init(APP_ID, context);
        connect();
        mExampleChatController = new ExampleChatController(this, (ListView) findViewById(R.id.ChatListView), R.layout.chatline, R.id.chat_line_textview, R.id.chat_line_timeview);
        mExampleChatController.show();

        // 로티 애니메이션뷰 리소스 아이디연결
        songLikeAnimButton = (LottieAnimationView)findViewById(R.id.button_song_like_animation);

        //제목 수정 팝업 후 적용됨
        txt_dummy=(TextView)findViewById(R.id.txt_dummytext);
        alarm = (TextView)findViewById(R.id.txt_dummytext3);
        //이벤트 팝업용
        coupon_name_dummy=(TextView)findViewById(R.id.blabla01);
        coupon_ect_dummy=(TextView)findViewById(R.id.blabla02);

        //공지 수정 팝업 후 적용됨
        txt_dummy2=(TextView)findViewById(R.id.txt_dummytext2);

        //좋아요 개수
        heart = (TextView)findViewById(R.id.heart_num);

        //시청인원
        people = (TextView)findViewById(R.id.participant);;
    }
    private void connect() {
        SendBird.connect(USER_ID, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (e != null) {    // Error.
                    Log.d("connect erro","connect : 1" );
                    return;
                }
                operator.add(user);
                updateCurrentUserInfo(USER_ID);
                //updateCurrentUserPushToken();
            }
        });
    }

    private void updateCurrentUserInfo(final String userNickname) {
        SendBird.updateCurrentUserInfo(userNickname, null, new SendBird.UserInfoUpdateHandler() {
            @Override
            public void onUpdated(SendBirdException e) {
                if (e != null) {
                    // Error!
                    Log.e("nickname",e.getMessage()+" : "+e.getCode());
                    return;
                }
                //PreferenceUtils.setNickname(userNickname);
            }
        });
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
                if(canStart){
                    create_title(view);
                    if(!canStart){
                        people.setText(Integer.toString(getParticipantnum()));
                    }
                }else{
                    broadcastfinish();
                }
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

    public void LikePlayer(int num){
        toggleSongLikeAnimButton();
        heart.setText(num);
    }
    public void AlarmPlayer(String data){
        alarm.setText(data);
    }

    // 좋아요 로띠 애니메이션을 실행 시키는 메소드
    private boolean toggleSongLikeAnimButton(){
        // 애니메이션을 한번 실행시킨다.
        // Custom animation speed or duration.
        // ofFloat(시작 시간, 종료 시간).setDuration(지속시간)
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 0.6f).setDuration(500);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                songLikeAnimButton.setProgress((Float) animation.getAnimatedValue());
            }
        });
        animator.start();

        return true;

    }


    /****** sendbird ******/
    public void broadcastfinish(){
        //방송 종료시 방송 채널 삭제, not yet: control채널에게 방송 채널이 비었음을 알림
        mOpenChannel.delete(new OpenChannel.OpenChannelDeleteHandler() {
            @Override
            public void onResult(SendBirdException e) {
            }
        });
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        canStart = true;
        category_items.clear();
    }
    //앱을 종료시 센드버드에서 로그아웃 시켜주는 메소드 아직은 사용 X
    private void disconnect() {
        SendBird.unregisterPushTokenAllForCurrentUser(new SendBird.UnregisterPushTokenHandler() {
            @Override
            public void onUnregistered(SendBirdException e) {
                if (e != null) {
                    // Error!
                    Log.d(" ","onunregister");
                    e.printStackTrace();
                    // Don't return because we still need to disconnect.
                } else {
//                    Toast.makeText(MainActivity.this, "All push tokens unregistered.", Toast.LENGTH_SHORT).show();
                }
                ConnectionManager.logout(new SendBird.DisconnectHandler() {
                    @Override
                    public void onDisconnected() {
                        try {
                            PreferenceUtils.setConnected(false);
                        }catch (Exception e) {
                            Log.d("logout", "");
                            e.printStackTrace();
                        }
                        Log.d("","connect : onDisconnected : " );
                    }
                });
            }
        });
    }

    private void sendUserMessage(String text, String type) {
        if(mOpenChannel == null) Log.d("sendU", "channel is null");
        mOpenChannel.sendUserMessage(text, text, type, new BaseChannel.SendUserMessageHandler() {
            @Override
            public void onSent(UserMessage userMessage, SendBirdException e) {
                if (e != null) {
                    // Error!
                    Toast.makeText(context, "Send failed with error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
    private void getChannel(String channelUrl){
        OpenChannel.getChannel(channelUrl, new OpenChannel.OpenChannelGetHandler() {
            @Override
            public void onResult(OpenChannel openChannel, SendBirdException e) {
                if (e != null) {    // Error.
                    Log.d("getchannelerror",e.getMessage());
                    e.printStackTrace();
                    return;
                }
                openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
                    @Override
                    public void onResult(SendBirdException e) {
                        if (e != null) {    // Error.
                            Log.d("entererror","");
                            e.printStackTrace();
                            return;
                        }
                    }
                });
            }
        });
    }
    public int getParticipantnum(){
        UserListQuery userListQuery = mOpenChannel.createParticipantListQuery();
        userListQuery.next(new UserListQuery.UserListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }
                UserList = setUserList(list);
            }
        });
        return UserList.size();
    }
    /*******end of sendbird ********/

    //방송 시작 시 카테고리 설정하는 팝업창
    public void create_Category(View view) {
        View mView_c = getLayoutInflater().inflate(R.layout.popup_category, null);

        // ArrayAdapter 생성. 아이템 View를 선택(multiple choice)가능하도록 만듦.
        adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, category_items) ;

        // listview 생성 및 adapter 지정.
        ListView listView = (ListView)mView_c.findViewById(R.id.listView) ;
        listView.setAdapter(adapter1);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final AlertDialog.Builder alert05 = new AlertDialog.Builder(BroadcastMain.this);

        EditText editText = (EditText)mView_c.findViewById(R.id.editText);
        Button btn_Add = (Button)mView_c.findViewById(R.id.btnAdd);
        Button btn_Del = (Button)mView_c.findViewById(R.id.btnDel);
        Button btn_Exit = (Button)mView_c.findViewById(R.id.btnExit);
        Button btn_Select = (Button)mView_c.findViewById(R.id.btnSelect);

        alert05.setView(mView_c);

        final AlertDialog alertDialog = alert05.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_Add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String text = editText.getText().toString();
                if(text.length()!=0){
                    category_items.add(text);
                    editText.setText("");
                    sendUserMessage("add:"+text,"category");
                    adapter1.notifyDataSetChanged();
                }
            }
        });

        btn_Del.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                int pos;
                pos = listView.getCheckedItemPosition();
                if(pos != ListView.INVALID_POSITION){
                    sendUserMessage("delete:"+category_items.get(pos), "category");
                    //Log.d("delete category", category_items.get(pos));
                    category_items.remove(pos);
                    listView.clearChoices();
                    adapter1.notifyDataSetChanged();
                }
            }
        });

        btn_Exit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                adapter1.notifyDataSetChanged();
                alertDialog.dismiss();
            }
        });

        btn_Select.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "방송 시작 후 해당 상품을 판매할 때 눌러주세요.", Toast.LENGTH_LONG).show();
            }
        });

        Toast.makeText(getApplicationContext(), "방송 시작 전, 판매할 상품의 카테고리를 기입해주세요.", Toast.LENGTH_LONG).show();
        alertDialog.show();
    }

    // 방송 시작 첫 제목 설정
    public void create_title(View view) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(BroadcastMain.this);
        View mView = getLayoutInflater().inflate(R.layout.init_channel, null);

        final EditText newtitle = (EditText)mView.findViewById(R.id.init_title);
        Button btn_cancel = (Button)mView.findViewById(R.id.init_cancel);
        Button btn_ok = (Button)mView.findViewById(R.id.init_ok);

        alert.setView(mView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_cancel.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                init_t = newtitle.getText().toString();
                if (init_t != null) {
                    OpenChannel.createChannel(init_t, null, null, null, operator, new OpenChannel.OpenChannelCreateHandler() {
                        @Override
                        public void onResult(OpenChannel openChannel, SendBirdException e) {
                            if (e != null) {  return;  }
                            //방송 생성시에만 방송 url받아서 들어가기 위한 준비.
                            CHANNEL_URL = openChannel.getUrl();
                            mOpenChannel = openChannel;
                            HashMap<String, Integer> map = new HashMap<String, Integer>();
                            map.put("heart", 0);
                            openChannel.createMetaCounters(map, new BaseChannel.MetaCounterHandler() {
                                @Override
                                public void onResult(Map<String, Integer> map, SendBirdException e) {
                                    if( e!= null) {Log.e("counter error ", e.getMessage()); return;}
                                    Log.d("counter", "success, current url : "+ CHANNEL_URL);
                                }
                            });
                            getChannel(CHANNEL_URL);
                        }
                    });
                    SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
                        @Override
                        public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                            mExampleChatController.msgfilter(baseChannel,baseMessage);
                        }
                    });
                    txt_dummy.setText(newtitle.getText().toString());
                    alertDialog.dismiss();
                    canStart = false;
                    create_Category(view);
                }
            }
        });
        Toast.makeText(getApplicationContext(), "방송 시작 전, 방송의 제목을 입력해주세요.", Toast.LENGTH_LONG).show();
        alertDialog.show();
    }

    //제목 수정 팝업창
    public void btn_showDialog(View view) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(BroadcastMain.this);
        View mView = getLayoutInflater().inflate(R.layout.custom_dialog, null);

        final EditText txt_inputText = (EditText)mView.findViewById(R.id.init_title);
        Button btn_cancel = (Button)mView.findViewById(R.id.btn_cancel);
        Button btn_ok = (Button)mView.findViewById(R.id.btn_ok);

        alert.setView(mView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_cancel.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                init_t = txt_inputText.getText().toString();
                txt_dummy.setText(txt_inputText.getText().toString());
                String coverUrl = mOpenChannel.getCoverUrl();
                mOpenChannel.updateChannel(init_t, coverUrl, "null", new OpenChannel.OpenChannelUpdateHandler() {
                    @Override
                    public void onResult(OpenChannel openChannel, SendBirdException e) {
                        if(e != null) {}
                    }
                });
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    //쿠폰 이벤트 설정 팝업창
    public void btn_editPopUp(View view) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(BroadcastMain.this);
        View mView = getLayoutInflater().inflate(R.layout.popup_custom_dialog, null);

        final EditText txt_coupon_name = (EditText)mView.findViewById(R.id.blabla01);
        final EditText txt_coupon_ect = (EditText)mView.findViewById(R.id.blabla02);
        Button coupon_btn_ok_02 = (Button)mView.findViewById(R.id.coupon_btn_ok_02);
        Button coupon_btn_cancel_02 = (Button)mView.findViewById(R.id.coupon_btn_cancel_02);

        numberpicker_h = (NumberPicker)mView.findViewById(R.id.hour);
        numberpicker_m = (NumberPicker)mView.findViewById(R.id.minute);
        numberpicker_s = (NumberPicker)mView.findViewById(R.id.seconds);

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

        coupon_btn_cancel_02.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        coupon_btn_ok_02.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                e_n=txt_coupon_name.getText().toString();
                e_a=txt_coupon_ect.getText().toString();
                e_t_h = numberpicker_h.getValue();
                e_t_m = numberpicker_m.getValue();
                e_t_s = numberpicker_s.getValue();
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    //쿠폰 이벤트 생성 팝업창
    public void btn_showPopUp (View view){
        final AlertDialog.Builder alert01 = new AlertDialog.Builder(BroadcastMain.this);
        View mView01 = getLayoutInflater().inflate(R.layout.popup_coupon, null);
        final EditText coupon_name_txt = (EditText)mView01.findViewById(R.id.blabla011);
        final EditText coupon_ect_txt = (EditText)mView01.findViewById(R.id.blabla022);
        final EditText coupon_time_txt = (EditText)mView01.findViewById(R.id.blabla033);  //n초뒤 사라짐 이라고 띄우는 부분
        Button coupon_btn_cancel_01 = (Button)mView01.findViewById(R.id.coupon_btn_cancel_01);
        Button coupon_btn_ok_01 = (Button)mView01.findViewById(R.id.coupon_btn_ok_01);

        String result  = "cn="+e_n+"ci="+e_a+"\nTimeLimit="+e_t_h+":"+e_t_m+":"+e_t_s;
        sendUserMessage(result, "event");

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

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                coupon_time_txt.setText(save_time + "초 뒤 사라짐");
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
        });
        thread.start();

        coupon_btn_cancel_01.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        coupon_btn_ok_01.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        e_n = null; e_a = null; e_t_h = 0; e_t_m = 0; e_t_s = 0;
        alertDialog.show();
    }

    //공지 수정 팝업창
    public void btn_showDialog2(View view) {
        final AlertDialog.Builder alert03 = new AlertDialog.Builder(BroadcastMain.this);
        View mView = getLayoutInflater().inflate(R.layout.notification_custom_dialog, null);

        final EditText txt_inputText2 = (EditText)mView.findViewById(R.id.txt_input2);
        Button btn_cancel2 = (Button)mView.findViewById(R.id.btn_cancel2);
        Button btn_ok2 = (Button)mView.findViewById(R.id.btn_ok2);

        alert03.setView(mView);

        final AlertDialog alertDialog = alert03.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_cancel2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        btn_ok2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                txt_dummy2.setText(txt_inputText2.getText().toString());
                sendUserMessage(txt_inputText2.getText().toString(), "Setting");
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    List<User> UserList = new ArrayList<>();
    //시청자 목록 보는 팝업창
    public void btn_showPeople(View view) {
        View mView = getLayoutInflater().inflate(R.layout.popup_people, null);
        //User만을 담은 유저리스트 생성
        getUserList();
        people.setText(Integer.toString(getParticipantnum()));
        //리스트뷰에 보여주기 위한 리스트 생성
        ArrayList<String> ShowList = new ArrayList<>();
        for(User user : UserList){
            ShowList.add(user.getUserId() + "(" + user.getNickname() + ")");
        }
        // listview 생성 및 adapter 지정.
        final ListView listview = (ListView) mView.findViewById(R.id.listview1);
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, ShowList) ;

        listview.setAdapter(adapter) ;

        final AlertDialog.Builder alert04 = new AlertDialog.Builder(BroadcastMain.this);

        Button btn_cancel = (Button)mView.findViewById(R.id.popup_cancel);
        Button selectAllButton = (Button)mView.findViewById(R.id.select_all);
        Button ban = (Button)mView.findViewById(R.id.ben);
        EditText search = (EditText)mView.findViewById(R.id.searchPeople);
        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout)mView.findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                //3초후에 해당 adapter를 갱신하고 동글뱅이를 닫아준다.setRefreshing(false);
                //핸들러를 사용하는 이유는 일반쓰레드는 메인쓰레드가 가진 UI에 접근할 수 없기 때문에 핸들러를 이용해서
                //메시지큐에 메시지를 전달하고 루퍼를 이용하여 순서대로 UI에 접근한다.
                //list.clear();
                //반대로 메인쓰레드에서 일반 쓰레드에 접근하기 위해서는 루퍼를 만들어야 한다.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //해당 어댑터를 서버와 통신한 값이 나오면 됨
                        //초기에 세팅한 리스트는 모두 사라지고 랜덤이름 3개가 뜸
                        getUserList();
                        for(User user : UserList){
                            ShowList.add(user.getUserId() + "(" + user.getNickname() + ")");
                        }
                        listview.setAdapter(adapter);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                },500);
            }
        });

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

        btn_cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        //사용자 벤
        ban.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                SparseBooleanArray checkedItems = listview.getCheckedItemPositions();
                int count = adapter.getCount() ;
                for (int i = count-1; i >= 0; i--) {
                    if (checkedItems.get(i)) {
                        mOpenChannel.muteUserWithUserId(UserList.get(i).getUserId(), "ban by operater", 10, new OpenChannel.OpenChannelMuteHandler() {
                            @Override
                            public void onResult(SendBirdException e) {
                                if(e!=null){
                                    Log.e("muteuser", e.getMessage()+e.getCode());
                                }
                            }
                        });
                    }
                }
                // 모든 선택 상태 초기화.
                listview.clearChoices() ;
                adapter.notifyDataSetChanged();
            }
        });

        //"Select All" Button 클릭 시 모든 아이템 선택.
        selectAllButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                int count = 0 ;
                count = adapter.getCount() ;

                for (int i=0; i<count; i++) {
                    listview.setItemChecked(i, true) ;
                }
            }
        }) ;

        alertDialog.show();
    }

    private void getUserList() {
        UserListQuery userListQuery = mOpenChannel.createParticipantListQuery();
        userListQuery.next(new UserListQuery.UserListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }
                UserList = setUserList(list);
            }
        });
    }
    private List<User> setUserList(List<User> userList) {
        List<User> sortedUserList = new ArrayList<>();
        for (User participant : userList) {
            if (!(participant.getUserId().equals(operator.get(0).getUserId()))) {
                //String listtext =
                sortedUserList.add(participant);//er.getUserId() + "(" + user.getNickname() + ")");
            }
        }
        return sortedUserList;
    }
    public void searching(String charText, ArrayAdapter A) {
        List<String> searchlist = new ArrayList<String>();
        List<String> alllist = new ArrayList<String>();
        for(User user : UserList){
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

    //카테고리 설정하는 팝업창
    public void btn_Category(View view) {
        View mView_c = getLayoutInflater().inflate(R.layout.popup_category, null);

        // ArrayAdapter 생성. 아이템 View를 선택(multiple choice)가능하도록 만듦.
        adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, category_items) ;

        // listview 생성 및 adapter 지정.
        ListView listView = (ListView)mView_c.findViewById(R.id.listView) ;
        listView.setAdapter(adapter1);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final AlertDialog.Builder alert05 = new AlertDialog.Builder(BroadcastMain.this);

        EditText editText = (EditText)mView_c.findViewById(R.id.editText);
        Button btn_Add = (Button)mView_c.findViewById(R.id.btnAdd);
        Button btn_Del = (Button)mView_c.findViewById(R.id.btnDel);
        Button btn_Exit = (Button)mView_c.findViewById(R.id.btnExit);
        Button btn_Select = (Button)mView_c.findViewById(R.id.btnSelect);

        alert05.setView(mView_c);

        final AlertDialog alertDialog = alert05.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_Add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String text = editText.getText().toString();
                if(text.length()!=0){
                    sendUserMessage("add:"+text,"category");
                    category_items.add(text);
                    editText.setText("");
                    adapter1.notifyDataSetChanged();
                }
            }
        });

        btn_Del.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                int pos;
                pos = listView.getCheckedItemPosition();
                if(pos != ListView.INVALID_POSITION){
                    sendUserMessage("delete:"+category_items.get(pos), "category");
                    category_items.remove(pos);
                    listView.clearChoices();
                    adapter1.notifyDataSetChanged();
                }
            }
        });

        btn_Exit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                adapter1.notifyDataSetChanged();
                alertDialog.dismiss();
            }
        });

        btn_Select.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                int pos2;
                pos2 = listView.getCheckedItemPosition();
                if(pos2 != ListView.INVALID_POSITION){
                    String current_item = category_items.get(pos2).toString();
                    String s_text = "지금은 " + current_item + "을 판매중!";
                    sendUserMessage(s_text, "notification");
                    sendUserMessage("select:"+current_item, "category");
                    mExampleChatController.add2(s_text);
                }
            }
        });
        alertDialog.show();
    }


}
