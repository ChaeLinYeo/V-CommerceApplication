package com.pedro.rtpstreamer.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.SocialObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.util.helper.log.Logger;
import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.broadcaster.BroadcastMain;
import com.pedro.rtpstreamer.broadcaster.BroadcastManager;
import com.pedro.rtpstreamer.server.LocalfileManager;
import com.pedro.rtpstreamer.server.SendbirdConnection;
import com.sendbird.android.User;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.facebook.FacebookSdk.getApplicationContext;

public class PopupManager {

    private Context mContext;

    private static ArrayList<String> category_items = new ArrayList<>();//카테고리 아이템들
    private static ArrayList<String> selected_items = new ArrayList<>();
    private static ArrayList<String> temp = new ArrayList<>();
    private ArrayList<String> couponuser = new ArrayList<>();

    //신고 선택 구분용 변수
    //0은 선택 안된 것, 1은 선택 된 것.
    private boolean is_declare_1 = false;	//음란물
    private boolean is_declare_2 = false;	//욕설
    private boolean is_declare_3 = false;	//폭력
    private boolean is_declare_4 = false;	//부적절한 상품
    private boolean is_declare_5 = false;	//저작권
    private boolean is_declare_6 = false;	//기타
    //신고 사유 기술용 변수
    private String txt_dummy_save;
    Button makecoupon;
    //이벤트 쿠폰 팝업용 변수
    public String e_n = "";
    public String e_a="";
    public int e_t_h=0;
    public int e_t_m=0;
    public int e_t_s=0;
    private int save_time_before;
    private int save_time;

    //create_title의 팝업 끄는것을 PM밖에서 통제하기위해
    AlertDialog alertDialog;

    private final Handler handler = new Handler();
    TextView coupon_time_txt;

    public PopupManager(Context context){
        mContext = context;
    }

    public static void setCC(){
        temp.clear();
        int last = selected_items.size();
        if(last > 0) {
            Log.d("btn_buy", selected_items.get(last-1));
            temp.add(selected_items.get(last - 1));
            selected_items.remove(last - 1);
        }
    }
    public void PopupEnd(){
        alertDialog.dismiss();
    }
    public void create_Category(LayoutInflater inflater) {
        View mView_c = inflater.inflate(R.layout.popup_category, null);

        // ArrayAdapter 생성. 아이템 View를 선택(multiple choice)가능하도록 만듦.
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_multiple_choice, category_items) ;

        // listview 생성 및 adapter 지정.
        ListView listView = mView_c.findViewById(R.id.listView) ;
        listView.setAdapter(adapter1);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final androidx.appcompat.app.AlertDialog.Builder alert05 = new androidx.appcompat.app.AlertDialog.Builder(mContext);

        EditText editText = mView_c.findViewById(R.id.editText);
        Button btn_Add = mView_c.findViewById(R.id.btnAdd);
        Button btn_Del = mView_c.findViewById(R.id.btnDel);
        Button btn_Exit = mView_c.findViewById(R.id.btnExit);
        Button btn_Select = mView_c.findViewById(R.id.btnSelect);

        alert05.setView(mView_c);

        final androidx.appcompat.app.AlertDialog alertDialogcc = alert05.create();
        alertDialogcc.setCanceledOnTouchOutside(false);

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
                SendbirdConnection.addCategory(item);
            }
            adapter1.notifyDataSetChanged();
            alertDialogcc.dismiss();
        });

        btn_Select.setOnClickListener((View view) ->
                Toast.makeText(mContext.getApplicationContext(), "방송 시작 후 해당 상품을 판매할 때 눌러주세요.", Toast.LENGTH_LONG).show()
        );

        Toast.makeText(mContext.getApplicationContext(), "방송 시작 전, 판매할 상품의 카테고리를 기입해주세요.", Toast.LENGTH_LONG).show();
        alertDialogcc.show();
    }

    // 방송 시작 첫 제목 설정
    public void create_title(LayoutInflater inflater, TextView title_text, LocalfileManager LM) {
        View mView = inflater.inflate(R.layout.init_channel, null);
        final EditText newtitle = mView.findViewById(R.id.init_title);
        Button btn_cancel = mView.findViewById(R.id.init_cancel);
        Button btn_ok = mView.findViewById(R.id.init_ok);
        final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setView(mView);
        alertDialog  = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_cancel.setOnClickListener((View lView) -> alertDialog.dismiss());

        btn_ok.setOnClickListener(
                (View view) -> {
                    String init_t = newtitle.getText().toString();
                    SendbirdConnection.createChannel(init_t);
                    title_text.setText(init_t);
                    LM.savetitle(0, title_text.getText().toString());
                }
        );
        Toast.makeText(mContext.getApplicationContext(), "방송 시작 전, 방송의 제목을 입력해주세요.", Toast.LENGTH_LONG).show();
        alertDialog.show();
    }


    //쿠폰 이벤트를 만드는 팝업창
    public void btn_editPopUp(LayoutInflater inflater) {
        final androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(mContext);
        View mView = inflater.inflate(R.layout.popup_custom_dialog, null);

        final EditText txt_coupon_name = mView.findViewById(R.id.blabla01);
        final EditText txt_coupon_ect = mView.findViewById(R.id.blabla02);
        Button coupon_btn_ok_02 = mView.findViewById(R.id.coupon_btn_ok_02);
        Button coupon_btn_cancel_02 = mView.findViewById(R.id.coupon_btn_cancel_02);
        if(!e_n.equals("")) {txt_coupon_name.setText(e_n);}
        if(!e_a.equals("")) {txt_coupon_ect.setText(e_a);}

        NumberPicker numberpicker_h= mView.findViewById(R.id.hour);
        NumberPicker numberpicker_m= mView.findViewById(R.id.minute);
        NumberPicker numberpicker_s= mView.findViewById(R.id.seconds);

        alert.setView(mView);

        final androidx.appcompat.app.AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        //시
        numberpicker_h.setMinValue(0);
        numberpicker_h.setMaxValue(23);
        numberpicker_h.setValue(e_t_h);
        //분
        numberpicker_m.setMinValue(0);
        numberpicker_m.setMaxValue(59);
        numberpicker_m.setValue(e_t_m);
        //초
        numberpicker_s.setMinValue(0);
        numberpicker_s.setMaxValue(59);
        numberpicker_s.setValue(e_t_s);

        coupon_btn_cancel_02.setOnClickListener((View view) -> alertDialog.dismiss());

        coupon_btn_ok_02.setOnClickListener((View view) -> {
                    e_n=txt_coupon_name.getText().toString();
                    e_a=txt_coupon_ect.getText().toString();
                    e_t_h = numberpicker_h.getValue();
                    e_t_m = numberpicker_m.getValue();
                    e_t_s = numberpicker_s.getValue();
                    alertDialog.dismiss();
            if(e_a.equals("") || e_n.equals("") || (e_t_s < 1 && e_t_m < 1 && e_t_s < 1)){
                makecoupon.setText("이벤트 설정");
            }else{
                makecoupon.setText("이벤트 수정");}
            }
        );

        alertDialog.show();
    }

    //쿠폰 이벤트 생성 팝업창
    public void btn_showPopUp(LayoutInflater inflater){
        final androidx.appcompat.app.AlertDialog.Builder alert01 = new androidx.appcompat.app.AlertDialog.Builder(mContext);
        View mView01 = inflater.inflate(R.layout.popup_coupon, null);
        final EditText coupon_name_txt = mView01.findViewById(R.id.blabla011);
        final EditText coupon_ect_txt = mView01.findViewById(R.id.blabla022);
        final EditText coupon_time_txt = mView01.findViewById(R.id.blabla033);  //n초뒤 사라짐 이라고 띄우는 부분
        Button coupon_btn_cancel_01 = mView01.findViewById(R.id.coupon_btn_cancel_01);
        Button coupon_btn_ok_01 = mView01.findViewById(R.id.coupon_btn_ok_01);

        if(couponuser.size() >0){
            String result  = "User=";
            for(String user : couponuser) {
                Log.d("selectuser", user);
                result += user+",";
            }
            result +="\ncn="+e_n+"\nci="+e_a+"\nTimeLimit="+e_t_h+":"+e_t_m+":"+e_t_s;
            SendbirdConnection.sendUserMessage(result, "event_someone");
        }else {
            String result  = "cn="+e_n+"\nci="+e_a+"\nTimeLimit="+e_t_h+":"+e_t_m+":"+e_t_s;
            SendbirdConnection.sendUserMessage(result, "event_everyone");}

        coupon_name_txt.setText(e_n);
        coupon_ect_txt.setText(e_a);

        save_time_before = (e_t_h*1000*60*60) + (e_t_m*1000*60) + (e_t_s*1000); //시간 int로 저장
        save_time = e_t_h + e_t_m + e_t_s;
        alert01.setView(mView01);

        final androidx.appcompat.app.AlertDialog alertDialog = alert01.create();
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
        e_n =""; e_a =""; e_t_h = 0; e_t_m = 0; e_t_s = 0;
        alertDialog.show();
    }


    //시청자 목록 보는 팝업창
    public void btn_showPeople(LayoutInflater inflater) {
        View mView = inflater.inflate(R.layout.popup_people, null);

        //User만을 담은 유저리스트 생성
        List<User> userList = SendbirdConnection.getUserList(true);

        //리스트뷰에 보여주기 위한 리스트 생성
        ArrayList<String> ShowList = new ArrayList<>();
        for(User user : userList){
            ShowList.add(user.getUserId() + "(" + user.getNickname() + ")");
        }
        // listview 생성 및 adapter 지정.
        final ListView listview = mView.findViewById(R.id.listview1);
        final ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_multiple_choice, ShowList) ;

        listview.setAdapter(adapter) ;

        final androidx.appcompat.app.AlertDialog.Builder alert04 = new androidx.appcompat.app.AlertDialog.Builder(mContext);

        Button btn_cancel = mView.findViewById(R.id.popup_cancel);
        Button selectAllButton = mView.findViewById(R.id.select_all);
        Button ban = mView.findViewById(R.id.ben);
        Button sendcoupon =  mView.findViewById(R.id.show_event);
        makecoupon =  mView.findViewById(R.id.custom_event);
        EditText search = mView.findViewById(R.id.searchPeople);
        SwipeRefreshLayout mSwipeRefreshLayout = mView.findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
                    mSwipeRefreshLayout.setRefreshing(true);
                    new Handler().postDelayed(() -> {
                                ShowList.clear();
                                List<User> reuserList = SendbirdConnection.getUserList(true);
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

        final androidx.appcompat.app.AlertDialog alertDialog = alert04.create();
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
                            SendbirdConnection.banUser(i);
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

        makecoupon.setOnClickListener((View v) ->{
            btn_editPopUp(inflater);
        });

        sendcoupon.setOnClickListener((View v) -> {
                    if(e_a.equals("") || e_n.equals("") || (e_t_s < 1 && e_t_m < 1 && e_t_s < 1)){
                        Log.d("dd", e_a +"," + e_n);
                        Log.d("ddd", Integer.toString(e_t_h)+","+Integer.toString(e_t_m)+","+Integer.toString(e_t_s));
                        Toast.makeText(mContext.getApplicationContext(), "쿠폰 정보를 입력하세요", Toast.LENGTH_LONG).show();
                    }else{
                        SparseBooleanArray checkedItems = listview.getCheckedItemPositions();
                        int count = adapter.getCount();
                        couponuser.clear();
                        for (int i = count - 1; i >= 0; i--) {
                            if (checkedItems.get(i)) {
                                couponuser.add(userList.get(i).getUserId());
                            }
                        }
                        btn_showPopUp(inflater);
                        // 모든 선택 상태 초기화.
                        listview.clearChoices();
                        adapter.notifyDataSetChanged();
                        makecoupon.setText("이벤트 설정");
                    }
                }
        );

        alertDialog.show();
    }

    public void searching(String charText, ArrayAdapter A) {
        List<String> searchlist = new ArrayList<>();
        List<String> alllist = new ArrayList<>();
        ///////////////////////////////////////////////
        List<User> userList = SendbirdConnection.getUserList(false);
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

    public void btn_buy(LayoutInflater inflater) {
        View mView_c = inflater.inflate(R.layout.buylist_popup, null);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, category_items) ;
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, selected_items) ;
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, temp) ;

        // listview 생성 및 adapter 지정.
        ListView listView = mView_c.findViewById(R.id.listView);
        final AlertDialog.Builder alert05 = new AlertDialog.Builder(mContext);

        Button btn_Exit = mView_c.findViewById(R.id.btnExit);
        Button already = mView_c.findViewById(R.id.post);
        Button notyet = mView_c.findViewById(R.id.pre);
        Button now = mView_c.findViewById(R.id.curr);

        alert05.setView(mView_c);

        final AlertDialog alertDialog = alert05.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_Exit.setOnClickListener((View view) -> alertDialog.dismiss());
        notyet.setOnClickListener((View view) -> {
            listView.setAdapter(adapter1);
            adapter1.notifyDataSetChanged();
        });
        already.setOnClickListener((View view) -> {
            listView.setAdapter(adapter2);
            adapter2.notifyDataSetChanged();
        });
        now.setOnClickListener((View view) -> {
            listView.setAdapter(adapter3);
            adapter3.notifyDataSetChanged();
        });
        alertDialog.show();
    }

    public void select_Declare(LayoutInflater inflater) {
        Log.d("declare","0");
        final AlertDialog.Builder alert01 = new AlertDialog.Builder(mContext);
        Log.d("declare","0");
        View mView = inflater.inflate(R.layout.declare_popup01, null);
        Log.d("declare","0");

        //신고항목
        final Button txt_input1 = mView.findViewById(R.id.txt_input1);
        final Button txt_input2 = mView.findViewById(R.id.txt_input2);
        final Button txt_input3 = mView.findViewById(R.id.txt_input3);
        final Button txt_input4 = mView.findViewById(R.id.txt_input4);
        final Button txt_input5 = mView.findViewById(R.id.txt_input5);
        final Button txt_input6 = mView.findViewById(R.id.txt_input6);
        Log.d("declare","0");

        //취소, 다음
        Button btn_cancel01 = mView.findViewById(R.id.btn_cancel1);
        Button btn_ok01 = mView.findViewById(R.id.btn_ok1);

        alert01.setView(mView);

        final AlertDialog alertDialog = alert01.create();
        alertDialog.setCanceledOnTouchOutside(false);

        txt_input1.setOnTouchListener(declareSelectListener);

        txt_input2.setOnTouchListener(declareSelectListener);

        txt_input3.setOnTouchListener(declareSelectListener);

        txt_input4.setOnTouchListener(declareSelectListener);

        txt_input5.setOnTouchListener(declareSelectListener);

        txt_input6.setOnTouchListener(declareSelectListener);

        btn_cancel01.setOnClickListener((View view) -> alertDialog.dismiss());

        btn_ok01.setOnClickListener((View view) -> {
            alertDialog.dismiss();
            write_Declare(inflater);
        });

        alertDialog.show();
    }

    //신고 사유 기술 팝업창
    private void write_Declare(LayoutInflater inflater) {
        final AlertDialog.Builder alert02 = new AlertDialog.Builder(mContext);
        View mView = inflater.inflate(R.layout.declare_popup02, null);

        final EditText txt_input = mView.findViewById(R.id.txt_input);
        Button btn_cancel02 = mView.findViewById(R.id.btn_cancel2);
        Button btn_ok02 = mView.findViewById(R.id.btn_ok2);

        alert02.setView(mView);

        final AlertDialog alertDialog = alert02.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_cancel02.setOnClickListener((View view) -> alertDialog.dismiss());

        btn_ok02.setOnClickListener((View view) -> {
            //txt_dummy_save에 신고 사유 기술 내용을 저장한다.
            txt_dummy_save = txt_input.getText().toString();
            Log.d("declare",txt_dummy_save);
            alertDialog.dismiss();
        });

        alertDialog.show();
    }


    //공유
    public void btn(int channelNum) {
        //카카오 공유기능
//        FeedTemplate params = FeedTemplate
//                .newBuilder(ContentObject.newBuilder("bambuser수신 앱 공유!",
//                        "https://hanyang.web.app/img/big-hanyang.png",
//                        LinkObject.newBuilder().setWebUrl("https://developers.kakao.com")
//                                .setMobileWebUrl("https://developers.kakao.com").build())
//                        .setDescrption("클릭해서 지금 라이브 방송을 시청하세요")
//                        .build())
//                .setSocial(SocialObject.newBuilder().setLikeCount(10).setCommentCount(20)
//                        .setSharedCount(30).setViewCount(40).build())
//                .addButton(new ButtonObject("웹에서 보기", LinkObject.newBuilder().setWebUrl("'https://developers.kakao.com").setMobileWebUrl("'https://developers.kakao.com").build()))
//                .addButton(new ButtonObject("앱에서 보기", LinkObject.newBuilder()
//                        .setWebUrl("'https://developers.kakao.com")
//                        .setMobileWebUrl("'https://developers.kakao.com")
//                        .setAndroidExecutionParams("board_id="+channelNum)
//                        .setIosExecutionParams("key1=value1")
//                        .build()))
//                .build();
//
//        Map<String, String> serverCallbackArgs = new HashMap<>();
//        serverCallbackArgs.put("user_id", "${current_user_id}");
//        serverCallbackArgs.put("product_id", "${shared_product_id}");
//
//        KakaoLinkService.getInstance().sendDefault(mContext, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
//            @Override
//            public void onFailure(ErrorResult errorResult) {
//                Logger.e(errorResult.toString());
//            }
//
//            @Override
//            public void onSuccess(KakaoLinkResponse result) {
//                // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용하여야 한다.
//            }
//        });

        //파이어베이스 공유기능
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        // Set default text message
        // 카톡, 이메일, MMS 다 이걸로 설정 가능
        //String subject = "문자의 제목";
        String text = "https://chaelin.page.link/eNh4" +
                "\n"+"설치시 앱 or 미설치시 구글 플레이스토어로 이동";
        intent.putExtra(Intent.EXTRA_TEXT, text);
        // Title of intent
        Intent chooser = Intent.createChooser(intent, "친구에게 공유하기");
        mContext.startActivity(chooser);
    }

    private View.OnTouchListener declareSelectListener = (View view, MotionEvent motionEvent) -> {
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                view.setBackgroundColor(Color.parseColor("#C5C5C5"));
                return true;
        } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
            boolean is_declare = false;
            switch(view.getId()){
                case R.id.txt_input1:
                    is_declare = is_declare_1;
                    is_declare_1 = !is_declare_1;
                    break;
                case R.id.txt_input2:
                    is_declare = is_declare_2;
                    is_declare_2 = !is_declare_2;
                    break;
                case R.id.txt_input3:
                    is_declare = is_declare_3;
                    is_declare_3 = !is_declare_3;
                    break;
                case R.id.txt_input4:
                    is_declare = is_declare_4;
                    is_declare_4 = !is_declare_4;
                    break;
                case R.id.txt_input5:
                    is_declare = is_declare_5;
                    is_declare_5 = !is_declare_5;
                    break;
                case R.id.txt_input6:
                    is_declare = is_declare_6;
                    is_declare_6 = !is_declare_6;
                    break;
            }
            if(!is_declare) view.setBackgroundColor(Color.parseColor("#E91E63"));
            else view.setBackgroundColor(Color.parseColor("#C5C5C5"));

            return true;
        }
        view.performClick();
        return false;
    };

    //만들어진 쿠폰 이벤트가 띄워지는 팝업창
    public void CouponPlayer(LayoutInflater inflater, int h, int m, int s, String name, String info) {
        final AlertDialog.Builder alert01 = new AlertDialog.Builder(mContext);
        View mView01 = inflater.inflate(R.layout.popup, null);
        final TextView coupon_name_txt = (TextView) mView01.findViewById(R.id.blabla011);
        final TextView coupon_ect_txt = (TextView) mView01.findViewById(R.id.blabla022);
        coupon_time_txt = (TextView) mView01.findViewById(R.id.blabla033);  //n초뒤 사라짐 이라고 띄우는 부분
        Button coupon_btn_cancel_01 = (Button) mView01.findViewById(R.id.coupon_btn_cancel_01);
        Button coupon_btn_ok_01 = (Button) mView01.findViewById(R.id.coupon_btn_ok_01);

        coupon_name_txt.setText(name);
        coupon_ect_txt.setText(info);

        save_time_before = (h * 1000 * 60 * 60) + (m * 1000 * 60) + (s * 1000);
        save_time = h * 3600 + m * 60 + s;
        alert01.setView(mView01);

        final AlertDialog alertDialog = alert01.create();
        alertDialog.setCanceledOnTouchOutside(false);

//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
////                coupon_time_txt.setText(save_time + "초 뒤 사라짐");
//                // n초가 지나면 다이얼로그 닫기
//                TimerTask task = new TimerTask() {
//                    @Override
//                    public void run() {
//                        Update();
//                        save_time--;
//                        alertDialog.dismiss();
//                    }
//                };
//                Timer timer = new Timer();
//                timer.schedule(task, save_time_before);
//            }
//        });
//        thread.start();

        TimerTask task = new TimerTask(){
            @Override
            public void run() {
//                Update();
                save_time--;
                coupon_time_txt.post(new Runnable() {
                    @Override
                    public void run() {
                        coupon_time_txt.setText(save_time + "초 뒤 사라짐");
                    }
                });
                alertDialog.dismiss();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, save_time_before);

        coupon_btn_cancel_01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        coupon_btn_ok_01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 사용자의 쿠폰함에 일련 쿠폰 번호 저장 (사용자는 쿠폰 번호 볼 수 X)
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public static void clearCategoryI(){
        category_items.clear();
    }
    public static void clearSCategory() { selected_items.clear(); }

    public static void addCategoryI(String item){
        category_items.add(item);
    }
    public static void addSCategory(String item) { selected_items.add(item); }

    public static void setCategory(List<String> cate) {
        ArrayList<String> aL = (ArrayList<String>)cate;
        category_items = aL;
    }

    //공지 수정 팝업창
    public void btn_showDialog2(LayoutInflater inflater, TextView broadcast_notice) {
        final androidx.appcompat.app.AlertDialog.Builder alert03 = new androidx.appcompat.app.AlertDialog.Builder(mContext);
        SendbirdConnection sendbirdConnection = SendbirdConnection.getInstance();

        View mView = inflater.inflate(R.layout.notification_custom_dialog, null);
        final EditText txt_inputText2 = mView.findViewById(R.id.txt_input2);
        Button btn_cancel2 = mView.findViewById(R.id.btn_cancel2);
        Button btn_ok2 = mView.findViewById(R.id.btn_ok2);

        alert03.setView(mView);

        final androidx.appcompat.app.AlertDialog alertDialog = alert03.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_cancel2.setOnClickListener((View view) -> alertDialog.dismiss());

        btn_ok2.setOnClickListener((View view) -> {
                    broadcast_notice.setText(txt_inputText2.getText().toString());
                    sendbirdConnection.sendUserMessage(txt_inputText2.getText().toString(), "notice");
                    alertDialog.dismiss();
                }
        );

        alertDialog.show();
    }
    int color;

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void btn_Text(LayoutInflater inflater, BroadcastManager BM) {
        View mView = inflater.inflate(R.layout.text_setup, null);
        final androidx.appcompat.app.AlertDialog.Builder alert05 = new androidx.appcompat.app.AlertDialog.Builder(mContext);
        color = Color.BLACK;
        EditText text = mView.findViewById(R.id.newText);
        Button btn_Exit = mView.findViewById(R.id.btnExit);
        Button btn_Accept = mView.findViewById(R.id.btnAccept);
        Button red = mView.findViewById(R.id.select_red);
        Button green = mView.findViewById(R.id.select_green);
        Button blue = mView.findViewById(R.id.select_blue);
        Button black = mView.findViewById(R.id.select_black);
        TextView select_color = mView.findViewById(R.id.select_color);

        alert05.setView(mView);
        final androidx.appcompat.app.AlertDialog alertDialog = alert05.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_Exit.setOnClickListener((View view) -> alertDialog.dismiss());
        red.setOnClickListener((View view) -> {
            color = Color.RED;
            select_color.setText("현재 선택된 색 : 빨강");
            text.setTextColor(Color.RED);
        });
        black.setOnClickListener((View view) -> {
            color = Color.BLACK;
            select_color.setText("현재 선택된 색 : 검정");
            text.setTextColor(Color.BLACK);
        });
        blue.setOnClickListener((View view) -> {
            color = Color.BLUE;
            select_color.setText("현재 선택된 색 : 파랑");
            text.setTextColor(Color.BLUE);
        });
        green.setOnClickListener((View view) -> {
            color = Color.GREEN;
            select_color.setText("현재 선택된 색 : 초록");
            text.setTextColor(Color.GREEN);
        });
        btn_Accept.setOnClickListener((View view) -> {
            if(!text.getText().toString().equals("")){
                BM.setText(text.getText().toString(), color);
            }
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    //카테고리 설정하는 팝업창
    public void btn_Category(LayoutInflater inflater, LocalfileManager LM_time, long time) {
        SendbirdConnection sendbirdConnection = SendbirdConnection.getInstance();
        View mView_c = inflater.inflate(R.layout.popup_category, null);

        // ArrayAdapter 생성. 아이템 View를 선택(multiple choice)가능하도록 만듦.

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_multiple_choice, category_items) ;

        // listview 생성 및 adapter 지정.
        ListView listView = mView_c.findViewById(R.id.listView) ;
        listView.setAdapter(adapter1);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final androidx.appcompat.app.AlertDialog.Builder alert05 = new androidx.appcompat.app.AlertDialog.Builder(mContext);

        EditText editText = mView_c.findViewById(R.id.editText);
        Button btn_Add = mView_c.findViewById(R.id.btnAdd);
        Button btn_Del = mView_c.findViewById(R.id.btnDel);
        Button btn_Exit = mView_c.findViewById(R.id.btnExit);
        Button btn_Select = mView_c.findViewById(R.id.btnSelect);

        alert05.setView(mView_c);

        final androidx.appcompat.app.AlertDialog alertDialog = alert05.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_Add.setOnClickListener((View view) -> {
                    String text = editText.getText().toString();
                    if(text.length()!=0){
                        sendbirdConnection.addCategory(text);
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
                        sendbirdConnection.removeCategory(category_items.get(pos));
                        category_items.remove(pos);
                        listView.clearChoices();
                        adapter1.notifyDataSetChanged();
                    }
                }
        );

        btn_Exit.setOnClickListener((View view) -> {
                    adapter1.notifyDataSetChanged();
                    alertDialog.dismiss();
                }
        );

        btn_Select.setOnClickListener((View view) -> {
            long t = System.currentTimeMillis()-time;
            int pos2;
            pos2 = listView.getCheckedItemPosition();
            if(pos2 != ListView.INVALID_POSITION){
                String current_item = category_items.get(pos2);
                sendbirdConnection.selectCategory(current_item);
                LM_time.savetimeline(t,current_item+"\n");
                category_items.remove(pos2);
                listView.clearChoices();
                adapter1.notifyDataSetChanged();
            }
        });
        alertDialog.show();
    }

    //제목 수정 팝업창
    public void btn_showDialog(LayoutInflater inflater, LocalfileManager LM, long systemtime, TextView title_text) {
        final androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(mContext);
        View mView = inflater.inflate(R.layout.custom_dialog, null);
        SendbirdConnection sendbirdConnection = SendbirdConnection.getInstance();

        final EditText txt_inputText = mView.findViewById(R.id.init_title);
        Button btn_cancel = mView.findViewById(R.id.btn_cancel);
        Button btn_ok = mView.findViewById(R.id.btn_ok);

        alert.setView(mView);

        final androidx.appcompat.app.AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_cancel.setOnClickListener((View view) -> alertDialog.dismiss());

        btn_ok.setOnClickListener((View view) -> {
                    String init_t = txt_inputText.getText().toString();
                    long time = System.currentTimeMillis() - systemtime;
                    title_text.setText(init_t);
                    sendbirdConnection.updateTitle(init_t);
                    LM.savetitle(time, init_t);
                    alertDialog.dismiss();
                }
        );
        alertDialog.show();
    }

}