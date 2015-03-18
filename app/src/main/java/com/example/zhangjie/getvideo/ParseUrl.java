package com.example.zhangjie.getvideo;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangjie on 2015/3/17.
 */
public class ParseUrl {

    public ArrayList<String> Matcher(String Result) throws UnsupportedEncodingException {
        ArrayList<String> ls=new ArrayList<String>();
        Result=new String(Result.getBytes("GBK"),"gb2312");
        Pattern pattern;Matcher matcher;
        pattern=Pattern.compile("\\s+<br>.+\\s+?</tr>");
        matcher=pattern.matcher(Result);
        if (matcher.find()){
            String parseResult = matcher.group();
            Log.i("parse", parseResult);
            if (parseResult.contains("自动切割")){
                pattern=Pattern.compile("<BR><a.href=\"(.+?)\".target");
                Matcher matcher1=pattern.matcher(parseResult);
                while (matcher1.find()){
                    Log.i("match-1",""+matcher1.group(1));
                    ls.add(matcher1.group(1));
                }
            }else {
                pattern=Pattern.compile("<br>下载.+?ef=\"(.+?)\".target");
                Matcher matcher2=pattern.matcher(parseResult);
                while (matcher2.find()){
                    Log.i("match-2",matcher2.group(1));
                    ls.add(matcher2.group(1));
                }
            }
        }
        Log.i("list-1",""+ ls);
        return ls;

    }

    public String Matcher(String Result,int i) throws UnsupportedEncodingException {
        String VideoName="";
        Result=new String(Result.getBytes("GBK"),"gb2312");
        Pattern pattern=Pattern.compile("\\s+<strong>\\w+.+?<strong>");
        Matcher matcher=pattern.matcher(Result);
        if (matcher.find()){
            VideoName=matcher.group().replaceAll("<strong>|</strong>","");
            Log.i("videoname", VideoName);
        }
        return VideoName;
    }

    public String parseUrl(String mUrl){
        HttpGet httpGet=new HttpGet(mUrl);
        HttpClient httpClient=new DefaultHttpClient();
        HttpResponse httpResponse;
        HttpEntity httpEntity;
        InputStream inputStream=null;
        try {
            httpResponse=httpClient.execute(httpGet);
            httpEntity=httpResponse.getEntity();
            inputStream=httpEntity.getContent();
            BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream,"GBK"));
            String Result="";
            String line;
            while ((line=reader.readLine())!=null){
                Result=Result+line;
            }
            Log.i("result",Result);
            return Result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*public void getFileFromBytes(String name,String path) {
        byte[] b=name.getBytes();
        BufferedOutputStream stream = null;
        File file = null;
        try {
            file = new File(path);
            FileOutputStream fstream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fstream);
            stream.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }*/
}
