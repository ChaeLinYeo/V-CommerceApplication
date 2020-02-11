package com.pedro.rtpstreamer.server;

import android.os.Environment;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class AWSfileManager {

    private BufferedReader reader;
    private String fileName;

    public AWSfileManager(String filename){
        try {
            String foldername = Environment.getExternalStorageDirectory().getAbsolutePath() + "/chatDown";
            fileName = foldername + "/" + filename;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName),"UTF8"));
        } catch(FileNotFoundException e){
            Log.e("error",""+e.getMessage());
        } catch (UnsupportedEncodingException e) {
            Log.e("error2", e.getMessage());
            e.printStackTrace();
        }
    }

    public ArrayList<String> setTL() throws IOException {
        String d = reader.readLine();
        ArrayList<String> al = new ArrayList<>();
        while(d != null){
            al.add(d);
            d = reader.readLine();
        }
        return al;
    }

    public ArrayList<String> setSL() throws IOException {
        String Heart = reader.readLine();
        StringTokenizer st = new StringTokenizer(Heart);
        String d = reader.readLine();
        ArrayList<String> al = new ArrayList<>();
        while(d != null){
            al.add(d);
            d = reader.readLine();
        }
        return al;
    }

    public void End(){
        try {
            reader.close();
        } catch(IOException e){
            Log.d("eror",""+e.getMessage());
        }
    }


}
