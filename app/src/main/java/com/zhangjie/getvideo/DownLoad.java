package com.zhangjie.getvideo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.zhangjie.downloader.ConfigUtils;
import com.zhangjie.downloader.DownloadListAdapter;
import com.zhangjie.downloader.MyIntents;
import com.zhangjie.downloader.StorageUtils;
import com.zhangjie.downloader.TextUtils;
import com.zhangjie.downloader.ViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangjie on 2015/1/31.
 */
public class DownLoad extends ToolBar{
    private ListView downloadList;
    private DownloadListAdapter downloadListAdapter;
    private MyReceiver mReceiver;
    public String video_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableHomeButton();
        Intent intent=getIntent();
        video_name=intent.getStringExtra("VideoName");
        final ArrayList url;
        url=intent.getStringArrayListExtra("list");
        ConfigUtils.setVideoName(this,"VideoName",video_name);
        downloadList = (ListView) findViewById(R.id.download_list);
        downloadListAdapter = new DownloadListAdapter(this);
        downloadList.setAdapter(downloadListAdapter);
        if (!StorageUtils.isSDCardPresent()) {
            Toast.makeText(this, "未发现SD卡", Toast.LENGTH_LONG).show();
            return;
        }
        if (!StorageUtils.isSdCardWrittenable()) {
            Toast.makeText(this, "SD卡不能读写", Toast.LENGTH_LONG).show();
            return;
        }

        if (url!=null&&!fileIsExists()){
            for (int i=0;i<url.size();i++){
                String url_tmp= (String) url.get(i);
                Log.i("downloadUrl",""+url_tmp);
                startDownload(url_tmp);
            }
        }

        /*Intent downloadIntent = new Intent("com.zhangjie.downloader.IDownloadService");
        downloadIntent.putExtra(MyIntents.TYPE, MyIntents.Types.START);
        startService(downloadIntent);*/

        mReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.zhangjie.Download");
        registerReceiver(mReceiver, filter);
        Log.i("receiver:",""+mReceiver);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.download;
    }


    public void startDownload(String url){
        Log.i("step-1", "get intent");
        Intent downloadIntent = new Intent("com.zhangjie.downloader.IDownloadService");
        downloadIntent.putExtra(MyIntents.TYPE, MyIntents.Types.ADD);
        downloadIntent.putExtra(MyIntents.URL, url);
        Intent Lintent=new Intent(createExplicitFromImplicitIntent(DownLoad.this,downloadIntent));
        startService(Lintent);
    }

    /*public boolean dirIsNotNull(){
        File dir=new File("/VideoGet/");
        if (dir.exists()&&dir.isDirectory()){
            if (dir.list().length>0) return true;
            else return false;
        }else {
            return false;
        }
    }*/

    public boolean fileIsExists(){
        try{
            File f=new File(Environment.getExternalStorageDirectory().getPath()+
                    "/VideoGet/"+video_name+".flv");
            if(!f.exists()){
                return false;
            }
        }catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            handleIntent(intent);//可以接受下载进度来更新ui
        }

        private void handleIntent(Intent intent) {

            if (intent != null
                    && intent.getAction().equals(
                    "com.example.zhangjie.Download")) {
                int type = intent.getIntExtra(MyIntents.TYPE, -1);
                String url;

                switch (type) {
                    case MyIntents.Types.ADD:
                        url = intent.getStringExtra(MyIntents.URL);
                        boolean isPaused = intent.getBooleanExtra(MyIntents.IS_PAUSED, false);
                        if (!TextUtils.isEmpty(url)) {
                            downloadListAdapter.addItem(url, isPaused);
                        }
                        break;
                    case MyIntents.Types.COMPLETE:
                        url = intent.getStringExtra(MyIntents.URL);
                        if (!TextUtils.isEmpty(url)) {
                            downloadListAdapter.removeItem(url);
                        }
                        break;
                    case MyIntents.Types.PROCESS:
                        url = intent.getStringExtra(MyIntents.URL);
                        View taskListItem = downloadList.findViewWithTag(url);
                        ViewHolder viewHolder = new ViewHolder(taskListItem,DownLoad.this);
                        viewHolder.setData(url, intent.getStringExtra(MyIntents.PROCESS_SPEED),
                                intent.getStringExtra(MyIntents.PROCESS_PROGRESS));
                        break;
                    case MyIntents.Types.ERROR:
                        url = intent.getStringExtra(MyIntents.URL);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // 为了支持5.0的startService
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }
}
