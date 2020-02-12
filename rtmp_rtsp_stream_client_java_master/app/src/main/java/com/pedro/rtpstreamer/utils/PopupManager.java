package com.pedro.rtpstreamer.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PopupManager {

    private Context mContext;

    private ArrayList<String> category_items  = new ArrayList<>();//카테고리 아이템들

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

    private int save_time_before;
    private int save_time;

    public PopupManager(Context context){
        mContext = context;
    }

    public void btn_buy(LayoutInflater inflater) {
        View mView_c = inflater.inflate(R.layout.buylist_popup, null);

        // ArrayAdapter 생성. 아이템 View를 선택(multiple choice)가능하도록 만듦.
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_multiple_choice, category_items) ;

        // listview 생성 및 adapter 지정.
        ListView listView = mView_c.findViewById(R.id.listView) ;
        listView.setAdapter(adapter1);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final AlertDialog.Builder alert05 = new AlertDialog.Builder(mContext);

        Button btn_Exit = mView_c.findViewById(R.id.btnExit);

        alert05.setView(mView_c);

        final AlertDialog alertDialog = alert05.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_Exit.setOnClickListener((View view) -> alertDialog.dismiss());
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

    public void CouponPlayer(LayoutInflater inflater, int h, int m, int s, String name, String info) {
        final AlertDialog.Builder alert01 = new AlertDialog.Builder(mContext);
        View mView01 = inflater.inflate(R.layout.popup, null);
        final TextView coupon_name_txt = (TextView) mView01.findViewById(R.id.blabla011);
        final TextView coupon_ect_txt = (TextView) mView01.findViewById(R.id.blabla022);
        final TextView coupon_time_txt = (TextView) mView01.findViewById(R.id.blabla033);  //n초뒤 사라짐 이라고 띄우는 부분
        Button coupon_btn_cancel_01 = (Button) mView01.findViewById(R.id.coupon_btn_cancel_01);
        Button coupon_btn_ok_01 = (Button) mView01.findViewById(R.id.coupon_btn_ok_01);

        coupon_name_txt.setText(name);
        coupon_ect_txt.setText(info);

        //초*1000
        //분*1000*60
        //시*1000*60*60
        save_time_before = (h * 1000 * 60 * 60) + (m * 1000 * 60) + (s * 1000); //시간 int로 저장
        save_time = h * 3600 + m * 60 + s;
        alert01.setView(mView01);

        final AlertDialog alertDialog = alert01.create();
        alertDialog.setCanceledOnTouchOutside(false);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                coupon_time_txt.setText(save_time + "초 뒤 사라짐");
                // n초가 지나면 다이얼로그 닫기
                TimerTask task = new TimerTask() {
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

    public void clearCategoryI(){
        category_items.clear();
    }

    public void addCategoryI(String item){
        category_items.add(item);
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

        alert05.setView(mView);
        final androidx.appcompat.app.AlertDialog alertDialog = alert05.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btn_Exit.setOnClickListener((View view) -> alertDialog.dismiss());
        red.setOnClickListener((View view) -> {color = Color.RED;});
        black.setOnClickListener((View view) -> {color = Color.BLACK;});
        blue.setOnClickListener((View view) -> {color = Color.BLUE;});
        green.setOnClickListener((View view) -> {color = Color.GREEN;});
        btn_Accept.setOnClickListener((View view) -> {
            if(!text.getText().toString().equals("")){
                BM.setText(text.getText().toString(), color);
            }
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    //카테고리 설정하는 팝업창
    public void btn_Category(LayoutInflater inflater, LocalfileManager LM_time) {
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
                    int pos2;
                    pos2 = listView.getCheckedItemPosition();
                    if(pos2 != ListView.INVALID_POSITION){
                        String current_item = category_items.get(pos2);
                        LM_time.savetimeline(System.currentTimeMillis(),":"+current_item+"\n");
                        sendbirdConnection.removeCategory(category_items.get(pos2));
                        category_items.remove(pos2);
                        listView.clearChoices();
                        adapter1.notifyDataSetChanged();
                    }
                }
        );
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