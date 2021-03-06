package com.pedro.rtpstreamer.player;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.utils.StaticVariable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

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

    private int curBroad = 0;
    private int fragNum = -1;

    private boolean full_ing = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_main);
        initBtn();
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

        if(isBroadcasting(channelNum)) showBroadcast(channelNum);
        else Toast.makeText(this, "This channel is not on air.", Toast.LENGTH_SHORT).show();
    }

    /////////////////구현 필요 @민아
    //channelNum은 선택한 채널
    //해당 채널이 방송중인지를 true/false로 리턴
    public boolean isBroadcasting(int channelNum){
        //test용. 지우고 해주세연
        return channelNum == 0 || channelNum == 2;
    }

    public int setBroadcast(int channelNum){
        resourceUri.clear();
        previewUri.clear();
        curBroad=0;
        int res = -1;
        for(int i=0; i<StaticVariable.numChannel; i++){
            if(isBroadcasting(i)) {
                getBroadcast(i);
                curBroad++;
                if(channelNum >= i) res++;
            }
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

    private void getBroadcast(int channelNum){
        Request request = new Request.Builder()
                .url("https://api.bambuser.com/broadcasts?byAuthors="+StaticVariable.defaultUrl+""+ StaticVariable.broadcastAuthor[channelNum])
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
                    resourceUri.add(resource);
                    previewUri.add(latestBroadcast.optString("preview"));
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
        FullVideoFragment frag = new FullVideoFragment(resourceUri, previewUri);
        fragmentTransaction.add(R.id.fullVideo, frag, "fullFragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        getSupportFragmentManager().executePendingTransactions();
    }
}
