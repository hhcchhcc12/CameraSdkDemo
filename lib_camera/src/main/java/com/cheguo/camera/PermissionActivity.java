package com.cheguo.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.cheguo.camera.helper.PermissionUtils;

import java.util.Arrays;

/**
 * Created by huchao on 2017/9/21.
 * Description :
 */

public class PermissionActivity extends Activity {

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private final String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE
            , Manifest.permission.WRITE_EXTERNAL_STORAGE};

    // 打开相机请求Code，多个权限请求Code
    private final int REQUEST_CODE_CAMERA = 1,REQUEST_CODE_PERMISSIONS=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestMorePermissions(this);
    }

    // 普通申请一个权限
    private void requestPermission(final Context mContext){
        PermissionUtils.checkAndRequestPermission(mContext, PERMISSION_CAMERA, REQUEST_CODE_CAMERA,
                new PermissionUtils.PermissionRequestSuccessCallBack() {
                    @Override
                    public void onHasPermission() {
                        // 权限已被授予
                        Intent intent = new Intent(mContext, CameraActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    // 普通申请多个权限
    private void requestMorePermissions(final Context mContext){
        PermissionUtils.checkAndRequestMorePermissions(mContext, PERMISSIONS, REQUEST_CODE_PERMISSIONS,
                new PermissionUtils.PermissionRequestSuccessCallBack() {
                    @Override
                    public void onHasPermission() {
                        // 权限已被授予
                        Intent intent = new Intent(mContext, CameraActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    /**
     * 显示前往应用设置Dialog
     *
     */
    private void showToAppSettingDialog() {
        new AlertDialog.Builder(PermissionActivity.this)
                .setTitle("需要权限")
                .setMessage("我们需要相关权限，才能实现功能，点击前往，将转到应用的设置界面，请开启应用的相关权限。")
                .setPositiveButton("前往", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionUtils.toAppSetting(PermissionActivity.this);
                    }
                })
                .setNegativeButton("取消", null).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                if(PermissionUtils.isPermissionRequestSuccess(grantResults))
                {
                    // 权限申请成功
                    Intent intent = new Intent(PermissionActivity.this, CameraActivity.class);
                    startActivity(intent);

                } else {
                    Toast.makeText(PermissionActivity.this,"打开相机失败",Toast.LENGTH_SHORT).show();
                }
                finish();
                break;
            case REQUEST_CODE_PERMISSIONS:
                PermissionUtils.onRequestMorePermissionsResult(PermissionActivity.this, PERMISSIONS, new PermissionUtils.PermissionCheckCallBack() {
                    @Override
                    public void onHasPermission() {
                        // 权限申请成功
                        Intent intent = new Intent(PermissionActivity.this, CameraActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onUserHasAlreadyTurnedDown(String... permission) {
                        Toast.makeText(PermissionActivity.this, "我们需要"+ Arrays.toString(permission)+"权限", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                        Toast.makeText(PermissionActivity.this, "我们需要"+ Arrays.toString(permission)+"权限", Toast.LENGTH_SHORT).show();
                        showToAppSettingDialog();
                    }
                });

        }
    }

}
