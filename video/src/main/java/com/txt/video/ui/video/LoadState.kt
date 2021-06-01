package com.txt.video.ui.video

/**
 * Created by JustinWjq
 * @date 2019-12-23.
 * description：
 */
sealed class LoadState<out T> {
    class Loading(val loadingMessage: String) : LoadState<Nothing>()
    class Success<out T>(val data: T) : LoadState<T>()
    class Error(val error: String) : LoadState<Nothing>()
}