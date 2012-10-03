package com.threeDBJ.numberSlider;

import android.widget.Button;
import android.content.Context;

import android.util.AttributeSet;

public class GameButton extends Button {

    numberSliderActivity mActivity;

    public GameButton(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public void setActivity(numberSliderActivity activity) {
	mActivity = activity;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if((w != 0) && (h != 0) && mActivity != null) {
	    mActivity.setTranslate(w, h);
	}
    }
}
