package com.example.zhangjie.getvideo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by zhangjie on 2015/3/17.
 */
public class SiteNoJs extends ToolBar{

    private WebView siteNojs;
    private String url,VideoName;
    private ProgressBar mProgress;
    private boolean quality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableHomeButton();
        siteNojs= (WebView) findViewById(R.id.site_nojs);
        mProgress= (ProgressBar) findViewById(R.id.mprogress);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SiteNoJs.this);
        quality= preferences.getBoolean("quality", false);
        Intent Nojs=getIntent();
        url=Nojs.getStringExtra("soku");
        siteNojs.loadUrl(url);

        FloatingActionButton mFAB = new FloatingActionButton.Builder(this)
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

    public void getVideo(String url){
        String FLVCD="http://www.flvcd.com/parse.php?format=&kw=";
        FLVCD=FLVCD+url;
        Log.i("FLVCD",FLVCD);
        new VideoParse().execute(FLVCD);
    }

    private ProgressDialog progressDialog;
    private ArrayList<String> downloadUrl=new ArrayList<String>();

    public class VideoParse extends AsyncTask<String,Integer,String> {

        @Override
        protected void onPreExecute() {
            progressDialog=ProgressDialog.show(SiteNoJs.this,"正在解析地址","请稍等");
            progressDialog.setCancelable(true);
            Log.i("onPreExecute","做准备");
            super.onPreExecute();
        }

        int[] flag=new int[3];
        @Override
        protected String doInBackground(String... strings) {
            Log.i("doInBackground","正在解析链接"+strings[0]);
            ParseUrl mParseUrl = new ParseUrl();
            String[] result = new String[3];
            result[0]=mParseUrl.parseUrl(strings[0]);
            if (result[0].contains("高清版")){
                Log.i("此视频可下载更高画质","");
                flag[1]=1;
            }if (result[0].contains("超清版")) flag[2]=2;
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
            Log.i("onPostExecute","结束了");
            progressDialog.dismiss();
            progressDialog=null;
            if (downloadUrl.size()==0) {
                Toast.makeText(SiteNoJs.this, "请在播放页面获取下载链接", Toast.LENGTH_LONG).show();
            }else if (downloadUrl.get(0).contains("温馨提示")){
                Toast.makeText(SiteNoJs.this,"由于特殊原因，没能获取到下载链接",Toast.LENGTH_LONG).show();
            }
            else {
                Intent intent=new Intent(SiteNoJs.this,DownLoad.class);
                Bundle bundle=new Bundle();
                bundle.putStringArrayList("list",downloadUrl);
                bundle.putString("VideoName", VideoName);
                intent.putExtras(bundle);
                startActivity(intent);
            }
            super.onPostExecute(s);
        }

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.site_nojs;
    }

    public void onBackPressed() {
        if (siteNojs.canGoBack()) {
            siteNojs.goBack();
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        Log.i("onResume", "resume?");
        url="";
        siteNojs.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String _url) {
                Log.i("mywebview","myweb"+_url);
                view.loadUrl(_url);
                url=_url;
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mProgress.setProgress(0);
                super.onPageFinished(view, url);
            }

        });

        siteNojs.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mProgress.setProgress(newProgress);
                super.onProgressChanged(view, newProgress);
            }

        });

        super.onResume();
    }

}
