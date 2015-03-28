package com.zhangjie.downloader;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.zhangjie.getvideo.DownLoad;
import com.zhangjie.getvideo.R;

import java.util.ArrayList;
import java.util.HashMap;


public class DownloadListAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<HashMap<Integer, String>> dataList;

	public DownloadListAdapter(Context context) {
		mContext = context;
		dataList = new ArrayList<>();
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void addItem(String url) {
		addItem(url, false);
	}

	public void addItem(String url, boolean isPaused) {
		HashMap<Integer, String> item = ViewHolder.getItemDataMap(url, null,
				null, isPaused + "");
		dataList.add(item);
		this.notifyDataSetChanged();
	}

	public void removeItem(String url) {
		String tmp;
		for (int i = 0; i < dataList.size(); i++) {
			tmp = dataList.get(i).get(ViewHolder.KEY_URL);
			if (tmp.equals(url)) {
				dataList.remove(i);
				this.notifyDataSetChanged();
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//应该是创建list的时候使用,这是一个ListView的adapter
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.video_downloading_item, null);
		}

		HashMap<Integer, String> itemData = dataList.get(position);
		String url = itemData.get(ViewHolder.KEY_URL);
		convertView.setTag(url);

		ViewHolder viewHolder = new ViewHolder(convertView,mContext);
		viewHolder.setData(itemData);
		viewHolder.continueButton.setOnClickListener(new DownloadBtnListener(
				url, viewHolder));
		viewHolder.pauseButton.setOnClickListener(new DownloadBtnListener(url,
				viewHolder));
		viewHolder.deleteButton.setOnClickListener(new DownloadBtnListener(url,
				viewHolder));
		return convertView;
	}

	private class DownloadBtnListener implements View.OnClickListener {
		private String url;
		private ViewHolder mViewHolder;

		public DownloadBtnListener(String url, ViewHolder viewHolder) {
			this.url = url;
			this.mViewHolder = viewHolder;
		}

		@Override
		public void onClick(View v) {
			Intent downloadIntent = new Intent(
					"com.zhangjie.downloader.IDownloadService");

			switch (v.getId()) {
			case R.id.btn_continue:
				downloadIntent.putExtra(MyIntents.TYPE,
						MyIntents.Types.CONTINUE);
				downloadIntent.putExtra(MyIntents.URL, url);
				mContext.startService(new Intent(DownLoad.createExplicitFromImplicitIntent
                        (mContext,downloadIntent)));
				mViewHolder.continueButton.setVisibility(View.GONE);
				mViewHolder.pauseButton.setVisibility(View.VISIBLE);
				break;
			case R.id.btn_pause:
				downloadIntent.putExtra(MyIntents.TYPE, MyIntents.Types.PAUSE);
				downloadIntent.putExtra(MyIntents.URL, url);
                mContext.startService(new Intent(DownLoad.createExplicitFromImplicitIntent
                        (mContext,downloadIntent)));
				mViewHolder.continueButton.setVisibility(View.VISIBLE);
				mViewHolder.pauseButton.setVisibility(View.GONE);
				break;
			case R.id.btn_delete:
				downloadIntent.putExtra(MyIntents.TYPE, MyIntents.Types.DELETE);
				downloadIntent.putExtra(MyIntents.URL, url);
                mContext.startService(new Intent(DownLoad.createExplicitFromImplicitIntent
                        (mContext,downloadIntent)));
				removeItem(url);
				break;
			}
		}
	}

}