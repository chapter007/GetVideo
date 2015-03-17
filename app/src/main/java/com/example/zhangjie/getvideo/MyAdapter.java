package com.example.zhangjie.getvideo;

import android.app.DownloadManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by zhangjie on 2015/2/8.
 */

public class MyAdapter extends BaseAdapter {

    List<Map<String, Object>> list;
    LayoutInflater infl = null;
    TextView status;
    Button cancel;

    public MyAdapter(Context context, List<Map<String, Object>> _list){
        list=_list;
        infl= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView=infl.inflate(R.layout.video_downloading_item,null);
        ProgressBar progressBar= (ProgressBar) convertView.findViewById(R.id.download_progress);
        TextView textView= (TextView) convertView.findViewById(R.id.video_name);
        status= (TextView) convertView.findViewById(R.id.status);
        cancel= (Button) convertView.findViewById(R.id.stop);
        Map<String, Object> detail = list.get(position);
        String t = (String) detail.get("video_name");
        String s= (String) detail.get("status");
        textView.setText(t);
        status.setText(s);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        int progress = (Integer) detail.get("progress");
        int maxprogress= (int) detail.get("maxProgress");
        progressBar.setMax(maxprogress);
        progressBar.setProgress(progress);
        return convertView;
    }

    // 改变进度，postion就是要改的那个进度
    public void changeProgress(int postion, Map<String, Object> detail) {
        list.set(postion, detail);
        notifyDataSetChanged();
    }

}