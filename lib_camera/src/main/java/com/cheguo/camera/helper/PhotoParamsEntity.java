package com.cheguo.camera.helper;

import com.cheguo.camera.CameraPhotosAdapter;
import com.cheguo.camera.PhotoFragment;

import java.io.Serializable;

/**
 * Created by huchao on 2017/11/3.
 * Description :
 */

public class PhotoParamsEntity implements Serializable{

    public int width;//照片宽
    public int height;//照片高
    public String picDir;//自定义照片存放路径
    public int coverImage;//拍照页面覆盖层
    public boolean isShowSuffixName;//照片是否添加后缀名
    public boolean isSaveToAlbum;//照片是否保存到系统相册
    public CameraPhotosAdapter.IPhotoLoad listLoad;//已拍照片列表图片加载
    public PhotoFragment.IPhotoLoad viewPagerLoad;//照片列表详情加载

}
