package com.txt.myapplication

import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import com.lxj.xpopup.impl.FullScreenPopupView
import com.lxj.xpopup.util.XPopupUtils
import com.txt.video.TXSdk

/**
 * Created by JustinWjq
 * @date 2020/12/10.
 * description：
 */
public class CustomFullScreenPopup constructor(context: Context) : FullScreenPopupView(context){

    override fun getImplLayoutId(): Int {
        return R.layout.layout_customfullscreen
    }
    override fun onCreate() {
        super.onCreate()
        val rgMimpro = findViewById<RadioGroup>(R.id.rg_mimpro)
        val rg_tx_app = findViewById<RadioGroup>(R.id.rg_tx_app)
        val rg_tx_isshowbt = findViewById<RadioGroup>(R.id.rg_tx_isshowbt)
        val rg_tx_isshowtempbt = findViewById<RadioGroup>(R.id.rg_tx_isshowtempbt)
        val et_mimpro_title = findViewById<EditText>(R.id.et_mimpro_title)
        val et_mimpro_card = findViewById<EditText>(R.id.et_mimpro_card)
        val et_mimpro_username = findViewById<EditText>(R.id.et_mimpro_username)
        val btConfirm = findViewById<Button>(R.id.bt_confirm)
        btConfirm.setOnClickListener {
            TXSdk.getInstance().txConfig.miniprogramType  =when (rgMimpro.checkedRadioButtonId) {
                R.id.minpro_test -> {
                    TXSdk.Environment.TEST
                }
                R.id.minpro_dev -> {
                    TXSdk.Environment.DEV
                }
                R.id.minpro_rel -> {
                    TXSdk.Environment.RELEASE
                }
                else -> {
                    TXSdk.Environment.TEST
                }
            }
            TXSdk.getInstance().checkoutNetEnv(when (rg_tx_app.checkedRadioButtonId) {
                R.id.app_test -> {
                    TXSdk.Environment.TEST
                }
                R.id.app_dev -> {
                    TXSdk.Environment.DEV
                }
                R.id.app_rel -> {
                    TXSdk.Environment.RELEASE
                }
                else -> {
                    TXSdk.Environment.TEST
                }
            })
            TXSdk.getInstance().txConfig.isShowInviteButton  =when (rg_tx_isshowbt.checkedRadioButtonId) {
                R.id.isshowbt_yes -> {
                    true
                }
                R.id.isshowbt_no -> {
                   false
                }
                else ->true
            }
            TXSdk.getInstance().txConfig.isShowTemporaryButton = when (rg_tx_isshowtempbt.checkedRadioButtonId) {
                R.id.isshowtempbt_yes -> {
                    true
                }
                R.id.isshowtempbt_no -> {
                    false
                }
                else -> {
                    true
                }
            }
            val etMimproTitleStr = et_mimpro_title.text.toString()
            val etMimproCardStr = et_mimpro_card.text.toString()
            val etMimproUsername = et_mimpro_username.text.toString()
            if (etMimproTitleStr.isNotEmpty()){
                TXSdk.getInstance().txConfig.miniprogramTitle=etMimproTitleStr
            }

            if (etMimproCardStr.isNotEmpty()){
                TXSdk.getInstance().txConfig.miniprogramCard = etMimproCardStr
            }

            if (etMimproUsername.isNotEmpty()){
                TXSdk.getInstance().txConfig.userName = etMimproUsername
            }


            dismiss()
        }


        when (TXSdk.getInstance().txConfig.miniprogramType) {
            TXSdk.Environment.TEST-> {
                rgMimpro.check( R.id.minpro_test)
            }
            TXSdk.Environment.DEV-> {
                rgMimpro.check( R.id.minpro_dev)
            }
            TXSdk.Environment.RELEASE-> {
                rgMimpro.check( R.id.minpro_rel)
            }
        }

        when (TXSdk.getInstance().environment) {
            TXSdk.Environment.TEST-> {
                rg_tx_app.check( R.id.app_test)
            }
            TXSdk.Environment.DEV-> {
                rg_tx_app.check( R.id.app_dev)
            }
            TXSdk.Environment.RELEASE-> {
                rg_tx_app.check( R.id.app_rel)
            }
        }

        when (TXSdk.getInstance().txConfig.isShowInviteButton) {
            true-> {
                rg_tx_isshowbt.check( R.id.isshowbt_yes)
            }
            false-> {
                rg_tx_isshowbt.check( R.id.isshowbt_no)
            }

        }

        when (TXSdk.getInstance().txConfig.isShowTemporaryButton) {
            true-> {
                rg_tx_isshowtempbt.check( R.id.isshowtempbt_yes)
            }
            false-> {
                rg_tx_isshowtempbt.check( R.id.isshowtempbt_no)
            }

        }

        et_mimpro_title.setText(TXSdk.getInstance().txConfig.miniprogramTitle)
        et_mimpro_card.setText(TXSdk.getInstance().txConfig.miniprogramCard)
        et_mimpro_username.setText(TXSdk.getInstance().txConfig.userName)
    }

}