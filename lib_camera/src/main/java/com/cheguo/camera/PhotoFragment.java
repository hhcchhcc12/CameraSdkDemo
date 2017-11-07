package com.cheguo.camera;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cheguo.camera.view.CustomViewPager;

import java.io.Serializable;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by huchao on 2017/8/29.
 * Description :图片查看页面
 */

public class PhotoFragment extends BaseDialogFragment {

    public static final String KEY_PHOTO_URL = "KEY_PHOTO_URL";
    public static final String KEY_POSITION = "KEY_POSITION";
    public static final String KEY_PHOTOLOAD = "KEY_PHOTOLOAD";

    ImageView titleBarLeftIv;
    CustomViewPager photosViewpager;

    private String[] photoUrls;
    private int startPosition;
    private IPhotoLoad load;

    public PhotoFragment() {
        super();
    }

    public void setLoad(IPhotoLoad load) {
        this.load = load;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_detail_layout, container);

        initData();
        initView(view);

        return view;
    }

    private void initView(View view) {
        titleBarLeftIv = (ImageView) view.findViewById(R.id.title_bar_left_iv);
        photosViewpager = (CustomViewPager) view.findViewById(R.id.photos_viewpager);

        photosViewpager.setAdapter(new SamplePagerAdapter(photoUrls));
        photosViewpager.setCurrentItem(startPosition);

        titleBarLeftIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exit();
            }
        });
    }

    private void initData() {
        photoUrls = getArguments().getStringArray(KEY_PHOTO_URL);
        startPosition = getArguments().getInt(KEY_POSITION);
        load = (IPhotoLoad) getArguments().getSerializable(KEY_PHOTOLOAD);

    }

    protected void exit(){
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    class SamplePagerAdapter extends PagerAdapter {

        private String[] photoUrls;

        public SamplePagerAdapter(String[] photoUrls) {
            this.photoUrls = photoUrls;
        }

        @Override
        public int getCount() {
            return photoUrls.length;
        }

        @Override
        public View instantiateItem(ViewGroup container, final int position) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_photo_viewpager_item_layout, null);
            PhotoView photoView = (PhotoView) view.findViewById(R.id.photo_view);
            if (!TextUtils.isEmpty(photoUrls[position]) && load != null) {
                load.loadImage(getActivity(),photoView,photoUrls[position]);
//                Glide.with(PhotoFragment.this).load(photoUrls[position])
//                        .placeholder(R.drawable.bg_default_pic)   // 加载过程中的占位Drawable
//                        .error(R.drawable.bg_default_pic)
//                        .into(photoView);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

    public interface IPhotoLoad extends Serializable {
        void loadImage(Activity context, ImageView photo, String imagePath);
    }

}
