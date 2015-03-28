package com.zhangjie.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zhangjie.getvideo.R;
import com.zhangjie.getvideo.VideoSite;

import java.io.File;

/**
 * Created by zhangjie on 2015/2/13.
 */
public class MainView extends Fragment{

    private LinearLayout ku6,youku,tudou;
    private EditText SearchVideo;
    private Button Search;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.activity_main,container,false);

        ku6= (LinearLayout) view.findViewById(R.id.ku6);
        youku= (LinearLayout) view.findViewById(R.id.youku);
        tudou= (LinearLayout) view.findViewById(R.id.tudou);
        Search= (Button) view.findViewById(R.id.SearchBtn);
        SearchVideo= (EditText) view.findViewById(R.id.SearchVideo);

        Search.setOnClickListener(new myOnclick());
        ku6.setOnClickListener(new myOnclick());
        youku.setOnClickListener(new myOnclick());
        tudou.setOnClickListener(new myOnclick());
        File videoDir=new File(Environment.getExternalStorageDirectory().getPath()+"/VideoGet");
        if (!videoDir.exists()){videoDir.mkdir();}

        return view;
    }

    public class myOnclick implements View.OnClickListener {

        Intent intent=new Intent(getActivity(),VideoSite.class);
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.ku6:
                    intent.putExtra("ku6","http://m.ku6.com");
                    startActivity(intent);
                    break;
                case R.id.youku:
                    intent.putExtra("youku","http://m.youku.com");
                    startActivity(intent);
                    break;
                case R.id.tudou:
                    intent.putExtra("tudou","http://m.tudou.com");
                    startActivity(intent);
                    break;
                case R.id.SearchBtn:
                    if (!TextUtils.isEmpty(SearchVideo.getText())){
                    intent.putExtra("soku","http://www.soku.com/m/y/video?q="+
                            SearchVideo.getText().toString());
                        startActivity(intent);
                    }else{
                        Toast.makeText(getActivity(),"请输入内容",Toast.LENGTH_SHORT).show();
                    }
                    break;
            }

        }

    }

}
