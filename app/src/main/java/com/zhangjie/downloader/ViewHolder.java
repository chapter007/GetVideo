package com.zhangjie.downloader;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhangjie.getvideo.DownLoad;
import com.zhangjie.getvideo.R;
import java.util.HashMap;

public class ViewHolder {

	public static final int KEY_URL = 0;
	public static final int KEY_SPEED = 1;
	public static final int KEY_PROGRESS = 2;
	public static final int KEY_IS_PAUSED = 3;
    public TextView titleText;
	public ProgressBar progressBar;
	public TextView speedText;
	public Button pauseButton;
	public Button deleteButton;
	public Button continueButton;
    private boolean hasInited = false;
    private Context context;

	public ViewHolder(View parentView,Context context) {
		if (parentView != null) {
			titleText = (TextView) parentView.findViewById(R.id.title);
			speedText = (TextView) parentView.findViewById(R.id.speed);
			progressBar = (ProgressBar) parentView
					.findViewById(R.id.progress_bar);
			pauseButton = (Button) parentView.findViewById(R.id.btn_pause);
			deleteButton = (Button) parentView.findViewById(R.id.btn_delete);
			continueButton = (Button) parentView
					.findViewById(R.id.btn_continue);
            hasInited = true;
            this.context=context;
		}
	}

	public static HashMap<Integer, String> getItemDataMap(String url,
			String speed, String progress, String isPaused) {
		HashMap<Integer, String> item = new HashMap<Integer, String>();
		item.put(KEY_URL, url);
		item.put(KEY_SPEED, speed);
		item.put(KEY_PROGRESS, progress);
		item.put(KEY_IS_PAUSED, isPaused);
		return item;
	}

	public void setData(HashMap<Integer, String> item) {
		if (hasInited) {
            titleText.setText(ConfigUtils.getVideoName(context,"VideoName"));  //怎么得到下载文件名是我现在的主要问题
			speedText.setText(item.get(KEY_SPEED));
			String progress = item.get(KEY_PROGRESS);
			if (TextUtils.isEmpty(progress)) {
				progressBar.setProgress(0);
			} else {
				progressBar.setProgress(Integer.parseInt(progress));
			}
			if (Boolean.parseBoolean(item.get(KEY_IS_PAUSED))) {
				onPause();
			}
		}
	}

	public void onPause() {
		if (hasInited) {
			pauseButton.setVisibility(View.GONE);
			continueButton.setVisibility(View.VISIBLE);
		}
	}

	public void setData(String url, String speed, String progress) {
		setData(url, speed, progress, false + "");
	}

	public void setData(String url, String speed, String progress,
			String isPaused) {
		if (hasInited) {
			HashMap<Integer, String> item = getItemDataMap(url, speed,
					progress, isPaused);
			titleText.setText(ConfigUtils.getVideoName(context,"VideoName"));   //需要从url里面得到下载文件的名字
			speedText.setText(speed);
			if (TextUtils.isEmpty(progress)) {
				progressBar.setProgress(0);
			} else {
				progressBar.setProgress(Integer.parseInt(item.get(KEY_PROGRESS)));
			}

		}
	}

	public void bindTask(DownloadTask task) {
		if (hasInited) {
			titleText.setText(ConfigUtils.getVideoName(context,"VideoName"));
			speedText.setText(task.getDownloadSpeed() + "kb/s | "
					+ task.getDownloadSize() + " / " + task.getTotalSize());
			progressBar.setProgress((int) task.getDownloadPercent());
			if (task.isInterrupt()) {
				onPause();
			}
		}
	}

}
