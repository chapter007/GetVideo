package com.zhangjie.getvideo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zhangjie.fragment.MainView;
import com.zhangjie.fragment.VideoExplore;
import im.fir.sdk.FIR;
import im.fir.sdk.callback.VersionCheckCallback;
import im.fir.sdk.version.AppVersion;


public class MainActivity extends ToolBar {

    private ActionBarDrawerToggle mToogle;
    private DrawerLayout mDrawer;
    private ListView nav_list;
    private MainView myMainView;
    private VideoExplore videoExplore;
    private FloatingActionButton mFAB;
    private ProgressDialog progressDialog;
    private ArrayList<String> downloadUrl=new ArrayList<>();
    private SharedPreferences preference;
    private boolean quality;
    private String VideoName,updateLog;
    private String Token="95b86c5ebbaffd0d4bf2a47e2fa8935c";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableHomeButton();
        myMainView=new MainView();
        initDrawer();
        initLayout();
        try {
            File f=new File(this.getFilesDir()+"/flvmerge");
            if (!f.exists()) getAsserts();
        } catch (IOException e) {
            e.printStackTrace();
        }
        preference=PreferenceManager.getDefaultSharedPreferences(this);
        quality=preference.getBoolean("quality",false);
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
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                LayoutInflater layoutInflater=LayoutInflater.from(MainActivity.this);
                LinearLayout linearLayout= (LinearLayout) layoutInflater.inflate(R.layout.alert_dialog,null);
                final EditText url= (EditText) linearLayout.findViewById(R.id.input_url);
                builder.setTitle("直接下载视频");
                builder.setView(linearLayout);
                builder.setNegativeButton("下载",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!TextUtils.isEmpty(url.getText())){
                            getVideo(url.getText().toString());
                        }else{
                            Toast.makeText(MainActivity.this,"请输入下载链接",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.show();
            }
        });


        //check_update
        FIR.checkForUpdateInFIR(this,Token,new VersionCheckCallback() {
            @Override
            public void onSuccess(AppVersion appVersion, boolean b) {
                Log.i("AppVersion","versionName:"+appVersion.getVersionName());
                updateLog=appVersion.getChangeLog();
                PackageManager pm=MainActivity.this.getPackageManager();
                try {
                    PackageInfo pi=pm.getPackageInfo(MainActivity.this.getPackageName(),
                            PackageManager.GET_ACTIVITIES);
                    if (pi != null) {
                        int currentVersionCode = pi.versionCode;
                        String currentVersionName = pi.versionName;
                        if (appVersion.getVersionCode() > currentVersionCode) {
                            //需要更新
                            Log.i("info_1", "need update");
                            update();
                        } else if (appVersion.getVersionCode() == currentVersionCode) {
                            //如果本地app的versionCode与FIR上的app的versionCode一致，则需要判断versionName.
                            if (!currentVersionName.equals(appVersion.getVersionName())) {
                                Log.i("info", "need update");
                                update();
                            }
                        } else {
                            //不需要更新,当前版本高于FIR上的app版本.
                            Log.i("info", " no need update");
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this,"当前版本是："+appVersion.getVersionName(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(String s, int i) {

            }

            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }
        });

    }

    private void update(){
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        final AlertDialog dialog=builder.create();
        dialog.setTitle("有新版本，是否更新？");
        TextView update_log=new TextView(MainActivity.this);
        update_log.setTextSize(20);
        update_log.setText(updateLog);
        dialog.setView(update_log);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE,"确定",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://fir.im/8gpe");
                intent.setData(content_url);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void getAsserts() throws IOException {
        Log.i("asserts","flvmerge build");
        BufferedInputStream inputStream = new BufferedInputStream( getResources().getAssets().open("flvmerge") );
        RandomAccessFile file= new RandomAccessFile(this.getFilesDir()+"/flvmerge","rw");
        byte[] buf=new byte[512];
        while (true) {
            int len=inputStream.read(buf,0,512);
            if (len==-1) {
                file.close();
                break;
            }else {
                file.write(buf,0,len);
            }
        }
        Runtime.getRuntime().exec("chmod 777 "+this.getFilesDir()+"/flvmerge");
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.drawer;
    }

    public void getVideo(String url){
        String FLVCD="http://www.flvcd.com/parse.php?format=&kw=";
        FLVCD=FLVCD+url;
        Log.i("FLVCD",FLVCD);
        new VideoParse().execute(FLVCD);
    }

    public class VideoParse extends AsyncTask<String,Integer,String> {

        @Override
        protected void onPreExecute() {
            progressDialog= ProgressDialog.show(MainActivity.this, "正在解析地址", "请稍等");
            progressDialog.setCancelable(true);
            Log.i("onpreexecute","做准备");
            super.onPreExecute();
        }

        int[] flag=new int[3];
        @Override
        protected String doInBackground(String... strings) {
            ParseUrl mParseUrl = new ParseUrl();
            String[] result = new String[3];
            result[0]=mParseUrl.parseUrl(strings[0]);
            if (result[0].contains("高清版")){
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
            progressDialog.dismiss();
            progressDialog=null;
            if (downloadUrl.size()==0) {
                Toast.makeText(MainActivity.this,"请输入合法的地址",Toast.LENGTH_LONG).show();
            }else if (downloadUrl.get(0).contains("温馨提示")){
                Toast.makeText(MainActivity.this,"没能获取到下载链接",Toast.LENGTH_LONG).show();
            }
            else {
                Intent intent=new Intent(MainActivity.this,DownLoad.class);
                Bundle bundle=new Bundle();
                bundle.putStringArrayList("list",downloadUrl);
                bundle.putString("VideoName", VideoName);
                intent.putExtras(bundle);
                startActivity(intent);
            }
            super.onPostExecute(s);
        }

    }

    public void initLayout(){
        getSupportFragmentManager().beginTransaction().add(R.id.frame, myMainView)
                .commit();
    }

    public void initDrawer(){
        mDrawer= (DrawerLayout) findViewById(R.id.left_drawer);
        nav_list= (ListView) findViewById(R.id.nav_list);
        mToogle=new ActionBarDrawerToggle(this,mDrawer,mToolbar,0,0);
        mToogle.setDrawerIndicatorEnabled(true);
        mToogle.syncState();
        mDrawer.setDrawerListener(mToogle);
        List<Map<String,Object>> data=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put("icon",R.drawable.ic_play_download_black_48dp);
        map.put("content","下载视频");
        data.add(map);
        Map<String,Object> map1=new HashMap<>();
        map1.put("icon", R.drawable.ic_play_install_black_48dp);
        map1.put("content","已下载");
        data.add(map1);
        Map<String,Object> map2=new HashMap<>();
        map2.put("icon", R.drawable.ic_get_app_black_48dp);
        map2.put("content","正在下载");
        data.add(map2);
        Log.i("data",""+data);
        SimpleAdapter adapter=new SimpleAdapter(this,data,R.layout.list_layout,
                new String[]{"icon","content"},new int[]{R.id.Licon,R.id.Lcontent});
        nav_list.setAdapter(adapter);
        nav_list.setOnItemClickListener(new MyOnclick());
    }

    public class MyOnclick implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mDrawer.closeDrawer(Gravity.LEFT);
            Log.i("position",""+i);
            if (i==0){
                getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.frame, myMainView).commit();
            }else if (i==1){
                Log.i("back","onInitLayout"+videoExplore);
                videoExplore=new VideoExplore();
                getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.frame, videoExplore)
                        .commit();
            }else if (i==2){
                Intent intent=new Intent(MainActivity.this,DownLoad.class);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.i("destroy","ok,it is destroy");
        System.exit(0);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent=new Intent(this,Setting.class);
            startActivity(intent);
        }
        if (id==R.id.about){
            Intent intent=new Intent(this,About.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}
