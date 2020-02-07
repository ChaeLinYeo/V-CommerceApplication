package com.pedro.rtpstreamer.player;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.utils.StaticVariable;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PlayerMain extends AppCompatActivity
    implements View.OnClickListener {

    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    private ArrayList<String> resourceUri = new ArrayList<>();
    private ArrayList<String> previewUri = new ArrayList<>();
    private ArrayList<String> sendUrl = new ArrayList<>();
    private ArrayList<Integer> channelNumList = new ArrayList<>();

    private int curBroad = 0;
    private int fragNum = -1;

    private boolean full_ing = false;

    //랜덤영문 +숫자
    private Random r = new Random();
    private int f = r.nextInt(26);
    private String f2 = Character.toString((char) (f+65));
    private int d = r.nextInt(26);
    private String d2 = Character.toString((char) (d+65));
    private int num = r.nextInt(10000);
    private int num2 = r.nextInt(10000);
    public static String USER_ID;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_main);

        USER_ID = f2 + d2 + num + num2;

        SendBird.init(getString(R.string.sendbird_app_id), this);
        SendBird.connect(USER_ID,
                (User user, SendBirdException e) -> {
                    if (e != null) {    // Error.
                        Log.d("connect error","connect : 1" );
                        return;
                    }
                    updateCurrentUserInfo(USER_ID);
                }
        );

        Intent intent = getIntent();
        if(intent.getAction() == null){
            Log.d("action","null");

        } else if(intent.getAction().equals(Intent.ACTION_VIEW)) {
            Log.d("action","action view");
            Uri uri = intent.getData();
            if(uri != null) {
                String data = uri.getQueryParameter("board_id");
                int channelNum = Integer.parseInt(""+data);
                Log.d("channel","channel num : "+channelNum);
                getSendbird(channelNum);
            }
        }

        initBtn();
    }

    private void updateCurrentUserInfo(final String userNickname) {
        SendBird.updateCurrentUserInfo(userNickname, null,
                (SendBirdException e) -> {
                    if (e != null) Log.e("nickname",e.getMessage()+" : "+e.getCode());
                }
        );
    }

    @Override
    public void onBackPressed(){
        if(full_ing){
            full_ing = false;
            FullVideoFragment fvf = (FullVideoFragment) getSupportFragmentManager().findFragmentByTag("fullFragment");
            if(fvf != null) fvf.closeFull();
            else Log.d("back pressed", "fvf is null");
        }

        super.onBackPressed();
    }

    private void initBtn(){
        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        findViewById(R.id.btn3).setOnClickListener(this);
        findViewById(R.id.btn4).setOnClickListener(this);
        findViewById(R.id.btn5).setOnClickListener(this);
        findViewById(R.id.btn6).setOnClickListener(this);
        findViewById(R.id.btn7).setOnClickListener(this);
        findViewById(R.id.btn8).setOnClickListener(this);
        findViewById(R.id.btn9).setOnClickListener(this);
        findViewById(R.id.btn10).setOnClickListener(this);
    }

    /////////////추가 구현 필요 @민아
    //방송정보로 각 버튼 내용 설정하기
    @Override
    public void onClick(View view){
        int channelNum = -1;
        switch(view.getId()){
            case R.id.btn1:
                channelNum = 0;
                break;
            case R.id.btn2:
                channelNum = 1;
                break;
            case R.id.btn3:
                channelNum = 2;
                break;
            case R.id.btn4:
                channelNum = 3;
                break;
            case R.id.btn5:
                channelNum = 4;
                break;
            case R.id.btn6:
                channelNum = 5;
                break;
            case R.id.btn7:
                channelNum = 6;
                break;
            case R.id.btn8:
                channelNum = 7;
                break;
            case R.id.btn9:
                channelNum = 8;
                break;
            case R.id.btn10:
                channelNum = 9;
                break;

                default:
                return;
        }

        getSendbird(channelNum);
    }

//    public void startBroadcast(int channelNum){
//        if(isBroadcasting(channelNum)) showBroadcast(channelNum);
//        else Toast.makeText(this, "This channel is not on air.", Toast.LENGTH_SHORT).show();
//    }
//
//    /////////////////구현 필요 @민아
//    //channelNum은 선택한 채널
//    //해당 채널이 방송중인지를 true/false로 리턴
//    public boolean isBroadcasting(int channelNum){
//        //test용. 지우고 해주세연
//        return channelNum == 0 || channelNum == 2;
//    }

    public void getSendbird(int channelNum){
        OpenChannel.getChannel(getString(R.string.sendbird_ctrlChannel),
                (OpenChannel openChannel, SendBirdException e) -> {
                    if (e != null) {    // Error.
                        Log.d("getchannelerror1", ""+e.getMessage());
                        e.printStackTrace();
                        return;
                    }
                    //////////////////////////////
                    openChannel.getAllMetaData(new BaseChannel.MetaDataHandler() {
                        @Override
                        public void onResult(Map<String, String> map, SendBirdException e) {
                            sendUrl.clear();
                            channelNumList.clear();
                            for(String key : map.keySet()) {
                                if (!map.get(key).equals("true")){
                                    sendUrl.add(map.get(key));
                                    channelNumList.add(Integer.parseInt(key));
                                }
                                else{
                                    if(key.equals(Integer.toString(channelNum))){
                                        sendUrl.clear();
                                        channelNumList.clear();
                                        Toast.makeText(getApplicationContext(), "This channel is not on air.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                            }
                            showBroadcast(channelNum);
                        }
                    });
                }
        );
    }

    public int setBroadcast(int channelNum){
        resourceUri.clear();
        previewUri.clear();
        curBroad=channelNumList.size();
        int res = -1;
        for(int i=0; i<curBroad; i++){
            resourceUri.add(null);
            previewUri.add(null);
        }
        for(int i=0; i<curBroad; i++){
            getBroadcast(channelNumList.get(i),i);
            if(channelNum == channelNumList.get(i)) res = i;
        }

        return res;
    }

    public void showBroadcast(int channelNum){
        this.fragNum = setBroadcast(channelNum);
    }

    public void startBroadcastPlay(){
        full_ing=true;
        setFull();
        FullVideoFragment fvf = (FullVideoFragment) getSupportFragmentManager().findFragmentByTag("fullFragment");
        if(fvf == null) {
            Log.d("showBroadcast", "fvf is null");
            return;
        }
        fvf.startFull(fragNum);
    }

    private void getBroadcast(int channelNum, int position){
        Request request = new Request.Builder()
                .url("https://api.bambuser.com/broadcasts?byAuthors="+StaticVariable.bambuserDefaultUrl +""+ StaticVariable.broadcastAuthor[channelNum])
                .addHeader("Accept", "application/vnd.bambuser.v1+json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + getString(R.string.application_key))
                .get()
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                runOnUiThread(()->{});
            }

            @Override
            public void onResponse(final Call call, final Response response) {
                String resource = null;
                String body = null;
                try {
                    if(response.body() != null) body = response.body().string();
                    else Log.d("request","null response");
                    JSONObject json = new JSONObject(body);
                    JSONArray results = json.getJSONArray("results");
                    JSONObject latestBroadcast = results.optJSONObject(0);
                    resource = latestBroadcast.optString("resourceUri");
                    resourceUri.set(position, resource);
                    previewUri.set(position, latestBroadcast.optString("preview"));
                    Log.d("request","add complete");
                } catch (Exception ignored) {}
                curBroad--;
                if(curBroad==0) runOnUiThread( () -> startBroadcastPlay());
            }
        });
    }

    public void setFull(){
        Log.d("setFull", ""+resourceUri.size());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        FullVideoFragment frag = new FullVideoFragment(resourceUri, previewUri, channelNumList, sendUrl);
        fragmentTransaction.add(R.id.fullVideo, frag, "fullFragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        getSupportFragmentManager().executePendingTransactions();
    }
}
