package com.cheguo.camera;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cheguo.camera.view.CameraUtil;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class CameraPhotosAdapter extends BaseAdapter {

	private ArrayList<String> imageList;
	private Activity context;
	private IPhotoLoad load;

	public CameraPhotosAdapter() {
	}

	public CameraPhotosAdapter(Activity context, ArrayList<String> imageList) {
		this.context = context;
		this.imageList = imageList;
	}

	public void refresh(ArrayList<String> imageList) {
		this.imageList = imageList;
		notifyDataSetChanged();

	}

	public void setLoad(IPhotoLoad load) {
		this.load = load;
	}

	@Override
	public int getCount() {
		return imageList.size();
	}

	@Override
	public Object getItem(int position) {
		return imageList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final String imagePath = imageList.get(position);

		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.camera_item, null);
			viewHolder.photo = (ImageView) convertView.findViewById(R.id.photoshare_item_image);
			viewHolder.icon = (ImageView) convertView.findViewById(R.id.photoshare_item_delete);
			convertView.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

//		Glide.with(context).load(imagePath)
//				.placeholder(R.drawable.bg_default_pic)   // 加载过程中的占位Drawable
//				.error(R.drawable.bg_default_pic)
//				.into(viewHolder.photo);
		if(load != null){
			load.loadImage(context,viewHolder.photo,imagePath);
		}

		viewHolder.icon.setVisibility(View.VISIBLE);
		viewHolder.icon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				File file=new File(imagePath);
				if(file.exists()){
					boolean bo = file.delete();
					if(bo){
						imageList.remove(imagePath);
						CameraUtils.deleteFile(imagePath);
						notifyDataSetChanged();
						//删除app数据库中图片信息
//						int row = ImageDao.getInstance(context).deleteImageByPath(imagePath);
//						if(row > 0){
//							Utils.deleteFile(imagePath);
//						}
						//删除媒体数据库中图片信息
						//Utils.deleteMediaData(imagePath, context);
					}
				}

			}
		});

		return convertView;

	}

	class ViewHolder {
		private ImageView photo;
		private ImageView icon;
	}

	public interface IPhotoLoad extends Serializable{
		void loadImage(Activity context,ImageView photo,String imagePath);
	}

}
