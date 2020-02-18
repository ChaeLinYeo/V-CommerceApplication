package com.pedro.rtpstreamer.player;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.utils.CustomViewPager;
import com.pedro.rtpstreamer.utils.SectionPageAdapter;
import com.pedro.rtpstreamer.utils.FragmentListener;

import java.util.ArrayList;

public class FullVideoFragment extends Fragment {

    private String TAG = "Full";

    private CustomViewPager viewPager;
    private SectionPageAdapter adapter;

    private ArrayList<String> mPlayList;
    private ArrayList<String> mPlaypreviewList;

    private int curFragment = -1;
    private FragmentListener fragmentListener;

    FullVideoFragment(ArrayList<String> mPlayList, ArrayList<String> mPlaypreviewList, FragmentListener fragmentListener1){ //package-private
        this.mPlayList = mPlayList;
        this.mPlaypreviewList = mPlaypreviewList;
        this.fragmentListener = fragmentListener1;
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
        adapter.clearFragment();
        for(int i = 0 ;i < mPlayList.size();i++) {
            adapter.addFragment(new Fragment_player(i), "" + i);
        }

        viewPager.setAdapter(adapter);
    }

    void startFull(int position){ //package private
        Log.d(TAG,"startFull : "+position);
        this.curFragment = position;
        viewPager.postDelayed(() -> {
            fragmentListener.loadComplete();
            Log.d("startFull","run viewPager");
            viewPager.setCurrentItem(curFragment);
            playStart(curFragment, mPlayList.get(curFragment), mPlaypreviewList.get(curFragment));
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

}
