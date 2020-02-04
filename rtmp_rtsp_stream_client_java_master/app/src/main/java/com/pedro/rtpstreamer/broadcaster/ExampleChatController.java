package com.pedro.rtpstreamer.broadcaster;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.PreviousMessageListQuery;
import com.sendbird.android.SendBirdException;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ExampleChatController {

	private PreviousMessageListQuery mPrevMessageListQuery;
	private final ChatLineArrayAdapter mChatAdapter;
	private final ListView mChatListView;
	private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHAT";

	// heart temp
	int new_heart;

	public ExampleChatController(Context context, ListView chatListView, int layoutRes, final int textRes, final int timeRes) {
		mChatAdapter = new ChatLineArrayAdapter(context, layoutRes, textRes, timeRes);
		mChatListView = chatListView;
		mChatListView.setAdapter(mChatAdapter);
	}

	/*
	 * Loads messages and adds them to current message list.
	 * <p>
	 * A PreviousMessageListQuery must have been already initialized through {@link #loadInitialMessageList(int)}
	 */
	/*
	private void loadNextMessageList(int numMessages, OpenChannel mChannel) throws NullPointerException {

		if (mChannel == null) {
			throw new NullPointerException("Current channel instance is null.");
		}

		if (mPrevMessageListQuery == null) {
			throw new NullPointerException("Current query instance is null.");
		}

		mPrevMessageListQuery.load(numMessages, true, new PreviousMessageListQuery.MessageListQueryResult() {
			@Override
			public void onResult(List<BaseMessage> list, SendBirdException e) {
				if (e != null) {
					// Error!
					e.printStackTrace();
					return;
				}

				for (BaseMessage message : list) {
					mChatAdapter.add(message);
				}
			}
		});
	}*/
	public void msgfilter(BaseChannel baseChannel, BaseMessage baseMessage){
		String Custom_Type = baseMessage.getCustomType();
		String Data = baseMessage.getData();
		BroadcastMain ex = new BroadcastMain();
		switch(Custom_Type) {
			case "notification":
				// ex. "type=Follow\nuser=user\nproduct=null"
				// ex. "type=Buy\nuser=user\nproduct=apple"
				add2(Data);
				// ** 채팅창 위에 나타낼 문장
				ex.AlarmPlayer(Data);
			break;
			case "chat" :
				//채팅 send버튼을 눌렀을 경우(수신부 시작)
				//사용자가 친 채팅 : Data에 담긴 모든 것
				// ex. customType=chat\nData=안녕하소!
				add(Data);
			break;
			case "like" :
				// 다른 사용자가 하트를 눌렀을 경우
				List<String> keys = new ArrayList<String>();
				keys.add("heart");
				baseChannel.getMetaCounters(keys, new BaseChannel.MetaCounterHandler() {
					@Override
					public void onResult(Map<String, Integer> map, SendBirdException e) {
						new_heart = map.get("heart");
					}
				});
				// **new_heart 반영 및 애니메이션 재생
				ex.LikePlayer(new_heart);
				//ExamplePlayerActivity.toggleSongLikeAnimButton();
			break;
			case "effect" :
				// 방송자가 이펙트를 눌렀을 경우 (송출부시작)
				// effecturl의 경우 방송자가 선택한 것의 url을 받음
				// ex. "effectonoff=off\neffecturl=http://naver.com"
				RecieveMessage.Effect(Data);
				//ex.LikePlayer();
				break;
            default :
            	add(Data);
				break;
		}
	}
	public void add(final String msg) {
		mChatAdapter.add(new ChatLine(DateFormat.getTimeInstance().format(new Date()), msg));
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
			if (mTimeViewResourceId != 0)
				((TextView)chatLineView.findViewById(mTimeViewResourceId)).setText(getItem(position).mTime);
			return chatLineView;
		}
		private final int mTimeViewResourceId;

	}


}
