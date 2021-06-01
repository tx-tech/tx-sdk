package com.txt.video.widget.callback

/**
 * Created by JustinWjq
 * @date 2020/8/28.
 * description：
 */
open interface onCreateRoomListener {
    fun onResultSuccess(roomId:String,serviceId:String)
    fun onResultFail(errCode:Int,errMsg:String)
}