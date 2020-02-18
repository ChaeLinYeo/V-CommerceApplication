package com.pedro.rtpstreamer.player;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView system_notice; //각종알림
    private InputMethodManager mIMM;
    private Button DeclareButton;
    private ImageButton ShareButton;
    private ImageView heartimg, eyeimg;
    private LinearLayout BottomBar;
    private RelativeLayout titleEtc;

    private ExampleChatController mExampleChatController;

    private Context mContext;

    // 로띠 애니메이션뷰 선언
    private LottieAnimationView songLikeAnimButton;

    // 좋아요 클릭 여부
    private boolean is_follow = false;
    private boolean canChat = true;
    private ListView listView;

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
    private RelativeLayout background;

    private int fragPosition;
    private int channelNum;

    private PopupManager pm;

    private int onoff = 1; //1은 on, 0은 off
    private int back_onoff = 1; //1은 on, 0은 off

    Fragment_player(int fragPosition){
        this.fragPosition = fragPosition;
        this.channelNum = SendbirdConnection.getLiveChannelNum(this.fragPosition);
    }

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
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
        system_notice = view.findViewById(R.id.system_notice);
        listView = view.findViewById(R.id.ChatListView);
        DeclareButton = view.findViewById(R.id.declare);
        ShareButton = view.findViewById(R.id.menu_share);
        heartimg = view.findViewById(R.id.imageView);
        eyeimg = view.findViewById(R.id.heartImage);
        BottomBar = view.findViewById(R.id.layout_open_chat_chatbox);
        titleEtc = view.findViewById(R.id.titleEtc);
        background = view.findViewById(R.id.rl_Live);

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
                if(!mMessageEditText.getText().toString().equals("")) {
                    String text = SendbirdConnection.getUserId() + " : " + mMessageEditText.getText().toString();
                    mExampleChatController.add(text);
                    mMessageEditText.setText("");
                    mIMM.hideSoftInputFromWindow(mMessageEditText.getWindowToken(), 0);
                    SendbirdConnection.sendUserMessage(text, "chat");
                }else{
                    Toast.makeText(mContext.getApplicationContext(), "채팅창에 내용을 입력하세요", Toast.LENGTH_LONG).show();
                }
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


        title.setOnClickListener((View view) -> {
            if(onoff == 1){
//                mExampleChatController.hide();
//                system_notice.setVisibility(View.GONE);
//                FollowButton.setVisibility(View.GONE);
//                DeclareButton.setVisibility(View.GONE);
//                ShareButton.setVisibility(View.GONE);
//                notify.setVisibility(View.GONE);
//                heartimg.setVisibility(View.GONE);
//                eyeimg.setVisibility(View.GONE);
//                BottomBar.setVisibility(View.GONE);
//                heart.setVisibility(View.GONE);
//                people.setVisibility(View.GONE);
//                songLikeAnimButton.setVisibility(View.GONE);
                background.setVisibility(View.GONE);
                onoff = 0;
            }
            else if(onoff == 0){
//                mExampleChatController.show();
//                system_notice.setVisibility(View.VISIBLE);
//                FollowButton.setVisibility(View.VISIBLE);
//                DeclareButton.setVisibility(View.VISIBLE);
//                ShareButton.setVisibility(View.VISIBLE);
//                notify.setVisibility(View.VISIBLE);
//                heartimg.setVisibility(View.VISIBLE);
//                eyeimg.setVisibility(View.VISIBLE);
//                BottomBar.setVisibility(View.VISIBLE);
//                heart.setVisibility(View.VISIBLE);
//                people.setVisibility(View.VISIBLE);
//                songLikeAnimButton.setVisibility(View.VISIBLE);
                background.setVisibility(View.VISIBLE);
                onoff = 1;
            }
        });


        mVideoSurfaceView.setOnClickListener((View view) -> {
            if(back_onoff == 1){
//                mExampleChatController.hide();
//                system_notice.setVisibility(View.GONE);
//                FollowButton.setVisibility(View.GONE);
//                DeclareButton.setVisibility(View.GONE);
//                ShareButton.setVisibility(View.GONE);
//                notify.setVisibility(View.GONE);
//                heartimg.setVisibility(View.GONE);
//                eyeimg.setVisibility(View.GONE);
//                BottomBar.setVisibility(View.GONE);
//                heart.setVisibility(View.GONE);
//                people.setVisibility(View.GONE);
//                songLikeAnimButton.setVisibility(View.GONE);
//                title.setVisibility(View.GONE);
//                streamer_nickname.setVisibility(View.GONE);
//                cover.setVisibility(View.GONE);
                titleEtc.setVisibility(View.GONE);
                background.setVisibility(View.GONE);
                back_onoff = 0;
            }
            else if(back_onoff == 0){
//                mExampleChatController.hide();
//                system_notice.setVisibility(View.VISIBLE);
//                FollowButton.setVisibility(View.VISIBLE);
//                DeclareButton.setVisibility(View.VISIBLE);
//                ShareButton.setVisibility(View.VISIBLE);
//                notify.setVisibility(View.VISIBLE);
//                heartimg.setVisibility(View.VISIBLE);
//                eyeimg.setVisibility(View.VISIBLE);
//                BottomBar.setVisibility(View.VISIBLE);
//                heart.setVisibility(View.VISIBLE);
//                people.setVisibility(View.VISIBLE);
//                songLikeAnimButton.setVisibility(View.VISIBLE);
//                title.setVisibility(View.VISIBLE);
//                streamer_nickname.setVisibility(View.VISIBLE);
//                cover.setVisibility(View.VISIBLE);
                titleEtc.setVisibility(View.VISIBLE);
                background.setVisibility(View.VISIBLE);
                back_onoff = 1;
            }
        });
    }



    // 좋아요 로띠 애니메이션을 실행 시키는 메소드
    private boolean toggleSongLikeAnimButton(){
        songLikeAnimButton.setVisibility(View.VISIBLE);
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 0.5f).setDuration(500);

        animator.addUpdateListener((ValueAnimator animation) ->
            songLikeAnimButton.setProgress((Float) animation.getAnimatedValue())
        );
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
                if(canChat) {
                    SendbirdConnection.sendUserMessage("", "like");
                    SendbirdConnection.increaseMetaCounters();
                }else{
                    Toast.makeText(mContext.getApplicationContext(), "X", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    public void msgfilter(String customType, String data){
        switch(customType) {
            case "notice":
<<<<<<< HEAD
//                notify.setText(data);
                setReadMore(notify, data, 2);
=======
                notify.setText(data);
>>>>>>> 8fdef2f4447fb74b22d17b702dc9f141c254cdef
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
        Log.d("event",""+data);
        int index = 3;
        int i = 0;
        String[] str = new String[index];
        HashMap<String, String> map = new HashMap<>();
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
        Log.d("event",""+data);
        int index = 4;
        int i = 0;
        boolean IsPlay = false;
        String[] str = new String[index];
        HashMap<String, String> map = new HashMap<>();
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
        if(toggleSongLikeAnimButton())  {
            heart.setText(Integer.toString(newheart));
        }
        if(newheart % 100 == 0){
            AlarmPlayer(newheart+"회 돌파~", 2);
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
        }

        @Override
        public void loadInitialMessage(String type, String data){
            if(type.equals("chat")){
                mExampleChatController.add(data);
            }else if(type.equals(("notice"))){
//                notify.setText(data);
                setReadMore(notify, data, 2);
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
            mMessageEditText.setText("");
        }

    };


    public static void setReadMore(final TextView view, final String text, final int maxLine) {
        final Context context = view.getContext();
        final String expanedText = "더보기";

        if (view.getTag() != null && view.getTag().equals(text)) { //Tag로 전값 의 text를 비교하여똑같으면 실행하지 않음.
            return;
        }
        view.setTag(text); //Tag에 text 저장
        view.setText(text); // setText를 미리 하셔야  getLineCount()를 호출가능
        view.post(new Runnable() { //getLineCount()는 UI 백그라운드에서만 가져올수 있음
            @Override
            public void run() {
                if (view.getLineCount() >= maxLine) { //Line Count가 설정한 MaxLine의 값보다 크다면 처리시작

                    int lineEndIndex = view.getLayout().getLineVisibleEnd(maxLine - 1); //Max Line 까지의 text length

                    String[] split = text.split("\n"); //text를 자름
                    int splitLength = 0;

                    String lessText = "";
                    for (String item : split) {
                        splitLength += item.length() + 1;
                        if (splitLength >= lineEndIndex) { //마지막 줄일때!
                            if (item.length() >= expanedText.length()) {
                                lessText += item.substring(0, item.length() - (expanedText.length())) + expanedText;
                            } else {
                                lessText += item + expanedText;
                            }
                            break; //종료
                        }
                        lessText += item + "\n";
                    }
                    SpannableString spannableString = new SpannableString(lessText);
                    spannableString.setSpan(new ClickableSpan() {//클릭이벤트
                        @Override
                        public void onClick(View v) {
                            view.setText(text);
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) { //컬러 처리
                            ds.setColor(ContextCompat.getColor(context, R.color.blue));
                        }
                    }, spannableString.length() - expanedText.length(), spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    view.setText(spannableString);
                    view.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
        });
    }

}
