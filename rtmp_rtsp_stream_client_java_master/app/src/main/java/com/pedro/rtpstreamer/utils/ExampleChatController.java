package com.pedro.rtpstreamer.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

//import com.sendbird.android.BaseChannel;
//import com.sendbird.android.BaseMessage;
//import com.sendbird.android.SendBirdException;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ExampleChatController {

//    TextView txt_test01;
//    final String newnoti="";
    // heart temp
//    int new_heart;

//    public void msgfilter(BaseChannel baseChannel, BaseMessage baseMessage){
//        String Custom_Type = baseMessage.getCustomType();
//        String Data = baseMessage.getData();
//        ExamplePlayerActivity ex = new ExamplePlayerActivity();
//        switch(Custom_Type) {
//            case "notification":
//                add2(Data);
//                break;
//            case "alarm":
//                add2(Data);
//                break;
//            case "chat" :
//                add(Data);
//                break;
//            case "event" :
//                //방송자가 이벤트를 시작하겠다고 이벤트버트을 눌렀을때?(송출부 시작)
//                //타입(Type)을 설정하고, 내용(이벤트 정보, 지속시간 : text1)을 정해서 보내면
//                // ex. "eventonoff=on\nType=Pop\nTimeLimit=1:20:30\nEventInfo=AB23:PlaneText";
//                ReceiveMessage.Event(Data);
//                break;
//            case "like" :
//                // 다른 사용자가 하트를 눌렀을 경우
//                List<String> keys = new ArrayList<String>();
//                keys.add("heart");
//                baseChannel.getMetaCounters(keys, new BaseChannel.MetaCounterHandler() {
//                    @Override
//                    public void onResult(Map<String, Integer> map, SendBirdException e) {
//                        new_heart = map.get("heart");
//                    }
//                });
//                // **new_heart 반영 및 애니메이션 재생
//                ex.LikePlayer(new_heart);
//                break;
//            case "effect" :
//                // 방송자가 이펙트를 눌렀을 경우 (송출부시작)
//                // effecturl의 경우 방송자가 선택한 것의 url을 받음
//                // ex. "effectonoff=off\neffecturl=http://naver.com"
//                ReceiveMessage.Effect(Data);
//                break;
//            case "setting" :
//                // 방송자가 방송공지를 변경했을 경우(송출부 시작)
//                //String text_s = "sth_setting";
//                ReceiveMessage.Setting(Data);
//                //ExamplePlayerActivity.
//                break;
//            case "category" :
//                ReceiveMessage.Category(Data);
//                add(Data);
//                break;
//            default :
//                break;
//        }
//    }

    public ExampleChatController(Context context, ListView chatListView, int layoutRes, final int textRes, final int timeRes) {
        mChatAdapter = new ChatLineArrayAdapter(context, layoutRes, textRes, timeRes);
        mChatListView = chatListView;
        mChatListView.setAdapter(mChatAdapter);
    }

    public void add(final String msg) {
        mChatAdapter.add(new ChatLine(DateFormat.getTimeInstance().format(new Date()),msg));
    }
    public void add2(final String msg) {
        mChatAdapter.add(new ChatLine(null,msg));
    }
    public void show() {
        mChatListView.setVisibility(View.VISIBLE);
    }
    public void hide() {
        mChatListView.setVisibility(View.GONE);
    }

    public boolean hasMessages() {
        return !mChatAdapter.isEmpty();
    }

    public boolean isShown() {
        return mChatListView.isShown();
    }

    private static final class ChatLine {
        ChatLine(String time, String text) {
            mTime = time;
            mText = text;
        }
        @Override
        public String toString() {
            return mText;
        }
        public final String mTime;
        public final String mText;
    }

    private static final class ChatLineArrayAdapter extends ArrayAdapter<ChatLine> {
        public ChatLineArrayAdapter(Context context, int resource, int textViewResourceId, int timeViewResourceId) {
            super(context, resource, textViewResourceId);
            mTimeViewResourceId = timeViewResourceId;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View chatLineView = super.getView(position, convertView, parent);
            if (mTimeViewResourceId != 0){
                ((TextView)chatLineView.findViewById(mTimeViewResourceId)).setText(getItem(position).mTime);
            }

            return chatLineView;
        }
        private final int mTimeViewResourceId;
    }

    private final ChatLineArrayAdapter mChatAdapter;
    private final ListView mChatListView;
}