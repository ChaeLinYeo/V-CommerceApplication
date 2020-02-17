package com.pedro.rtpstreamer.player;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.bambuser.broadcaster.BroadcastPlayer;
import com.bambuser.broadcaster.PlayerState;
import com.bambuser.broadcaster.SurfaceViewWithAutoAR;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.server.SendbirdConnection;
import com.pedro.rtpstreamer.server.SendbirdListner;
import com.pedro.rtpstreamer.utils.ExampleChatController;
import com.pedro.rtpstreamer.utils.PopupManager;
import com.pedro.rtpstreamer.utils.fragmentListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.StringTokenizer;

import com.bumptech.glide.request.RequestOptions;

public class Fragment_player extends Fragment
    implements View.OnClickListener {
    private static String TAG = "Fragment_player";
    private static String TAG1 = "Frag1";

    private EditText mMessageEditText;
    private Button mMessageSendButton;
    private Button FollowButton;	//팔로우버튼
    private Button OnOffButton; //채팅온오프
    private TextView system_notice; //각종알림
    private InputMethodManager mIMM;

    private ExampleChatController mExampleChatController;

    private Context mContext;

    // 로띠 애니메이션뷰 선언
    private LottieAnimationView songLikeAnimButton;

    // 좋아요 클릭 여부
    private boolean is_follow = false;
    private boolean canChat = true;
    private ListView listView;
    private String mChannelUrl;

    private TextView heart;
    private TextView people;
    private TextView title;
    private TextView notify;
    private ImageView cover;
    private TextView streamer_nickname;

    private BroadcastPlayer mBroadcastPlayer;

    private TextView liveTextView;
    private ImageView img_preview;
    private SurfaceViewWithAutoAR mVideoSurfaceView;

    private int fragPosition;
    private int channelNum;

    private fragmentListener cL;
    private PopupManager pm;

    private int onoff = 1; //1은 on, 0은 off

//    Fragment_player(int fragPosition, int channelNum, String mChannelUrl){
//        this.fragPosition = fragPosition;
//        this.channelNum = channelNum;
//        this.mChannelUrl = mChannelUrl;
//
//        Log.d("fragment",""+fragPosition+"/"+channelNum+"/"+mChannelUrl);
//    }

    Fragment_player(int fragPosition){
        this.fragPosition = fragPosition;
        this.channelNum = SendbirdConnection.getLiveChannelNum(this.fragPosition);
        this.mChannelUrl = SendbirdConnection.getPlayChannelUrl(this.channelNum);
    }

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        mContext = context;
        Log.d(TAG1, "onAttach");

        try {
            cL = (fragmentListener) getParentFragment();
        } catch (ClassCastException castException) {
            Log.d("onAttach","classCastException");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.d(TAG1, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        liveTextView = view.findViewById(R.id.BroadcastLiveTextView);
        mVideoSurfaceView = view.findViewById(R.id.VideoSurfaceView);
        img_preview = view.findViewById(R.id.img_preview);

        pm = new PopupManager(getContext());

        // 로티 애니메이션뷰 리소스 아이디연결
        songLikeAnimButton = view.findViewById(R.id.button_song_like_animation);

        // Set up chat box
        mMessageSendButton =  view.findViewById(R.id.button_open_channel_chat_send);
        mMessageEditText = view.findViewById(R.id.edittext_chat_message);
        FollowButton = view.findViewById(R.id.followButton);
        OnOffButton = view.findViewById(R.id.btn_onoff);
        system_notice = view.findViewById(R.id.system_notice);
        listView = view.findViewById(R.id.ChatListView);

        view.findViewById(R.id.buy_button).setOnClickListener(this);
        view.findViewById(R.id.declare).setOnClickListener(this);
        view.findViewById(R.id.menu_share).setOnClickListener(this);
        view.findViewById(R.id.HeartIcon).setOnClickListener(this);

        heart = view.findViewById(R.id.heartnum);
        people = view.findViewById(R.id.peoplenum);
        title = view.findViewById(R.id.titleSpace);
        notify = view.findViewById(R.id.broadcast_notice);
        cover = view.findViewById(R.id.imageButton3);
        streamer_nickname = view.findViewById(R.id.nickname);

        ///////////////////////////////////////
        cL.createComplete(fragPosition);

        return view;
    }

    /////////////////////////////////////////////////////////////////
    private void init(){
        SendbirdConnection.setSendbirdListner(sendbirdListner);

        SendbirdConnection.getPlayChannel(channelNum);

        mExampleChatController = new ExampleChatController(mContext, listView, R.layout.chatline, R.id.chat_line_textview, R.id.chat_line_timeview);
        mExampleChatController.show();
        mExampleChatController.add("생방송 채팅에 참여하세요!");

        //////////////////////
        FragmentActivity fa = getActivity();
        if(fa != null) mIMM = (InputMethodManager) fa.getSystemService(Context.INPUT_METHOD_SERVICE);
        else return;


        mMessageSendButton.setOnClickListener( (View v) -> {
            if(canChat) {
                String text = SendbirdConnection.getUserId() + " : " + mMessageEditText.getText().toString();
                mExampleChatController.add(text);
                mMessageEditText.setText("");
                mIMM.hideSoftInputFromWindow(mMessageEditText.getWindowToken(), 0);
                SendbirdConnection.sendUserMessage(text, "chat");
            }else{
                mMessageEditText.setText("채팅 불가");
            }
        });

        FollowButton.setOnClickListener((View view) -> {
            if(!is_follow){//팔로우 안한 상태에서 클릭하면
                FollowButton.setText("팔로우 취소");
                String text = SendbirdConnection.getUserId()+"님이 팔로우 하셨습니다.";
                SendbirdConnection.sendUserMessage(text, "alarm");
                AlarmPlayer(text,3);
            }
            else{//팔로우 한 상태에서 클릭하면
                FollowButton.setText("팔로우");
            }
            is_follow = !is_follow;//상태 바꿈
        });

        //채팅과 각종알림 온오프
        OnOffButton.setOnClickListener((View view) -> {
            if(onoff == 1){
                mExampleChatController.hide();
                system_notice.setVisibility(View.GONE);
                OnOffButton.setText("ON");
                onoff = 0;
            }
            else if(onoff == 0){
                mExampleChatController.show();
                system_notice.setVisibility(View.VISIBLE);
                OnOffButton.setText("OFF");
                onoff = 1;
            }
        });
    }



    // 좋아요 로띠 애니메이션을 실행 시키는 메소드
    private boolean toggleSongLikeAnimButton(){

        songLikeAnimButton.setVisibility(View.VISIBLE);
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 0.5f).setDuration(500);

        animator.addUpdateListener((ValueAnimator animation) -> {
            songLikeAnimButton.setProgress((Float) animation.getAnimatedValue());
        });
        animator.start();

        return true;

    }

    void closeBroadcast(){
        if(mBroadcastPlayer != null)
            mBroadcastPlayer.close();
    }

    private SurfaceViewWithAutoAR getVideoSurfaceView(){
        return mVideoSurfaceView;
    }

    void playStart(String resourceUri,String id, final String previewUri){ //package private
        init();
        Log.d(TAG1, "playStart / chat "+mChannelUrl+"/ resourceUri "+resourceUri);
        Picasso.with(getActivity()).load(previewUri).into(img_preview);
        img_preview.setVisibility(View.VISIBLE);
        if (mBroadcastPlayer != null) mBroadcastPlayer.close();

        mBroadcastPlayer = null;
        Context context = getContext();
        if(context != null) mBroadcastPlayer = new BroadcastPlayer(context, resourceUri, id, mPlayerObserver);
        else return;

        mBroadcastPlayer.setSurfaceView(getVideoSurfaceView());
        mBroadcastPlayer.setAcceptType(BroadcastPlayer.AcceptType.ANY);
        mBroadcastPlayer.setViewerCountObserver(mViewerCountObserver);

        mBroadcastPlayer.load();
    }

    private PlayerMain getMain_activity(){ return (PlayerMain) getActivity(); }

    private TextView getLiveTextView(){
        return liveTextView;
    }

    private final BroadcastPlayer.ViewerCountObserver mViewerCountObserver = new BroadcastPlayer.ViewerCountObserver() {
        @Override
        public void onCurrentViewersUpdated(long viewers) {
            Log.d(TAG,"ViewerCountObserver " + viewers);
        }
        @Override
        public void onTotalViewersUpdated(long viewers) {
        }
    };

    private final BroadcastPlayer.Observer mPlayerObserver = new BroadcastPlayer.Observer() {
        @Override
        public void onStateChange(PlayerState state) {
            Log.d(TAG,"state : " + state );
            boolean isPlayingLive = mBroadcastPlayer != null && mBroadcastPlayer.isTypeLive() && mBroadcastPlayer.isPlaying();
            TextView tvlive = getLiveTextView();
            if (tvlive != null && getMain_activity() != null) {
                tvlive.setVisibility(isPlayingLive ? View.VISIBLE : View.GONE);
            }
            if (state == PlayerState.PLAYING || state == PlayerState.PAUSED || state == PlayerState.COMPLETED) {
                if(state == PlayerState.PLAYING)
                    img_preview.setVisibility(View.GONE);
                if(state == PlayerState.COMPLETED)
                    img_preview.setVisibility(View.VISIBLE);
            }
        }
        @Override
        public void onBroadcastLoaded(boolean live, int width, int height) {
            TextView tvlive = getLiveTextView();
            if (tvlive != null)
                tvlive.setVisibility(live ? View.VISIBLE : View.GONE);
        }
    };

    @Override
    public void onClick(View view){
        Log.d("btn onclick","click");
        switch(view.getId()){
            case R.id.buy_button:
                SendbirdConnection.getAllMetaData();
                pm.btn_buy(getLayoutInflater());
                break;

            case R.id.menu_share:
                pm.btn(channelNum);
                break;

            case R.id.declare:
                pm.select_Declare(getLayoutInflater());
                break;

            case R.id.HeartIcon:
                SendbirdConnection.sendUserMessage("", "like");
                SendbirdConnection.increaseMetaCounters();
                break;
        }
    }


    public void msgfilter(String customType, String data){
        switch(customType) {
            case "notice":
                notify.setText(data);
                //mExampleChatController.add2(Data);
                break;
            case "alarm":
                AlarmPlayer(data,3);
                break;
            case "chat" :
                mExampleChatController.add(data);
                break;
            case "event_everyone" :
                EventPlayer(data);
                break;
            case "event_someone" :
                EEventPlayer(data);
                break;
            default :
                break;
        }
    }

    public void EventPlayer(String data) {
        // "cn="+e_n+"ci="+e_a+"\nTimeLimit="+e_t_h+":"+e_t_m+":"+e_t_s;
        Log.d("event",""+data);
        int index = 3;
        int i = 0;
        String[] str = new String[index];
        HashMap<String, String> map = new HashMap<String, String>();
        StringTokenizer st = new StringTokenizer(data,"\n");
        while(st.hasMoreTokens()) {
            str[i] = st.nextToken();
            i++;
        }
        i = 0;
        while(i < index) {
            StringTokenizer st2 = new StringTokenizer(str[i],"=");
            map.put(st2.nextToken(), st2.nextToken());
            i++;
        }

        String TL= map.get("TimeLimit");
        StringTokenizer ei = new StringTokenizer(TL,":");
        int h = Integer.parseInt(ei.nextToken());
        int m = Integer.parseInt(ei.nextToken());
        int s = Integer.parseInt(ei.nextToken());

        pm.CouponPlayer(getLayoutInflater(), h, m, s, map.get("cn"), map.get("ci"));
    }

    public void EEventPlayer(String data) {
        // "User=    ,  ,    ,\ncn="+e_n+"ci="+e_a+"\nTimeLimit="+e_t_h+":"+e_t_m+":"+e_t_s;
        Log.d("event",""+data);
        int index = 4;
        int i = 0;
        boolean IsPlay = false;
        String[] str = new String[index];
        HashMap<String, String> map = new HashMap<String, String>();
        StringTokenizer st = new StringTokenizer(data,"\n");
        while(st.hasMoreTokens()) {
            str[i] = st.nextToken();
            i++;
        }
        i = 0;
        while(i < index) {
            StringTokenizer st2 = new StringTokenizer(str[i],"=");
            map.put(st2.nextToken(), st2.nextToken());
            i++;
        }
        String Users = map.get("User");
        StringTokenizer ui = new StringTokenizer(Users,",");
        while(ui.hasMoreTokens()){
            String cid = ui.nextToken();
            Log.d("compare", cid);
            if(cid.equals(SendbirdConnection.getUserId())) {
                Log.d("same", cid);
                IsPlay = true;
                break;
            }
        }

        if(IsPlay) {
            String TL= map.get("TimeLimit");
            StringTokenizer ei = new StringTokenizer(TL,":");
            int h = Integer.parseInt(ei.nextToken());
            int m = Integer.parseInt(ei.nextToken());
            int s = Integer.parseInt(ei.nextToken());
            pm.CouponPlayer(getLayoutInflater(), h, m, s, map.get("cn"), map.get("ci"));
        }
    }

    public void LikePlayer(int newheart){
        //하트를 재생하라는 명령을 받을때마다 하트의 개수를 동기화
        if(toggleSongLikeAnimButton())  {
            heart.setText(Integer.toString(newheart));
        }
    }

    public void AlarmPlayer(String data, int type){
        switch(type){
            case 1:
                system_notice.setText(data);
                system_notice.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_blue_bright));
                break;
            case 2:
                system_notice.setText(data);
                system_notice.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_red_light));
                break;
            case 3:
                system_notice.setText(data);
                system_notice.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_green_light));
                break;
        }

    }

    public void displayRoundImageFromUrl(final Context context, final String url, final ImageView imageView) {
        RequestOptions myOptions = new RequestOptions()
                .centerCrop()
                .dontAnimate();

        Glide.with(context)
                .asBitmap()
                .apply(myOptions)
                .load(url)
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    private void setUseableEditText(EditText et, boolean useable) {
        et.setClickable(useable);
        et.setEnabled(useable);
        et.setFocusable(useable);
        et.setFocusableInTouchMode(useable);
    }

    private SendbirdListner sendbirdListner = new SendbirdListner() {
        @Override
        public void getUserListComplete(String peopleNum) {
            super.getUserListComplete(peopleNum);
            people.setText(peopleNum);
        }

        @Override
        public void getPlayChannelComplete(String coverUrl, String titleString, String operator){
            displayRoundImageFromUrl(getContext(), coverUrl, cover);
            title.setText(titleString);
            streamer_nickname.setText(operator);
        }

        @Override
        public void onMessageReceived(String customType, String data){
            msgfilter(customType, data);
        }

        @Override
        public void metaCounterUpdated(int heart){
            LikePlayer(heart);
            if(heart % 100 == 0){
                AlarmPlayer(heart+"회 돌파~", 2);
            }
        }

        @Override
        public void loadInitialMessage(String type, String data){
            if(type.equals("chat")){
                mExampleChatController.add(data);
            }else if(type.equals("alarm")){
                AlarmPlayer(data,2);//alarm.setText(data);
            }else if(type.equals(("notice"))){
                notify.setText(data);
            }
        }

        @Override
        public void onTitleChanged(String titleString){
            title.setText(titleString);
        }

        @Override
        public void userenter(String enterduser) {
            super.userenter(enterduser);
            AlarmPlayer(enterduser + "님이 들어오셨습니다.", 1);
        }

        @Override
        public void Imbanned(){
            super.Imbanned();
            canChat = false;
            setUseableEditText(mMessageEditText,false);
        }

        @Override
        public void Imunbanned(){
            super.Imunbanned();
            canChat = true;
            setUseableEditText(mMessageEditText,true);
        }

    };
}
