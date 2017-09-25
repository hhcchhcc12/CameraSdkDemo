package com.cheguo.camera.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.SeekBar;

import com.cheguo.camera.R;

/**
 * Created by huchao on 2017/9/25.
 * Description :
 */

public class ISOSeekBarPopwindow  extends PopupWindow {
    private View conentView;
    private SeekBar seekBar;
    private SeekCallback callback;

    public ISOSeekBarPopwindow(final Activity context,SeekBarParams seekBarParams,SeekCallback callback) {
        this.callback = callback;
        LayoutInflater inflater = LayoutInflater.from(context);
        conentView = inflater.inflate(R.layout.iso_popwindow_layout, null);
        seekBar = (SeekBar) conentView.findViewById(R.id.iso_seekbar);
        initSeekBar(seekBarParams);
        int h = context.getWindowManager().getDefaultDisplay().getHeight();
        int w = context.getWindowManager().getDefaultDisplay().getWidth();
        // 设置SelectPicPopupWindow的View
        this.setContentView(conentView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(w / 2 + 200);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        // 刷新状态
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);
        // mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        //this.setAnimationStyle(R.style.AnimationPreview);

    }

    private void initSeekBar(SeekBarParams seekBarParams){
        final int abs_min = Math.abs(seekBarParams.min);
        seekBar.setMax(seekBarParams.max + abs_min);
        seekBar.setProgress(seekBarParams.cur + abs_min);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                //cameraView.adjustZoom(arg1);
                int exposure = arg1 - abs_min;
                if(callback != null){
                    callback.progressChanged(exposure);
                }
//                cameraView.setCustomExposureCompensation(exposure);
//                cameraView.focus(null);

            }
        });
    }

    public interface SeekCallback{
        void progressChanged(int value);
    }

    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            this.showAsDropDown(parent, parent.getLayoutParams().width / 2, 50);
        } else {
            this.dismiss();
        }
    }
}
