package com.txt.video.widget.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.txt.video.R;
import com.txt.video.widget.utils.ToastUtils;

/**
 * @author ：Justin
 * time ：2021/3/1.
 * des ： 显示大屏幕的view
 */
public class ScreenView extends ConstraintLayout implements View.OnClickListener {
    public ScreenView(@NonNull Context context) {
        this(context, null);
    }

    public ScreenView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.tx_view_screenview, this);
        initView(this);
    }

    public ScreenView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    FrameLayout bigscreen;
    Group mGroup;
    ImageButton mVideoIB, mAudioIB, mSwitchIB, mHangUpIB;
    private BigScreenViewCallback mBigScreenViewCallback;

    public void initView(View itemView) {
        bigscreen = itemView.findViewById(R.id.view_merge_bigscreen);
        mVideoIB = itemView.findViewById(R.id.tx_merge_business_video);
        mAudioIB = itemView.findViewById(R.id.tx_merge_business_audio);
        mSwitchIB = itemView.findViewById(R.id.tx_merge_business_switch);
        mHangUpIB = itemView.findViewById(R.id.tx_merge_business_hangup);
        mGroup = itemView.findViewById(R.id.tx_merge_group);

        bigscreen.setOnClickListener(this);
        mVideoIB.setOnClickListener(this);
        mAudioIB.setOnClickListener(this);
        mSwitchIB.setOnClickListener(this);
        mHangUpIB.setOnClickListener(this);

    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility==View.VISIBLE){
            initGroup();
        }else{

        }
    }

    public void initGroup(){
        if (mGroup!=null){
            mGroup.setVisibility(GONE);
            ToastUtils.showLong("单击屏幕后,按钮显示！");
        }
    }

    public FrameLayout getBigScreenView() {
        return bigscreen;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (null != mBigScreenViewCallback) {
            if (id == R.id.tx_merge_business_video) {
                mBigScreenViewCallback.onMuteVideoClick();
            } else if (id == R.id.tx_merge_business_audio) {
                mBigScreenViewCallback.onMuteAudioClick();
            } else if (id == R.id.tx_merge_business_switch) {
                mBigScreenViewCallback.onSwitch();
            } else if (id == R.id.tx_merge_business_hangup) {
                mBigScreenViewCallback.onFinishClick();
            } else if (id == R.id.view_merge_bigscreen) {
                if (mGroup.getVisibility() == View.VISIBLE) {
                    mGroup.setVisibility(View.GONE);
                    ToastUtils.showLong("单击屏幕后,按钮显示！");
                } else {
                    mGroup.setVisibility(View.VISIBLE);
                    ToastUtils.showLong("单击屏幕后,按钮隐藏！");
                }
            }
        }

    }

    public void setBigScreenCallBack(BigScreenViewCallback bigScreenViewCallback) {
        this.mBigScreenViewCallback = bigScreenViewCallback;
    }

    public void muteVideo(boolean isMute) {
        mVideoIB.setSelected(isMute);
    }

    public void muteAudio(boolean isMute) {
        mAudioIB.setSelected(isMute);
    }


    public interface BigScreenViewCallback {
        void onFinishClick();

        void onSwitch();

        void onMuteAudioClick();

        void onMuteVideoClick();
    }

}
