package com.cheguo.camera.helper;

import com.cheguo.camera.CameraPhotosAdapter;
import com.cheguo.camera.PhotoFragment;

import java.io.Serializable;

/**
 * Created by huchao on 2017/11/3.
 * Description :
 */

public class PhotoParamsEntity implements Serializable{

    public int width;
    public int height;
    public String picDir;
    public int coverImage;
    public CameraPhotosAdapter.IPhotoLoad listLoad;
    public PhotoFragment.IPhotoLoad viewPagerLoad;

}
