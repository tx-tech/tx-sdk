package com.txt.video.widget.callback

/**
 * Created by JustinWjq
 * @date 2020/8/28.
 * description：
 */
open interface onExitDialogListenerCallBack {
    fun onConfirm()

    fun onTemporarilyLeave()

    fun end(){}
}