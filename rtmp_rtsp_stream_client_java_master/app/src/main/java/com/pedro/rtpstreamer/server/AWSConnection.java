package com.pedro.rtpstreamer.server;

import android.util.Log;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.ResultListener;
import com.amplifyframework.storage.result.StorageDownloadFileResult;
import com.amplifyframework.storage.result.StorageUploadFileResult;

public class AWSConnection {

    public static void uploadFile(String fileName, String localPath) {
        Amplify.Storage.uploadFile(
            fileName, localPath,
            new ResultListener<StorageUploadFileResult>() {
                @Override
                public void onResult(StorageUploadFileResult result) {
                    Log.i("StorageQuickStart", "Successfully uploaded: " + result.getKey());
                }

                @Override
                public void onError(Throwable error) {
                    Log.e("StorageQuickstart", "Upload error.", error);
                }
            }
        );
    }

    public static void downloadFile(String fileName, String localPath){
        Amplify.Storage.downloadFile(
            fileName, localPath,
            new ResultListener<StorageDownloadFileResult>() {
                @Override
                public void onResult(StorageDownloadFileResult result) {
                    Log.i("StorageQuickStart", "Successfully downloaded: " + result.getFile().getName());
                }

                @Override
                public void onError(Throwable error) {
                    Log.e("StorageQuickStart", error.getMessage());
                }
            }
        );
    }
}
