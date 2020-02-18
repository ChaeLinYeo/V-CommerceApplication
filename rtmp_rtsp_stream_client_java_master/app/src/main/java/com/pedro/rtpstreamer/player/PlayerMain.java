package com.pedro.rtpstreamer.player;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.server.SendbirdConnection;
import com.pedro.rtpstreamer.server.SendbirdListner;
import com.pedro.rtpstreamer.utils.StaticVariable;
import com.pedro.rtpstreamer.utils.FragmentListener;

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
    implements View.OnClickListener{

    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    private ArrayList<String> resourceUri = new ArrayList<>();
    private ArrayList<String> previewUri = new ArrayList<>();

    private int curBroad = 0;
    private int fragPosition = -1;

    private boolean full_ing = false;

    private int selectedChannelNum=-1;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_main);

        SendbirdConnection.setupSendbird(this, false, sendbirdListner);

        initBtn();
    }

    @Override
    public void onBackPressed(){
        if(full_ing){
            full_ing = false;
            FullVideoFragment fvf = (FullVideoFragment) getSupportFragmentManager().findFragmentByTag("fullFragment");
            if(fvf != null) fvf.closeFull();
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
        switch(view.getId()){
            case R.id.btn1:
                selectedChannelNum = 0;
                break;
            case R.id.btn2:
                selectedChannelNum = 1;
                break;
            case R.id.btn3:
                selectedChannelNum = 2;
                break;
            case R.id.btn4:
                selectedChannelNum = 3;
                break;
            case R.id.btn5:
                selectedChannelNum = 4;
                break;
            case R.id.btn6:
                selectedChannelNum = 5;
                break;
            case R.id.btn7:
                selectedChannelNum = 6;
                break;
            case R.id.btn8:
                selectedChannelNum = 7;
                break;
            case R.id.btn9:
                selectedChannelNum = 8;
                break;
            case R.id.btn10:
                selectedChannelNum = 9;
                break;

                default:
                return;
        }

        SendbirdConnection.getCtrl();
    }

    public void setBroadcast(){
        resourceUri.clear();
        previewUri.clear();
        curBroad = 0;
        fragPosition = -1;
        for(int i=0; i<StaticVariable.numChannel; i++){
            if(SendbirdConnection.isLive(i)){
                resourceUri.add(null);
                previewUri.add(null);
                getBroadcast(i, curBroad);
                if(selectedChannelNum == i) fragPosition = curBroad;

                curBroad++;
            }
        }
    }

    public void startBroadcastPlay(){
        full_ing=true;
        setFull();
        FullVideoFragment fvf = (FullVideoFragment) getSupportFragmentManager().findFragmentByTag("fullFragment");
        if(fvf == null) {
            return;
        }
        fvf.startFull(fragPosition);
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
                    JSONObject json = new JSONObject(body);
                    JSONArray results = json.getJSONArray("results");
                    JSONObject latestBroadcast = results.optJSONObject(0);
                    resource = latestBroadcast.optString("resourceUri");
                    resourceUri.set(position, resource);
                    previewUri.set(position, latestBroadcast.optString("preview"));
                } catch (Exception ignored) {}
                curBroad--;
                if(curBroad==0) runOnUiThread( () -> startBroadcastPlay());
            }
        });
    }

    public void setFull(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        FullVideoFragment frag = new FullVideoFragment(resourceUri, previewUri, fragmentListener);
        fragmentTransaction.add(R.id.fullVideo, frag, "fullFragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    private SendbirdListner sendbirdListner = new SendbirdListner() {
        @Override
        public void getCtrlComplete() {
            SendbirdConnection.getLiveChannelUrlList();
        }

        @Override
        public void getChannelComplete(boolean success) {
            if (SendbirdConnection.isLive(selectedChannelNum)) {
                findViewById(R.id.PlayerLoadingPanel).setVisibility(View.VISIBLE);
                setBroadcast();
            }
            else Toast.makeText(getApplicationContext(), "This channel is not onAir", Toast.LENGTH_LONG).show();
        }
    };

    private FragmentListener fragmentListener = new FragmentListener() {
        @Override
        public void loadComplete() {
            super.loadComplete();
            findViewById(R.id.PlayerLoadingPanel).setVisibility(View.GONE);
        }
    };
}
