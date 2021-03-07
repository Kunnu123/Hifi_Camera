package com.hificamera.thecamhi.main;

import java.io.File;
import com.hificamera.R;
import com.hificamera.thecamhi.activity.LocalPictureActivity;
import com.hificamera.thecamhi.base.HiToast;
import com.hificamera.thecamhi.base.TitleView;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.bean.MyCamera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PictureFragment extends HiFragment {
	private View view;
	private PictureListAdapter mPictureListAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_picture, null);
		initView();

		return view;
	}

	private void initView() {
		TitleView title = (TitleView) view.findViewById(R.id.title_top);
		title.setTitle(getResources().getString(R.string.title_picture_fragment));

		ListView picture_fragment_camera_list = (ListView) view.findViewById(R.id.picture_fragment_camera_list);
		mPictureListAdapter=new PictureListAdapter(getActivity());
		picture_fragment_camera_list.setAdapter(mPictureListAdapter);
		picture_fragment_camera_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				MyCamera selectedCamera = HiDataValue.CameraList.get(position);
				if (getFileCount(selectedCamera.getUid()) == 0) {
					HiToast.showToast(getActivity(), getString(R.string.tips_no_picture));
					return;
				}
				Bundle extras = new Bundle();
				extras.putString(HiDataValue.EXTRAS_KEY_UID, selectedCamera.getUid());
				Intent intent = new Intent();
				intent.putExtras(extras);
				intent.setClass(getActivity(), LocalPictureActivity.class);
				startActivity(intent);

			}
		});
	}

	public int getFileCount(String uid) {
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapshot/" + uid + "/";
		File folder = new File(path);
		String[] imageFiles = folder.list();

		if (imageFiles == null) {
			return 0;
		}
		int sum = 0;
		if (imageFiles != null && imageFiles.length > 0) {
			for (String imageFile : imageFiles) {
				if(imageFile.endsWith(".jpg")){
					sum++;
				}
			}
		}

		return sum;
	}

	protected class PictureListAdapter extends BaseAdapter {

		private LayoutInflater mInflater;


		public PictureListAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {

			return HiDataValue.CameraList.size();
		}

		@Override
		public Object getItem(int position) {

			return HiDataValue.CameraList.get(position);
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final MyCamera cam = HiDataValue.CameraList.get(position);

			if (cam == null)
				return null;

			ViewHolder holder = null;

			if (convertView == null) {

				convertView = mInflater.inflate(R.layout.list_video_camera, null);

				holder = new ViewHolder();
				holder.txt_video_camera_nike = (TextView) convertView.findViewById(R.id.txt_video_camera_nike);
				holder.txt_video_camera_uid = (TextView) convertView.findViewById(R.id.txt_video_camera_uid);
				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			String uid = cam.getUid();

			if (holder != null) {
				holder.txt_video_camera_nike.setText(cam.getNikeName());
				holder.txt_video_camera_uid.setText(uid+"("+getFileCount(cam.getUid())+")");
			}

			return convertView;

		}

		public final class ViewHolder {
			// public ImageView img;
			public TextView txt_video_camera_nike;
			public TextView txt_video_camera_uid;
		}

	}
	@Override
	public void onResume() {
		super.onResume();
		mPictureListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if(!hidden&&mPictureListAdapter!=null){
			mPictureListAdapter.notifyDataSetChanged();
		}
	}
}
