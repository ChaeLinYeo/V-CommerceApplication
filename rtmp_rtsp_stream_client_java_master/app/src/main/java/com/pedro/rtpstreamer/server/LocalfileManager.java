package com.pedro.rtpstreamer.server;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class LocalfileManager {

    private BufferedWriter writer;
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
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName),"UTF8"));
        } catch(FileNotFoundException e){
            Log.e("error",""+e.getMessage());
        } catch (UnsupportedEncodingException e) {
            Log.e("error2", e.getMessage());
            e.printStackTrace();
        }
    }

    public void savechat(long time, String data){
        try{
            String chatlog = time+"/chat/"+data+"\n";
            writer.append(chatlog);
            writer.flush();
        } catch(IOException e){
            Log.d("eror",""+e.getMessage());
        }
    }

    public void saveheart(long time){
        try{
            String likelog = time+"/like\n";
            writer.append(likelog);
            writer.flush();
        } catch(IOException e){
            Log.d("eror",""+e.getMessage());
        }
    }

    public void savetimeline(long time, String data){
        try{
            writer.append(time+"/"+data);
            writer.flush();
            Log.d("PKRRR","write timeline");
        }catch(IOException e){
            Log.d("error",""+e.getMessage());
        }
    }
    public void savetitle(long time, String title){
        try{
            writer.append(time +"/title/"+title +"\n");
            writer.flush();
        }catch(IOException e){
            Log.d("error",""+e.getMessage());
        }
    }

    public void savefinal(long time, String data, String type){
        try{
            String fh = time+"/" + type +"/"+ data+"\n";
            writer.write(fh);
            writer.flush();
        }catch(IOException e){
            Log.d("error",""+e.getMessage());
        }
    }

    public void LMEnd(){
        try {
            writer.close();
        } catch(IOException e){
            Log.d("eror",""+e.getMessage());
        }
    }

    public String getFileName(){
        return fileName;
    }
}