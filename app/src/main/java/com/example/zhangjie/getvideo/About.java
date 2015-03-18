package com.example.zhangjie.getvideo;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by zhangjie on 2015/2/22.
 */
public class About extends ToolBar{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableHomeButton();

    }


    @Override
    protected int getLayoutResource() {
        return R.layout.about;
    }


}
