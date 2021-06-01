package com.txt.video.widget.callback

/**
 * Created by JustinWjq
 * @date 2020/8/28.
 * description：
 */
open interface onDialogListenerCallBack {
    fun onConfirm()
    fun onFile()
    fun onItemClick(url: String?, images: MutableList<String>)
    fun onItemLongClick(id: String?)
    fun dismiss()
}