package com.pedro.rtpstreamer.player;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

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
import com.pedro.rtpstreamer.utils.CustomViewPager;
import com.pedro.rtpstreamer.utils.SectionPageAdapter;
import com.pedro.rtpstreamer.utils.fragmentListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FullVideoFragment extends Fragment
    implements fragmentListener {

    private String TAG = "Full";

    private CustomViewPager viewPager;
    private SectionPageAdapter adapter;

    private ArrayList<String> mPlayList;
    private ArrayList<String> mPlaypreviewList;
    private ArrayList<Integer> mPlayChannel;
    private ArrayList<String> mChatUrl;

    private int curFragment = -1;
    private int comFragment = 0;
    private boolean createAllFrag = false;

    FullVideoFragment(ArrayList<String> mPlayList, ArrayList<String> mPlaypreviewList, ArrayList<Integer> mPlayChannel, ArrayList<String> mChatUrl){ //package-private
        this.mPlayList = mPlayList;
        this.mPlaypreviewList = mPlaypreviewList;
        this.mPlayChannel = mPlayChannel;
        this.mChatUrl = mChatUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.d(TAG,"onCreateView");
        View view = inflater.inflate(R.layout.main_layout, container, false);
        viewPager = view.findViewById(R.id.viewPager);
        adapter = new SectionPageAdapter(getChildFragmentManager());
        initViewPager();
        return view;
    }

    private void initViewPager(){
        Log.d(TAG,"initViewPager");
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                Log.d("viewPager","onPageScrolled " + i);
            }

            @Override
            public void onPageSelected(int i) {
                Log.d("viewPager","onPageSelected " + i);
//                if(!createAllFrag) return;
                if(mPlayList.size() > i) {
                    playStart(i, mPlayList.get(i), mPlaypreviewList.get(i));
                    curFragment = i;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                Log.d("viewPager","onPageScrollStateChanged " + i);
            }
        });

        //init adapter
        comFragment = 0;
        createAllFrag = false;
        adapter.clearFragment();
        for(int i = 0 ;i < mPlayList.size();i++) {
            adapter.addFragment(new Fragment_player(i, mPlayChannel.get(i), mChatUrl.get(i)), "" + i);
        }

        viewPager.setAdapter(adapter);
    }

    void startFull(int position){ //package private
        Log.d(TAG,"startFull : "+position);
        this.curFragment = position;
        viewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                {
                    Log.d("startFull","run viewPager");
                    viewPager.setCurrentItem(curFragment);
                    playStart(curFragment, mPlayList.get(curFragment), mPlaypreviewList.get(curFragment));
                }
            }
        }, 100);
    }

    private void playStart(final int position, final String resourceUri,final String previewUri) {
        Log.d(TAG,"playStart");
        Fragment_player fp = (Fragment_player)(adapter.getFragmentList().get(position));
        if(fp==null) {
            return;
        }
        fp.playStart(resourceUri, getString(R.string.application_id), previewUri);
    }

    void closeFull(){ //package-private
        Fragment_player fp = (Fragment_player)(adapter.getFragmentList().get(this.curFragment));
        fp.closeBroadcast();
    }

    @Override
    public void createComplete(int numFrag){
//        Log.d("create",""+numFrag+"/"+curFragment);
//        if(numFrag == curFragment) {
//            createAllFrag = true;
//            Log.d("complete","complete");
//            playStart(curFragment, mPlayList.get(curFragment), mPlaypreviewList.get(curFragment));
//        }
    }

    @Override
    public void popUp(View view){

    }

}
