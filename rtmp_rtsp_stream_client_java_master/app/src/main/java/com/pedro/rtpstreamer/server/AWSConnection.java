package com.pedro.rtpstreamer.server;

import android.content.Context;
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

    public static void uploadFile(String fileName, String localPath, Context context) {
        Log.d("upload", ""+localPath);
//        Amplify.Storage.uploadFile(
//            fileName, localPath,
//            new ResultListener<StorageUploadFileResult>() {
//                @Override
//                public void onResult(StorageUploadFileResult result) {
//                    Log.i("StorageQuickStart", "Successfully uploaded: " + result.getKey());
//                }
//
//                @Override
//                public void onError(Throwable error) {
//                    Log.e("StorageQuickstart", "Upload error.", error);
//                }
//            }
//        );

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(context)
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentials()))
                        .build();

        TransferObserver uploadObserver =
                transferUtility.upload(
                        "public/"+fileName,
                        new File(localPath));

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("YourActivity", "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
                Log.e("error", ""+ ex.getMessage());
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }

        Log.d("YourActivity", "Bytes Transferred: " + uploadObserver.getBytesTransferred());
        Log.d("YourActivity", "Bytes Total: " + uploadObserver.getBytesTotal());
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
