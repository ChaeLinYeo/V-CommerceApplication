package com.pedro.rtpstreamer.broadcaster;

import java.util.StringTokenizer;

public class RecieveMessage {
    public static void Effect(String data) {
        int index = 2;
        String[] str = new String[index];
        String[] r_data= new String[index];
        int i = 0;
        StringTokenizer st = new StringTokenizer(data, "\n");
        while(st.hasMoreTokens()) {
            str[i] = st.nextToken();
            i++;
        }
        i = 0;
        while(i < index) {
            StringTokenizer st2 = new StringTokenizer(str[i],"=");
            st2.nextToken();
            r_data[i] = st2.nextToken();
            i++;
        }
        if(r_data[0].equals("off")) {
            //effect 재생 멈춤
        } else {
            //effect 재생 시작

        }
    }
}
