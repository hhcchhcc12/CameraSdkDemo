package com.cheguo.camera;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

import com.cheguo.camera.helper.PermissionUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by huchao on 2017/9/21.
 * Description :
 */

public class CameraUtils {

    /**
     * 将时间戳转化成格式化的时间<br>
     * "yyyy-MM-dd HH:mm:ss"<br>
     *
     * @method FormatTimeForm
     * @param time
     * @return
     * @throws
     * @since v1.0
     */
    public static String FormatTimeForm(long time, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(new Date(time));
    }

    /**
     * 获取当前应用程序的包名
     * @param context 上下文对象
     * @return 返回包名
     */
    public static String getAppProcessName(Context context) {
        //当前应用pid
        int pid = android.os.Process.myPid();
        //任务管理类
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //遍历所有应用
        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.pid == pid)//得到当前应用
                return info.processName;//返回包名
        }
        return "";
    }

    /**
     * 得到SD卡的路径
     *
     * @return
     */
    public static String getSDCardAbsolutePath() {
        if ((Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
            String sdPath = Environment.getExternalStorageDirectory().getPath() + "/";
            return sdPath;
        }
        return null;

    }

    /**
     * 获取SD卡剩余空间
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getSDFreeSize(){
        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = 0;
        //空闲的数据块的数量
        long freeBlocks = 0;
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){//4.3以后新api
            blockSize = sf.getBlockSizeLong();
            freeBlocks = sf.getAvailableBlocksLong();
        }
        else{//4.2及以前老api
            blockSize = sf.getBlockSize();
            freeBlocks = sf.getAvailableBlocks();
        }
        //返回SD卡空闲大小
        //return freeBlocks * blockSize;  //单位Byte
        //return (freeBlocks * blockSize)/1024;   //单位KB
        return (freeBlocks * blockSize)/1024 /1024; //单位MB
    }

    /**
     * 删除文件
     * @param path
     * @return
     */
    public static boolean deleteFile(String path) {
        if(TextUtils.isEmpty(path)){
            return false;
        }
        File file = new File(path);
        if (file != null && file.exists()) {
            return file.delete();
        }

        return false;

    }

    // 根据路径获得图片并压缩，返回bitmap用于显示
    public static Bitmap getSmallBitmap(byte[] image, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(image,0,image.length,options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(image, 0, image.length, options);
    }

    //计算图片的缩放值
    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 缩放图片
     * @param newWidth
     * @param newHeight
     * @param bitmap
     * @return
     */
    public static Bitmap scalingImageView(int angle,int newWidth,int newHeight, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        int bmWidth = bitmap.getWidth();
        int bmHeight = bitmap.getHeight();
        if(bmWidth > newWidth){
            //计算缩放率，新尺寸除原始尺寸
            float scaleWidth = ((float) newWidth) / bmWidth;
            float scaleHeight = ((float) newHeight) / bmHeight;
            // 缩放图片动作
            matrix.postScale(scaleWidth, scaleHeight);
        }
        if(angle > 0){
            // 旋转图片 动作
            matrix.postRotate(angle);
        }
        try {

            // 创建新的图片
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return resizedBitmap;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 图片保存为文件
     * @param bitmap
     * @param filePath
     * @param fileName
     * @param quality
     * @return
     */
    public static boolean saveBitmapToFile(Bitmap bitmap, String filePath,String fileName,int quality) {
        if(bitmap == null){
            Log.e("saveBitmapToFile","bitmap is null");
            return false;
        }

        File f = new File(filePath);
        if(!f.exists()){
            makeRootDirectory(filePath);
            f.mkdirs();
        }

        String local_file = f.getAbsolutePath()+"/"+fileName;
        File file = new File(local_file);
        BufferedOutputStream os = null;
        try {
            file.createNewFile();
            os = new BufferedOutputStream(new FileOutputStream(file));
            int q = 100;
            if(quality > 0 && quality < 101){
                q = quality;
            }
            return bitmap.compress(Bitmap.CompressFormat.JPEG, q, os);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void makeRootDirectory(String filePath) {
        File file = null;
        String newPath=null;
        String[] path=filePath.split("/");
        for(int i=0;i<path.length;i++){
            if(newPath==null){
                newPath=path[i];
            }else{
                newPath=newPath+"/"+path[i];
            }
            file = new File(newPath);
            if (!file.exists()) {
                file.mkdir();
            }
        }
    }

    /**
     * 显示前往应用设置Dialog
     *
     */
    public static void showToAppSettingDialog(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle("需要权限")
                .setMessage("我们需要相关权限，才能实现功能，点击前往，将转到应用的设置界面，请开启应用的相关权限。")
                .setPositiveButton("前往", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionUtils.toAppSetting(context);
                    }
                })
                .setNegativeButton("取消", null).show();
    }

}
