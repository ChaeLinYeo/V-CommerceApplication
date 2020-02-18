package com.pedro.rtpstreamer.server;

import android.util.Log;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.ResultListener;
import com.amplifyframework.storage.result.StorageDownloadFileResult;
import com.amplifyframework.storage.result.StorageUploadFileResult;

public class AWSConnection {

    private static AWSListner awsListner = null;

    public static void uploadFile(String fileName, String localPath) {
        if(awsListner != null) awsListner.startUpload();

        try{
            Amplify.Storage.uploadFile(
                fileName, localPath,
                new ResultListener<StorageUploadFileResult>() {
                    @Override
                    public void onResult(StorageUploadFileResult result) {
                        Log.i("StorageQuickStart", "Successfully uploaded: " + result.getKey());
                        if(awsListner != null) awsListner.uploadComplete(true);
                        LocalfileManager.removeFile(localPath);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e("StorageQuickstart", "Upload error.", error);
                        if(awsListner != null) awsListner.uploadComplete(false);
                    }
                }
            );
        } catch (Exception e){
            Log.e("ERROR",""+e.getMessage());
            e.printStackTrace();
        }
    }

    public static void downloadFile(String fileName, String localPath) {

        String key = "test/"+fileName+".txt";

        try {
            Amplify.Storage.downloadFile(
                key, localPath,
                new ResultListener<StorageDownloadFileResult>() {
                    @Override
                    public void onResult(StorageDownloadFileResult result) {
                        Log.i("StorageQuickStart", "Successfully downloaded: " + result.getFile().getName());
                        awsListner.downloadComplete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e("StorageQuickStart", ""+error.getMessage());
                    }
                }
            );
        } catch (Exception e){
            Log.e("ERROR",""+e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setAwsListner(AWSListner awsListner1){
        awsListner = awsListner1;
    }
}
