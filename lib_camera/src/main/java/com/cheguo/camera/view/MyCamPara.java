package com.cheguo.camera.view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.WindowManager;

public class MyCamPara {
    private static final String tag = "MyCamPara";
    private CameraSizeComparator sizeComparator = new CameraSizeComparator();
    private static MyCamPara myCamPara = null;
    private float aspectRatio = 1.77f;
    Context context;
    private MyCamPara(Context context){
        this.context = context;
    }
    public static MyCamPara getInstance(Context context){
        if(myCamPara == null){
            myCamPara = new MyCamPara(context);
            return myCamPara;
        }
        else{
            return myCamPara;
        }
    }

    public  Size getPreviewSize(List<Camera.Size> list, int th,boolean isScreenHorizontal){
        Collections.sort(list, sizeComparator);
        Point p = getScreenSzie(context);
        if (p.y > 0 && p.x > 0) {
            aspectRatio = (float)p.y/(float)p.x;
        }
        int i = 0;
//        for(Size s:list){  
//            if((s.width > th) && equalRate(s, 1.33f)){  
//                Log.i(tag, "最终设置预览尺寸:w = " + s.width + "h = " + s.height);  
//                break;  
//            }  
//            i++;  
//        }
        int size = list.size();
        for(int j = 0;j<size;j++){
            i = j;
            Size s = list.get(j);
            if((s.width > th) && equalRate(s, aspectRatio,isScreenHorizontal)){
                Log.i(tag, "最终设置预览尺寸:w = " + s.width + "h = " + s.height);
                break;
            }
        }

        return list.get(i);
    }
    public Size getPictureSize(List<Camera.Size> list, int th,boolean isScreenHorizontal){
        Collections.sort(list, sizeComparator);
        Point p = getScreenSzie(context);
        if (p.y > 0 && p.x > 0) {
            aspectRatio = (float)p.y/(float)p.x;
        }
        //aspectRatio = 1.33f;
        int i = 0;
        int size = list.size();
        for(int j = 0;j<size;j++){
            i = j;
            Size s = list.get(j);
            int sizeInt = s.width;
            if(isScreenHorizontal){
                sizeInt = s.height;
            }
            if((sizeInt > th) && equalRate(s, aspectRatio,isScreenHorizontal)){
                Log.i(tag, "最终设置图片尺寸:w = " + s.width + "h = " + s.height);
                break;
            }
        }
//        for(Size s:list){  
//            if((s.width > th) && equalRate(s, 1.33f)){  
//                Log.i(tag, "最终设置图片尺寸:w = " + s.width + "h = " + s.height);  
//                break;  
//            }  
//            i++;  
//        } 


        return list.get(i);
    }

    public boolean equalRate(Size s, float rate,boolean isScreenHorizontal){
        float r = (float)(s.width)/(float)(s.height);
        if(isScreenHorizontal){
            r = (float)(s.height)/(float)(s.width);
        }
        if(Math.abs(r - rate) <= 0.2)
        {
            return true;
        }
        else{
            return false;
        }
    }

    public  class CameraSizeComparator implements Comparator<Camera.Size>{
        //按升序排列  
        public int compare(Size lhs, Size rhs) {
            // TODO Auto-generated method stub  
            if(lhs.width == rhs.width){
                return 0;
            }
            else if(lhs.width > rhs.width){
                return 1;
            }
            else{
                return -1;
            }
        }

    }

    /**
     *
     * @method 获取屏幕尺寸
     * @param context
     * @return
     * @throws
     * @since v1.0
     */
    private Point getScreenSzie(Context context) {
        WindowManager manager = ((Activity) context).getWindowManager();
        Point outSize = new Point();
        manager.getDefaultDisplay().getSize(outSize);

        return outSize;
    }


}  
