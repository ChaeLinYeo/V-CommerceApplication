package com.pedro.rtpstreamer.utils;

import android.content.Context;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;

public class Data {
    private static final Data ourInstance = new Data();

    public static Data getInstance() {
        return ourInstance;
    }

    private Context mContext;

    private Data() {
    }

    public void setContext(Context context){
        mContext = context;
    }

    public void setAws(){
        AWSMobileClient.getInstance().initialize(mContext, new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                try {
                    Amplify.addPlugin(new AWSS3StoragePlugin());
                    Amplify.configure(mContext);
                    Log.i("StorageQuickstart", "All set and ready to go!");
                } catch (Exception e) {
                    Log.e("StorageQuickstart", e.getMessage());
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("StorageQuickstart", "Initialization error.", e);
            }
        });
    }
}
