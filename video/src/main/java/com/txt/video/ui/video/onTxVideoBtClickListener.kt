package com.txt.video.ui.video

import com.txt.video.TXSdk

/**
 * author ：Justin
 * time ：2021/3/29.
 * des ：按钮回调
 */
open interface  onTxVideoBtClickListener{
    fun onTxVideoBtClick(btType: TXSdk.VideoBtType)
}