package com.txt.video.base;

/**
 * Created by JustinWjq
 * @date 2019-12-23.
 * description：
 */
public interface IBaseView {

    /**
     * 加载中
     */
    void onLoading();

    /**
     * 加载错误回调
     */
    void onLoadFailed();

    /**
     * 加载完成
     */
    void onLoadSuccess();
}
