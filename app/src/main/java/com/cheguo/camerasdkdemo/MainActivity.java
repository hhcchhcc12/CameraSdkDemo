package com.cheguo.camerasdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.cheguo.camera.CameraActivity;

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
//                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
//                intent.putExtra("test","test");
//                startActivity(intent);
                CameraActivity.launch(MainActivity.this, 1440, 2560, resultCode,MainActivity.this);
//                Intent intent = new Intent(MainActivity.this,CameraActivity.class);
//                intent.putExtra(CameraUtil.PARAMS_IMAGE_WIDTH,1440);
//                intent.putExtra(CameraUtil.PARAMS_IMAGE_HEIGHT,2560);
//                intent.putExtra(CameraUtil.PARAMS_RESULTCODE,1);
//                startActivityForResult(intent,1);
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
