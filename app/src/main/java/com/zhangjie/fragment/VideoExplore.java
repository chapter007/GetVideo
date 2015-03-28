package com.zhangjie.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.zhangjie.getvideo.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangjie on 2015/2/11.
 */
public class VideoExplore extends Fragment{
    private List<Map<String,Object>> data=new ArrayList<>();
    private String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/VideoGet";
    private boolean deletefile;
    private SimpleAdapter adapter;
    File videoGet=new File(path);
    String _path="";

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.video_explore,container,false);
        ListView videos = (ListView) v.findViewById(R.id.videos);
        final Button back=new Button(getActivity());
        back.setText("返回上一层");
        hideFooter(back);
        initData();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        deletefile= preferences.getBoolean("append", false);
        Log.i("data",""+data);
        adapter=new SimpleAdapter(getActivity(),data,R.layout.video_items,
                new String[]{"video_pre","video_single_name","video_info"},
                new int[]{R.id.video_pre,R.id.video_single_name,R.id.video_info});

        videos.setAdapter(adapter);
        videos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                File[] Lvideos = videoGet.listFiles();
                if (Lvideos[position].isDirectory()) {
                    videoGet = Lvideos[position];
                    File[] video_temp = Lvideos[position].listFiles();
                    inflateFloder(video_temp);
                    back.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                } else if (Lvideos[position].isFile()) {
                    openVideo(Lvideos[position]);
                }
            }
        });
        videos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                final File[] Lvideos = videoGet.listFiles();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final AlertDialog dialog = builder.create();
                dialog.setTitle("选择操作");
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.choose_dialog, null);
                final ListView choose = (ListView) linearLayout.findViewById(R.id.choose);
                String[] choose_item = new String[]{"删除", "合并"};
                choose.setAdapter(new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_list_item_1, choose_item));
                if (Lvideos[position].isDirectory()) {
                    dialog.setView(linearLayout);
                    choose.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            switch (i) {
                                case 0:
                                    deleteFiles(Lvideos[position]);
                                    inflateFloder(videoGet.listFiles());
                                    adapter.notifyDataSetChanged();
                                    dialog.dismiss();
                                    break;
                                case 1:
                                    File[] temp = Lvideos[position].listFiles();
                                    String[] _videos = new String[temp.length];
                                    _path = Lvideos[position].getPath();
                                    Log.i("合并-1", "" + temp.length);
                                    for (int v = 0; v < temp.length; v++) {
                                        _videos[v] = temp[v].getPath();
                                    }
                                    new appendTask().execute(_videos);
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    });

                    dialog.show();

                } else if (Lvideos[position].isFile()) {
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "删除",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Lvideos[position].delete();
                                    inflateFloder(videoGet.listFiles());
                                    adapter.notifyDataSetChanged();
                                }
                            });
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialog.dismiss();
                                }
                            });
                    dialog.show();

                }
                return true;
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File currentParent = videoGet.getParentFile();
                videoGet=currentParent;
                Log.i("path",""+videoGet.getPath());
                hideFooter(back);
                File[] currentFiles = currentParent.listFiles();
                inflateFloder(currentFiles);
                adapter.notifyDataSetChanged();
            }
        });
        videos.addFooterView(back);
        return v;
    }

    private class appendTask extends AsyncTask<String,Integer,String>{

        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog=ProgressDialog.show(getActivity(),"正在合并视频","耐心等待");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String[] videos) {
            try {
                String finalFile=_path+".flv ";
                String dir="./data/data/com.example.zhangjie.getvideo/files/flvmerge ";
                String cmd=dir+finalFile;
                for (int i1=0;i1<videos.length;i1++){
                    cmd=cmd+videos[i1]+" ";
                }
                Log.i("cmd",cmd);
                Process process=Runtime.getRuntime().exec(cmd);
                InputStream is = process.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                int x=videos.length;
                String line;
                Log.i("line","x-"+x);
                /* 逐行读取脚本执行结果 */
                Log.i("result",""+br.readLine());
                while ((line = br.readLine()) != null) {
                    Log.i("line", line);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (deletefile) {
                File file = new File(_path);
                deleteFiles(file);
            }
            progressDialog.dismiss();
            inflateFloder(videoGet.listFiles());
            adapter.notifyDataSetChanged();
            super.onPostExecute(s);
        }
    }

    private void initData(){
        File[] fvideos=videoGet.listFiles();
        if (videoGet.list().length>0){
            for (int i=0;i<fvideos.length;i++){
                Map<String,Object> map=new HashMap<>();
                if (fvideos[i].isDirectory()){map.put("video_pre",R.drawable.ic_folder_black_48dp);}
                else {map.put("video_pre",R.drawable.ic_movie_creation_black_48dp);}
                map.put("video_single_name",fvideos[i].getName());
                if (fvideos[i].isFile()){map.put("video_info",formatDataSize(fvideos [i].length()));}
                data.add(map);
            }
        }else {
            Toast.makeText(getActivity(),"还没有下载视频",Toast.LENGTH_SHORT).show();
        }
    }

    private void inflateFloder(File[] files){
        data.clear();
        if (files.length>0){
            for (int i=0;i<files.length;i++){
                Map<String,Object> map=new HashMap<>();
                if (files[i].isDirectory()){map.put("video_pre",R.drawable.ic_folder_black_48dp);}
                else {map.put("video_pre",R.drawable.ic_movie_creation_black_48dp);}
                map.put("video_single_name",files[i].getName());
                if (files[i].isFile()){map.put("video_info",formatDataSize(files[i].length()));}
                data.add(map);
            }
        }else {
            Toast.makeText(getActivity(),"此文件夹无视频",Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDataSize(long file){
        DecimalFormat decimalFormat=new DecimalFormat("#.00");
        String fileSize="";
        if (file < 1024)
        {
            fileSize = decimalFormat.format((double) file) + "B";
        }
        else if (file < 1048576)
        {
            fileSize = decimalFormat.format((double) file / 1024) + "K";
        }
        else if (file < 1073741824)
        {
            fileSize = decimalFormat.format((double) file / 1048576) + "M";
        }
        else
        {
            fileSize = decimalFormat.format((double) file / 1073741824) + "G";
        }
        return fileSize;
    }

    private void openVideo(File file){
        Intent intent=new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type="video";
        intent.setDataAndType(Uri.fromFile(file),type);
        startActivity(intent);
    }

    private void hideFooter(Button back){
        Log.i("hide?","");
        if (videoGet.getPath().equals(Environment.getExternalStorageDirectory().getAbsolutePath()+"/VideoGet")){
            back.setVisibility(View.INVISIBLE);
        }
    }

    private void deleteFiles(File file){
        if (file.isDirectory()){
            File[] files=file.listFiles();
            for (int i=0;i<files.length;i++){
                this.deleteFiles(files[i]);
            }
        }else if (file.isFile()){
            file.delete();
        }
        file.delete();
    }

}
