package com.cheguo.camerasdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.cheguo.camera.CameraActivity;
import com.cheguo.camera.CameraUtils;
import com.cheguo.camera.helper.PhotoParamsEntity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraActivity.ImageCallback{

    private int resultCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pic_dir = CameraUtils.getSDCardAbsolutePath() +
                        CameraUtils.getAppProcessName(MainActivity.this) + "/newTest" + "/";

                PhotoParamsEntity entity = new PhotoParamsEntity();
                entity.width = 720;
                entity.height = 960;
                entity.picDir = pic_dir;

                CameraActivity.launch(MainActivity.this, entity, resultCode,MainActivity.this);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == this.resultCode){
            List<String> listImage = data.getStringArrayListExtra(CameraActivity.PARAMS_IMAGE_LIST);
            if(listImage != null){
                for (String path:listImage) {
                    Log.e("MainActivity","path : "+path);
                }
            }
        }
    }

    @Override
    public void setImagePaths(ArrayList<String> listImage) {
        if(listImage != null){
            for (String path:listImage) {
                Log.e("MainActivity","path : "+path);
            }
        }
    }
}
