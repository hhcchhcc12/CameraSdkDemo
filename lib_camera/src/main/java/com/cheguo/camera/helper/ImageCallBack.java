package com.cheguo.camera.helper;

import java.util.List;

/**
 * Created by huchao on 2017/9/22.
 * Description :
 */

public interface ImageCallBack<T> {

    void takePhotoComplete(List<T> list);

}
