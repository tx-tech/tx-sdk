package com.txt.video.net.bean

import com.txt.video.TXSdk

/**
 * Created by JustinWjq
 * @date 2020/9/24.
 * description：
 */

public data class TxConfig(

    @JvmField var wxKey: String = "",

    @JvmField var miniprogramType  : TXSdk.Environment= TXSdk.Environment.TEST,

    @JvmField var  userName: String = "",

    //是否展示邀请按钮
    @JvmField var  isShowInviteButton: Boolean = false,

    //小程序的导航栏标题
    @JvmField var  miniprogramTitle: String = "",

    //分享的小程序卡片文字
    @JvmField var  miniprogramCard: String = "",

    //分享的小程序卡片文字
    @JvmField var  isShowTemporaryButton: Boolean = false
)

