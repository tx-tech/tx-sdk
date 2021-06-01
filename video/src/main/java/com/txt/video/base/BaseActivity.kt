package com.txt.video.base

import android.app.Activity
import android.content.res.Resources
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.gyf.immersionbar.ImmersionBar
import com.txt.video.R

abstract class BaseActivity : AppCompatActivity() {

    /**
     * 是否需要使用DataBinding 供子类BaseVmDbActivity修改，用户请慎动
     */


//    abstract fun layoutId(): Int
//
//    abstract fun initView(savedInstanceState: Bundle?)
//
//    abstract fun showLoading(message: String = "请求网络中...")
//
//    abstract fun dismissLoading()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(layoutId())
//        initView(savedInstanceState)
    }



    /**
     * 在任何情况下本来适配正常的布局突然出现适配失效，适配异常等问题，只要重写 Activity 的 getResources() 方法
     */
    override fun getResources(): Resources {
//        AutoSizeCompat.autoConvertDensityOfGlobal(super.getResources())
        return super.getResources()
    }

    protected open fun openImmersionBar() {
        ImmersionBar.with(this)
            .reset()
            .navigationBarColor(R.color.tx_white)
            .transparentStatusBar()
            .statusBarDarkFont(true)
            .navigationBarDarkIcon(true)
            .keyboardEnable(false)
            .init()
    }

    protected open fun closeImmersionBar() {
        ImmersionBar.with(this)
            .reset()
            .statusBarColor(R.color.tx_white)
            .navigationBarColor(R.color.tx_white)
            .statusBarDarkFont(true)
            .navigationBarDarkIcon(true)
            .keyboardEnable(false)
            .init()
    }






}