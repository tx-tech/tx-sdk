package com.txt.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_meeting.*

class MeetingActivity : AppCompatActivity() {
    enum class TYPE{
        CREATEROOM,JOINROOM,RESERVATIONROOM
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting)

    }

    public fun onMeetClick(v: View) {
      val type =  when (v.id) {
            R.id.tv_createroom -> {
                "1"
            }
            R.id.tv_joinroom -> {
                "2"
            }
            R.id.tv_reservationroom -> {
                "3"
            }
            else -> {
                "1"
            }
        }

        MainActivity.gotoActivity(this,type)
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode ==12301) {
            val roomId = data?.getStringExtra("roomId")
            tx_roomid.text = "邀请码：$roomId"
        }
    }
}