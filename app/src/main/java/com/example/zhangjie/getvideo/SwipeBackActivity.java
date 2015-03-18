package com.example.zhangjie.getvideo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

/**
 * @author tim
 * @date 2014-12-19
 * @email tim_ding@qq.com
 */

public class SwipeBackActivity extends Activity {
	protected SwipeBackLayout layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		layout = (SwipeBackLayout) LayoutInflater.from(this).inflate(
				R.layout.base, null);
		layout.attachToActivity(this);
	}



    @Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
	}
	 

}
