package com.txt.video.widget.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;


/**
 * @author ：Justin
 * time ：2021/3/1.
 * des ： 自动显示和隐藏的 TextView
 */
public final class SmartTextView extends AppCompatTextView {

    public SmartTextView(Context context) {
        this(context, null);
    }

    public SmartTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public SmartTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        super.setText(text, type);
        // 判断当前有没有设置文本达到自动隐藏和显示的效果
        if (TextUtils.isEmpty(text) && getVisibility() != GONE) {
            setVisibility(GONE);
            return;
        }

        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
        }
    }
}