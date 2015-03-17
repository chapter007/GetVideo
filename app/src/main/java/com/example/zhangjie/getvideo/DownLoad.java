package com.example.zhangjie.getvideo;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Fragment.VideoExplore;

/**
 * Created by zhangjie on 2015/1/31.
 */
public class DownLoad extends ToolBar{
    private DownloadManager downloadManager;
    private DownloadManagerPro downloadManagerPro;
    private SharedPreferences preferences;
    private MyHandler mHandler;
    private long id;
    private String LID="downloadId";
    private MyAdapter adapter;
    private DownloadChangeObserver downloadChangeObserver;
    ArrayList<String> url_list=new ArrayList<>();
    List<Map<String,Object>> data=new ArrayList<>();
    Map<String, Object> map=new HashMap<>();
    String video_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableHomeButton();
        init();
        ListView listView = (ListView) findViewById(R.id.video_download);
        adapter= new MyAdapter(DownLoad.this,data);//初始化adapter，myadapter是自定义的一个adapter
        id= preferences.getLong(LID, 0);
        mHandler=new MyHandler();
        if (dirIsNotNull()){
            updateView();
        }
        updateView();
        Bundle bundle=this.getIntent().getExtras();
        url_list=bundle.getStringArrayList("list");
        video_name=bundle.getString("videoname").trim().replace("当前解析视频：", "");
        if (fileIsExists())
        listView.setAdapter(adapter);
        map.put("video_name", video_name);
        map.put("progress",0);
        map.put("maxProgress",0);
        data.add(map);
    }

    public void init(){
        downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManagerPro=new DownloadManagerPro(downloadManager);
        downloadChangeObserver=new DownloadChangeObserver();
        preferences= PreferenceManager.getDefaultSharedPreferences(this);

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.download;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Download-pause", "onPause");
        getContentResolver().unregisterContentObserver(downloadChangeObserver);
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Download-resume","   "+preferences.contains(LID)+"  "+fileIsExists());
        if(!preferences.contains(LID)||!fileIsExists()){
            for (int i=0;i<url_list.size();i++) {
                startDownload(url_list.get(i));
            }
        }
        queryDownloadStatus();
        getContentResolver().registerContentObserver(DownloadManagerPro.CONTENT_URI, true, downloadChangeObserver);
        updateView();
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //这里可以取得下载的id，这样就可以知道哪个文件下载完成了。适用与多个下载任务的监听
            Log.i("intent", ""+intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
            id=intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,0);
            queryDownloadStatus();
            updateView();
        }
    };

    class DownloadChangeObserver extends ContentObserver{

        public DownloadChangeObserver() {
            super(mHandler);
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.i("onChange","change");
            updateView();
            super.onChange(selfChange);
        }
    }

    private void queryDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(preferences.getLong(LID, 0));
        Cursor c = downloadManager.query(query);
        if(c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch(status) {
                case DownloadManager.STATUS_PAUSED:
                    Toast.makeText(DownLoad.this,"下载暂停了",Toast.LENGTH_SHORT).show();
                    break;
                case DownloadManager.STATUS_PENDING:
                    break;
                case DownloadManager.STATUS_RUNNING:
                    //正在下载，不做任何事情
                    Toast.makeText(DownLoad.this,"还在下载中",Toast.LENGTH_SHORT).show();
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    //完成
                    if (fileIsExists()) Toast.makeText(DownLoad.this,"下载已经完成",Toast.LENGTH_SHORT).show();
                    updateView();
                    break;
                case DownloadManager.STATUS_FAILED:
                    //清除已下载的内容，重新下载
                    downloadManager.remove(preferences.getLong(LID, 0));
                    preferences.edit().clear().apply();
                    break;
            }
        }

    }

    public void startDownload(String url){
        Uri resource=Uri.parse(url);
        //开始下载
        DownloadManager.Request request=new DownloadManager.Request(resource);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE| DownloadManager.Request.NETWORK_WIFI);
        request.setAllowedOverRoaming(false);//允许漫游
        //设置文件类型
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        String mimeString=mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
        request.setMimeType(mimeString);
        //通知栏通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(false);
        //sdcard的download
        if (url_list.size()>1){
            String videos_name=video_name.replaceAll("\\s","_");
            File dir=new File("/VideoGet/"+videos_name);
            if (!dir.exists()){dir.mkdir();}
            request.setDestinationInExternalPublicDir("/VideoGet/"+videos_name,videos_name+".flv");
        }else {request.setDestinationInExternalPublicDir("/VideoGet/",video_name+".flv");}
        request.setTitle("下载中:"+video_name);
        id= downloadManager.enqueue(request);
        //善后处理
        updateView();
        preferences.edit().putLong(LID, id).commit();
    }

    public boolean dirIsNotNull(){
        File dir=new File("/VideoGet/");
        if (dir.exists()&&dir.isDirectory()){
            if (dir.list().length>0) return true;
            else return false;
        }else {
            return false;
        }
    }

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

    public void updateView(){
        int[] bytesAndStatues =downloadManagerPro.getBytesAndStatus(id);
        mHandler.sendMessage(mHandler.obtainMessage(0,bytesAndStatues[0],bytesAndStatues[1],
                bytesAndStatues[2]));
    }

    class MyHandler extends android.os.Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    int status= (int) msg.obj;
                    Log.i("msg-0",""+msg.obj);
                    if (isDownloading(status)){
                        Log.i("msg-4","arg1"+msg.arg1+"arg2"+msg.arg2);
                        if (msg.arg2<0){
                            Log.i("msg-1","arg1"+msg.arg1+"arg2"+msg.arg2+"没有得到下载文件的大小");
                        }else {
                            Log.i("msg-2","arg1"+msg.arg1+"arg2"+msg.arg2+"得到了下载文件的大小");
                            updateProgress(0,msg.arg1,msg.arg2,status);
                        }
                    }else {
                        Log.i("msg-3","arg1"+msg.arg1+"arg2"+msg.arg2);
                        updateProgress(0,msg.arg1,msg.arg2,status);
                    }
                break;
            }
        }

        private void updateProgress(int id, int currentPos,int maxProgress,int status) {
            Log.i("data",""+data);
            String _status = null;
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    _status="paused";
                    break;
                case DownloadManager.STATUS_PENDING:
                    _status="pending";
                    break;
                case DownloadManager.STATUS_RUNNING:
                    _status="running";
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    _status="finish";
                    break;
                case DownloadManager.STATUS_FAILED:
                    _status="failed";
                    break;
            }
            Map<String, Object> dataTemp = data.get(id);
            dataTemp.put("progress", currentPos);
            dataTemp.put("maxProgress",maxProgress);
            dataTemp.put("status",_status);
            adapter.changeProgress(id, dataTemp);
        }
    }

    private boolean isDownloading(int downloadManagerStatus){
        return downloadManagerStatus==downloadManager.STATUS_RUNNING
             ||downloadManagerStatus==downloadManager.STATUS_PENDING
             ||downloadManagerStatus==downloadManager.STATUS_PAUSED;
    }

    /*private void notification(){
        boolean append=preferences.getBoolean("append",false);
        if (append){
            Intent intent=new Intent(this, VideoExplore.class);
            PendingIntent pd=PendingIntent.getActivity(DownLoad.this,0,intent,0);
            NotificationManager notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification notification=new Notification();
            notification.icon=R.drawable.getvideo;
            notification.tickerText="开始合并视频";
            notification.setLatestEventInfo(this,"合并","Test",pd);
            notificationManager.notify(110,notification);
        }
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
