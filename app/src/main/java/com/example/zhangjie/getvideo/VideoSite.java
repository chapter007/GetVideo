package com.example.zhangjie.getvideo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangjie on 2015/1/29.
 */

public class VideoSite extends ToolBar{

    private String url;
    private String VideoName;
    private WebView video_site;
    private ProgressBar progressBar;
    private FloatingActionButton mFAB;
    private SharedPreferences preferences;
    private boolean quality=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableHomeButton();
        progressBar= (ProgressBar) findViewById(R.id.progress);
        video_site= (WebView) findViewById(R.id.video_site);
        preferences= PreferenceManager.getDefaultSharedPreferences(VideoSite.this);
        quality=preferences.getBoolean("quality",false);
        video_site.getSettings().setJavaScriptEnabled(true);
        Intent intent=getIntent();
        String ku6 = intent.getStringExtra("ku6");
        String youku = intent.getStringExtra("youku");
        String tudou = intent.getStringExtra("tudou");

        if (ku6!=null){
            video_site.loadUrl(ku6);
            Toast.makeText(VideoSite.this,"ku6貌似有些问题(其实这个是凑数的)",Toast.LENGTH_SHORT).show();
        }else if (youku !=null){
            video_site.loadUrl(youku);
        }else if (tudou !=null){
            video_site.loadUrl(tudou);
        }

        mFAB=new FloatingActionButton.Builder(this)
                .withButtonColor(getResources().getColor(R.color.blue))
                .withButtonSize(80)
                .withDrawable(getResources().getDrawable(R.drawable.ic_file_download_white))
                .withGravity(Gravity.END | Gravity.BOTTOM)
                .withPaddings(0, 0, 10, 10)
                .create();
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getVideo(url);
            }
        });

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.video_site;
    }

    public void onBackPressed() {
        if (video_site.canGoBack()) {
            video_site.goBack();
        } else {
            finish();
        }
    }

    public void getVideo(String url){
        String FLVCD="http://www.flvcd.com/parse.php?format=&kw=";
        FLVCD=FLVCD+url;
        Log.i("FLVCD",FLVCD);
        new VideoParse().execute(FLVCD);
    }

    private ProgressDialog progressDialog;
    private ArrayList<String> downloadUrl=new ArrayList<String>();

    public class VideoParse extends AsyncTask<String,Integer,String>{

        @Override
        protected void onPreExecute() {
            progressDialog=ProgressDialog.show(VideoSite.this,"正在解析地址","请稍等");
            progressDialog.setCancelable(true);
            Log.i("onpreexecute","做准备");
            super.onPreExecute();
        }

        int[] flag=new int[3];
        @Override
        protected String doInBackground(String... strings) {
            Log.i("doinbackground","正在解析链接"+strings[0]);
            ParseUrl mParseUrl = new ParseUrl();
            String[] result = new String[3];
            result[0]=mParseUrl.parseUrl(strings[0]);
            if (result[0].contains("高清版")){
                Log.i("此视频可下载更高画质","");
                flag[1]=1;
            }if (result[0].contains("超清版")) flag[2]=2;
            Log.i("flag",""+flag);
            if (flag[1]==1) result[1]=mParseUrl.parseUrl(strings[0]+"&format=high");
            if (flag[2]==2) result[2]=mParseUrl.parseUrl(strings[0]+"&format=super");
            if (quality){
                try {
                    VideoName=mParseUrl.Matcher(result[1],0);
                    if (result[2]==null) downloadUrl=mParseUrl.Matcher(result[1]);
                    else downloadUrl=mParseUrl.Matcher(result[2]);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }else try {
                VideoName=mParseUrl.Matcher(result[0],0);
                downloadUrl=mParseUrl.Matcher(result[0]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i("onpostexecute","结束了");
            progressDialog.dismiss();
            progressDialog=null;
            if (downloadUrl.size()==0) {
                Toast.makeText(VideoSite.this,"请在播放页面获取下载链接",Toast.LENGTH_LONG).show();
            }else if (downloadUrl.get(0).contains("温馨提示")){
                Toast.makeText(VideoSite.this,"由于特殊原因，没能获取到下载链接",Toast.LENGTH_LONG).show();
            }
            else {
                Intent intent=new Intent(VideoSite.this,DownLoad.class);
                Bundle bundle=new Bundle();
                bundle.putStringArrayList("list",downloadUrl);
                bundle.putString("videoname", VideoName);
                intent.putExtras(bundle);
                startActivity(intent);
            }
            super.onPostExecute(s);
        }

    }

    @Override
    protected void onResume() {
        Log.i("onResume","resume?");
        url="";
        video_site.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String _url) {
                Log.i("mywebview","myweb"+_url);
                view.loadUrl(_url);
                url=_url;
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setProgress(0);
                super.onPageFinished(view, url);
            }

        });

        video_site.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                super.onProgressChanged(view, newProgress);
            }

        });

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.get_video,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;

            case R.id.get_button:
                Log.i("url",url);
                video_site.reload();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
