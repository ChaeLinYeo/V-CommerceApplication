package com.pedro.rtpstreamer.player;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bambuser.broadcaster.BroadcastPlayer;
import com.bambuser.broadcaster.PlayerState;
import com.bambuser.broadcaster.SurfaceViewWithAutoAR;
import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.utils.fragmentListener;
import com.squareup.picasso.Picasso;

public class Fragment_player extends Fragment {
    private static String TAG = "Fragment_player";
    private static String TAG1 = "Frag1";

    private BroadcastPlayer mBroadcastPlayer;

    private TextView liveTextView;
    private ImageView img_preview;
    private SurfaceViewWithAutoAR mVideoSurfaceView;

    private int numFrag;

    private fragmentListener cL;

    public Fragment_player(int numFrag){
        this.numFrag = numFrag;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Log.d(TAG1, "onAttach");

        try {
            cL = (fragmentListener) getParentFragment();
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.d(TAG1, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        liveTextView = view.findViewById(R.id.BroadcastLiveTextView);
        mVideoSurfaceView = view.findViewById(R.id.VideoSurfaceView);
        img_preview = view.findViewById(R.id.img_preview);
        cL.createComplete(numFrag);
        return view;
    }

    public void closeBroadcast(){
        mBroadcastPlayer.close();
    }

    private SurfaceViewWithAutoAR getVideoSurfaceView(){
        return mVideoSurfaceView;
    }

    public void playStart(String resourceUri,String id, final String previewUri){ //package private
        Log.d(TAG1, "playStart");
        Picasso.with(getActivity()).load(previewUri).into(img_preview);
        img_preview.setVisibility(View.VISIBLE);
        if (mBroadcastPlayer != null) mBroadcastPlayer.close();

        mBroadcastPlayer = null;
        mBroadcastPlayer = new BroadcastPlayer(getContext(), resourceUri, id, mPlayerObserver);

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
}
