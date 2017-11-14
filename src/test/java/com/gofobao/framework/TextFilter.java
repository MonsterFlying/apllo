package com.gofobao.framework;

import java.io.*;

/**
 * Created by xin on 2017/11/13.
 */
public class TextFilter {
    public static void main(String[] args) {
        try {
            BufferedReader bufferedInputStream = new BufferedReader(new FileReader("C:\\Users\\xin\\Desktop\\words2.txt"));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("C:\\Users\\xin\\Desktop\\words.txt"));
            String s = bufferedInputStream.readLine();
            int a=0;
            while(s!=null){
                a = s.indexOf('=');
                System.out.println(s.substring(0,a));
                bufferedWriter.write(s.substring(0,a)+"=1");
                bufferedWriter.newLine();
                s = bufferedInputStream.readLine();
            }
            bufferedWriter.flush();
            bufferedWriter.close();
            bufferedInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
