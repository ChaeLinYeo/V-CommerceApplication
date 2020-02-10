package com.pedro.rtpstreamer.player;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bambuser.broadcaster.BroadcastPlayer;
import com.bambuser.broadcaster.PlayerState;
import com.bambuser.broadcaster.SurfaceViewWithAutoAR;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.utils.ExampleChatController;
import com.pedro.rtpstreamer.utils.PopupManager;
import com.pedro.rtpstreamer.utils.fragmentListener;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.PreviousMessageListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;
import com.sendbird.android.UserMessage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;

public class Fragment_player extends Fragment
    implements View.OnClickListener {
    private static String TAG = "Fragment_player";
    private static String TAG1 = "Frag1";

    /////////////////////////////////////////////////////////////
    private EditText mMessageEditText;
    private Button mMessageSendButton;
    private Button FollowButton;	//팔로우버튼
    private InputMethodManager mIMM;

    private ExampleChatController mExampleChatController;

    private Context mContext;

    // 로띠 애니메이션뷰 선언
    private LottieAnimationView songLikeAnimButton;

    // 좋아요 클릭 여부
    private boolean is_follow = false;

    //랜덤영문 +숫자
    private Random r = new Random();
    private int f = r.nextInt(26);
    private String f2 = Character.toString((char) (f+65));
    private int d = r.nextInt(26);
    private String d2 = Character.toString((char) (d+65));
    private int num = r.nextInt(10000);
    private int num2 = r.nextInt(10000);
    private String USER_ID = f2 + d2 + num + num2;


    private ListView listView;
    ///////////////////////////////////////////////////////////
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHAT";

    private OpenChannel mChannel;
    private String mChannelUrl;

    TextView alarm;
    TextView heart;
    TextView people;
    TextView title;
    TextView notify;
    ImageView cover;
    TextView streamer_nickname;

    // 쿠폰 이름, 내용, 시간, 분, 초
    ///////////////////////////////////////////////////////////

    private BroadcastPlayer mBroadcastPlayer;

    private TextView liveTextView;
    private ImageView img_preview;
    private SurfaceViewWithAutoAR mVideoSurfaceView;

    private int numFrag;
    private int channelNum;

    private fragmentListener cL;
    private PopupManager pm;

    Fragment_player(int numFrag, int channelNum, String mChannelUrl){
        this.numFrag = numFrag;
        this.channelNum = channelNum;
        this.mChannelUrl = mChannelUrl;

        Log.d("fragment",""+numFrag+"/"+channelNum+"/"+mChannelUrl);
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

        ///////////////////////////////////////
        // 로티 애니메이션뷰 리소스 아이디연결
        songLikeAnimButton = view.findViewById(R.id.button_song_like_animation);

        // Set up chat box
        mMessageSendButton =  view.findViewById(R.id.button_open_channel_chat_send);
        mMessageEditText = view.findViewById(R.id.edittext_chat_message);
        FollowButton = view.findViewById(R.id.followButton);
        listView = view.findViewById(R.id.ChatListView);

        view.findViewById(R.id.buy_button).setOnClickListener(this);
        view.findViewById(R.id.declare).setOnClickListener(this);
        view.findViewById(R.id.menu_share).setOnClickListener(this);
        view.findViewById(R.id.HeartIcon).setOnClickListener(this);

        alarm = view.findViewById(R.id.system_notice);
        heart = view.findViewById(R.id.heartnum);
        //시청인원
        people = view.findViewById(R.id.peoplenum);
        title = view.findViewById(R.id.titleSpace);

        notify = view.findViewById(R.id.broadcast_notice);
        cover = view.findViewById(R.id.imageButton3);
        streamer_nickname = view.findViewById(R.id.nickname);

        ///////////////////////////////////////
        cL.createComplete(numFrag);

        return view;
    }

    /////////////////////////////////////////////////////////////////
    private void init(){

        getChannel();
        connect();

        mExampleChatController = new ExampleChatController(mContext, listView, R.layout.chatline, R.id.chat_line_textview, R.id.chat_line_timeview);
        mExampleChatController.show();
        mExampleChatController.add("생방송 채팅에 참여하세요!");

        //////////////////////
        FragmentActivity fa = getActivity();
        if(fa != null) mIMM = (InputMethodManager) fa.getSystemService(Context.INPUT_METHOD_SERVICE);
        else return;

        mMessageSendButton.setOnClickListener( (View v) -> {
            String text = USER_ID+" : "+mMessageEditText.getText().toString();
            mExampleChatController.add(text);
            mMessageEditText.setText("");
            mIMM.hideSoftInputFromWindow(mMessageEditText.getWindowToken(), 0);
            sendUserMessage(text, "chat");
        });

        FollowButton.setOnClickListener((View view) -> {
            if(!is_follow){//팔로우 안한 상태에서 클릭하면
                FollowButton.setText("팔로우 취소");
                String text = USER_ID+"님이 팔로우 하셨습니다.";
                sendUserMessage(text, "alarm");
                AlarmPlayer(text);
            }
            else{//팔로우 한 상태에서 클릭하면
                FollowButton.setText("팔로우");
            }
            is_follow = !is_follow;//상태 바꿈
        });
    }

    // 좋아요 로띠 애니메이션을 실행 시키는 메소드
    private boolean toggleSongLikeAnimButton(){

        songLikeAnimButton.setVisibility(View.VISIBLE);
        // 애니메이션을 한번 실행시킨다.
        // Custom animation speed or duration.
        // ofFloat(시작 시간, 종료 시간).setDuration(지속시간)
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
                pm.btn_buy(getLayoutInflater());
                break;

            case R.id.menu_share:
                pm.btn(channelNum);
                break;

            case R.id.declare:
                pm.select_Declare(getLayoutInflater());
                break;

            case R.id.HeartIcon:
                sendUserMessage("","like");
                if(toggleSongLikeAnimButton()){
                    HashMap<String, Integer> counters = new HashMap<String, Integer>();
                    counters.put("heart", 1);
                    mChannel.increaseMetaCounters(counters, new BaseChannel.MetaCounterHandler() {
                        @Override
                        public void onResult(Map<String, Integer> map, SendBirdException e) {
                            if (e != null) {    // Error.
                                return;
                            }
                            heart.setText(Integer.toString(map.get("heart")));
                        }
                    });
                }
                break;
        }
    }


    public void msgfilter(BaseChannel baseChannel, BaseMessage baseMessage){
        String Custom_Type = baseMessage.getCustomType();
        String Data = baseMessage.getData();
        switch(Custom_Type) {
            case "notice":
                notify.setText(Data);
                //mExampleChatController.add2(Data);
                break;
            case "alarm":
                AlarmPlayer(Data);
                break;
            case "chat" :
                mExampleChatController.add(Data);
                break;
            case "event" :
                //방송자가 이벤트를 시작하겠다고 이벤트버트을 눌렀을때?(송출부 시작)
                //타입(Type)을 설정하고, 내용(이벤트 정보, 지속시간 : text1)을 정해서 보내면
                // ex. "eventonoff=on\nType=Pop\nTimeLimit=1:20:30\nEventInfo=AB23:PlaneText";
                EventPlayer(Data);
                break;
            case "effect" :
                // 방송자가 이펙트를 눌렀을 경우 (송출부시작)
                // effecturl의 경우 방송자가 선택한 것의 url을 받음
                // ex. "effectonoff=off\neffecturl=http://naver.com"
//                EffectPlayer(Data);
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

    public void LikePlayer(int newheart){
        //하트를 재생하라는 명령을 받을때마다 하트의 개수를 동기화
        if(toggleSongLikeAnimButton())  {
            heart.setText(Integer.toString(newheart));
        }
    }

    public void AlarmPlayer(String data){
        alarm.setText(data);
    }

    public static void EffectPlayer(String data) {
        int index = 2;
        String[] str = new String[index];
        String[] r_data= new String[index];
        int i = 0;
        StringTokenizer st = new StringTokenizer(data, "\n");
        while(st.hasMoreTokens()) {
            str[i] = st.nextToken();
            i++;
        }
        i = 0;
        while(i < index) {
            StringTokenizer st2 = new StringTokenizer(str[i],"=");
            st2.nextToken();
            r_data[i] = st2.nextToken();
            i++;
        }
        if(r_data[0].equals("off")) {
            //effect 재생 멈춤
        } else {
            //effect 재생 시작

        }
    }

    private void sendUserMessage(String text, String type) {
        if(mChannel == null) {
            Log.d("sendU", "channel is null");
            return;
        }
        mChannel.sendUserMessage(text, text, type, new BaseChannel.SendUserMessageHandler() {
            @Override
            public void onSent(UserMessage userMessage, SendBirdException e) {
                if (e != null) {
                    // Error!
                    Toast.makeText(
                            getContext(),
                            "Send failed with error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                Log.d("send1 success","");
            }
        });
        Log.d("send success", text);
    }

    public void connect(){
        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                Log.d(TAG, "connect : onMessageReceived 1" + mChannelUrl);
                // Add new message to view
                if (baseChannel.getUrl().equals(mChannelUrl)) {
                    Log.d(TAG, "connect : onMessageReceived 2");
                    msgfilter(baseChannel, baseMessage);
                }
            }

            @Override
            public void onMetaCountersUpdated(BaseChannel channel, Map<String, Integer> metaCounterMap) {
                super.onMetaCountersUpdated(channel, metaCounterMap);
                LikePlayer(metaCounterMap.get("heart"));
            }

            @Override
            public void onMetaDataCreated(BaseChannel channel, Map<String, String> metaDataMap) {
                super.onMetaDataCreated(channel, metaDataMap);
                for(String key : metaDataMap.keySet()){
                    pm.addCategoryI(key);
                }
            }

            @Override
            public void onMetaDataUpdated(BaseChannel channel, Map<String, String> metaDataMap) {
                super.onMetaDataUpdated(channel, metaDataMap);
                pm.clearCategoryI();
                for(Map.Entry<String, String> entry : metaDataMap.entrySet()){
                    if(entry.getKey().equals("empty")){
                        continue;
                    }
                    else{
                        pm.addCategoryI(entry.getKey());
                    }
                }
            }

            @Override
            public void onChannelChanged(BaseChannel channel) {
                super.onChannelChanged(channel);
                title.setText(channel.getName());
            }
        });
    }

    private void updateCurrentUserInfo(final String userNickname) {
        SendBird.updateCurrentUserInfo(userNickname, null,
                (SendBirdException e) -> {
                    if (e != null) Log.e("nickname",e.getMessage()+" : "+e.getCode());
                }
        );
    }

    private void getChannel(){
        SendBird.init(getString(R.string.sendbird_app_id), getContext());
        SendBird.connect(PlayerMain.USER_ID,
                (User user, SendBirdException e) -> {
                    if (e != null) {    // Error.
                        Log.d("connect error","connect : 1" );
                        return;
                    }
                    updateCurrentUserInfo(PlayerMain.USER_ID);
                }
        );

        Log.d("getCh", ""+mChannelUrl);
        OpenChannel.getChannel(mChannelUrl, new OpenChannel.OpenChannelGetHandler() {
            @Override
            public void onResult(final OpenChannel openChannel, SendBirdException e) {
                if (e != null) {    // Error.
                    Log.d("getchannel",""+e.getMessage());
                    return;
                }
                openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
                    @Override
                    public void onResult(SendBirdException e) {
                        if (e != null) {    // Error.
                            Log.d("getc","enter error");
                            e.printStackTrace();
                            return;
                        }
                        mChannel = openChannel;
                        getUserList();
                        displayRoundImageFromUrl(getContext(), mChannel.getCoverUrl(), cover);
                        loadInitialMessageList(20);
                        title.setText(mChannel.getName());
                        streamer_nickname.setText(mChannel.getOperators().get(0).getNickname());
                    }
                });
            }
        });
    }

    private void getUserList() {
        UserListQuery userListQuery = mChannel.createParticipantListQuery();
        userListQuery.next(new UserListQuery.UserListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }
                people.setText(Integer.toString(list.size()));
            }
        });
    }

    private void loadInitialMessageList(int numMessages) {
        PreviousMessageListQuery mPrevMessageListQuery = mChannel.createPreviousMessageListQuery();
        mPrevMessageListQuery.load(numMessages, true, new PreviousMessageListQuery.MessageListQueryResult() {
            @Override
            public void onResult(List<BaseMessage> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }
                for(BaseMessage b : list){
                    if(b.getCustomType().equals("chat")){
                        mExampleChatController.add(b.getData());
                    }else if(b.getCustomType().equals("alarm")){
                        alarm.setText(b.getData());
                    }else if(b.getCustomType().equals(("notification"))){
                        notify.setText(b.getData());
                    }
                }
                //mChatAdapter.setMessageList(list);
            }
        });
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
}
