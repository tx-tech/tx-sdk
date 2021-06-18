package com.txt.video.net.bean

import com.txt.video.TXSdk

/**
 * Created by JustinWjq
 * @date 2020/9/24.
 * description：
 */

public data class TxConfig(

    @JvmField var wxKey: String = "",

    @JvmField var miniprogramType: TXSdk.Environment = TXSdk.Environment.TEST,

    @JvmField var userName: String = "",

    //是否展示邀请按钮
    @JvmField var isShowInviteButton: Boolean = false,

    //小程序的导航栏标题
    @JvmField var miniprogramTitle: String = "",

    //分享的小程序卡片文字
    @JvmField var miniprogramCard: String = "",

    //分享的小程序卡片文字
    @JvmField var isShowTemporaryButton: Boolean = false,

    //视频按钮
    @JvmField var startVideoTitle: String = "开启视频",
    @JvmField var stopVideoTitle: String = "关闭视频",

    //静音按钮
    @JvmField var startAudioTitle: String = "开启静音",
    @JvmField var stopAudioTitle: String = "关闭静音",

    //反转镜头按钮
    @JvmField var switchVideoTitle: String = "反转镜头",

    //开启投屏
    @JvmField var startScreenTitle: String = "开启投屏",
    @JvmField var stopScreenTitle: String = "关闭投屏",

    //显示浮窗
    @JvmField var showFloatTitle: String = "显示浮窗"
)

