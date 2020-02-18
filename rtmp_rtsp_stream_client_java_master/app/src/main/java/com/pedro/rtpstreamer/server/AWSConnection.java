package com.pedro.rtpstreamer.server;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.ResultListener;
import com.amplifyframework.storage.result.StorageDownloadFileResult;
import com.amplifyframework.storage.result.StorageUploadFileResult;

import java.io.File;

public class AWSConnection {

    private static AWSListner awsListner = null;

    public static void uploadFile(String fileName, String localPath) {
        if(awsListner != null) awsListner.startUpload();
        Log.d("upload", ""+localPath);
        Amplify.Storage.uploadFile(
            fileName, localPath,
            new ResultListener<StorageUploadFileResult>() {
                @Override
                public void onResult(StorageUploadFileResult result) {
                    Log.i("StorageQuickStart", "Successfully uploaded: " + result.getKey());
                    if(awsListner != null) awsListner.uploadComplete(true);
                }

                @Override
                public void onError(Throwable error) {
                    Log.e("StorageQuickstart", "Upload error.", error);
                    if(awsListner != null) awsListner.uploadComplete(false);
                }
            }
        );
    }

    public static void downloadFile(String fileName, String localPath) {

        String key = "test/"+fileName+".txt";

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
                    Log.e("StorageQuickStart", error.getMessage());
                }
            }
        );
    }

    public static void setAwsListner(AWSListner awsListner1){
        awsListner = awsListner1;
    }
}
