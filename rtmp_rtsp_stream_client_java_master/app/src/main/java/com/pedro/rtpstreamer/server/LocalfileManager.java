package com.pedro.rtpstreamer.server;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class LocalfileManager {

    private BufferedWriter writer;
    private FileOutputStream output;

    private String fileName;

    public LocalfileManager(String filename){
        try {
            Log.d("createLM", Environment.getExternalStorageDirectory().getAbsolutePath());
            String foldername = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Chatlog";
            File dir = new File(foldername);
            if (!dir.exists()) {
                dir.mkdir();
            }
            fileName = foldername + "/" + filename;
            output = new FileOutputStream(fileName);
            writer = new BufferedWriter(new OutputStreamWriter(output));
        } catch(FileNotFoundException e){
            Log.e("error",""+e.getMessage());
        }
    }

    public void savechat(String data){
        try{
            String chatlog = System.currentTimeMillis()+"/chat/"+data+"\n";
            writer.append(chatlog);
            writer.flush();
        } catch(IOException e){
            Log.d("eror",""+e.getMessage());
        }
    }

    public void saveheart(){
        try{
            String likelog = System.currentTimeMillis()+"/like\n";
            writer.append(likelog);
            writer.flush();
        } catch(IOException e){
            Log.d("eror",""+e.getMessage());
        }
    }

    public void savealarm(String data){
        try {
            String alarmlog = System.currentTimeMillis() + "/alarm/" + data +"\n";
            writer.append(alarmlog);
            writer.flush();
        } catch(IOException e){
            Log.d("eror",""+e.getMessage());
        }
    }
    
    public void savetimeline(String data){
        try{
            writer.append(data);
            writer.flush();
        }catch(IOException e){
            Log.d("eror",""+e.getMessage());
        }
    }

    public void LMEnd(){
        try {
            writer.close();
            output.close();
        } catch(IOException e){
            Log.d("eror",""+e.getMessage());
        }
    }

    public String getFileName(){
        return fileName;
    }
}