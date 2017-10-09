package com.cheguo.camera.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by huchao on 2017/8/21.
 */
public class CustomViewPager extends ViewPager {

    private boolean scrollble = true;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!scrollble) {
            return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {

        try {
            if (!scrollble)
                return false;
            else
                return super.onInterceptTouchEvent(arg0);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }

    }


    public boolean isScrollble() {
        return scrollble;
    }

    public void setScrollble(boolean scrollble) {
        this.scrollble = scrollble;
    }

}
