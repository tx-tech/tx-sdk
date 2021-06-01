package com.txt.video.widget.floatview

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Binder
import android.os.Build
import android.os.IBinder

/**
 * Created by JustinWjq
 * @date 2020/11/16.
 * description：
 */
class FloatVideoWindowService : Service() {


    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onBind(intent: Intent?): IBinder? {

        return MyBinder()
    }

    public class MyBinder : Binder() {
        public fun getService(): FloatVideoWindowService {
            return FloatVideoWindowService()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }






}