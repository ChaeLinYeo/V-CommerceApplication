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