package com.txt.video.base;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;


/**
 * Created by JustinWjq
 * @date 2019-12-23.
 * description：
 */
public abstract class BaseMVPActivity<V, P extends BasePresenter<V>> extends BaseActivity {

    protected P mPresenter;


    /**
     * 获取布局id
     * @return 当前需要加载的布局
     */
    protected abstract int getContentViewId();

    /**
     * 初始化
     * @param savedInstanceState
     */
    protected abstract void init(Bundle savedInstanceState);

    /**
     * 创建Presenter
     * @return 返回当前Presenter
     */
    protected abstract P createPresenter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());
        mPresenter = createPresenter();
        if (mPresenter != null) {
            mPresenter.attachView((V) this);
        }
        init(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }
}
