package com.natchi.demosdk

//import com.txt.video.widget.utils.ToastUtils
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.SimpleCallback
import com.txt.video.TXSdk
import com.txt.video.widget.callback.StartVideoResultOnListener
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        if (BuildConfig.DEBUG)
            et.setText("wjqdev123")

        et_account.setText(
            when (TXSdk.getInstance().environment) {
                TXSdk.Environment.DEV, TXSdk.Environment.TEST -> "gscyf_test"
                else -> "gsc_test"
            }
        )
        bt.setOnClickListener {
//            TxLogUtils.i("AndroidSystemUtil.getDevice"+AndroidSystemUtil.getDevice())
            startSDK()

        }
        tv_config.setOnClickListener(this)
        check_bt.setOnClickListener(this)

        changeUI()
    }


    @SuppressLint("SetTextI18n")
    fun changeUI() {
        et_account.setText(
            when (TXSdk.getInstance().environment) {
                TXSdk.Environment.DEV, TXSdk.Environment.TEST -> "test_org2"
                else -> "gsc_test"
            }
        )
        var sdkVersion=  "SDK："+TXSdk.getInstance().sdkVersion
        var appEnv = sdkVersion+"\n"+ when (TXSdk.getInstance().environment) {
            TXSdk.Environment.DEV -> "App：开发环境"
            TXSdk.Environment.TEST -> "App：测试环境"
            else -> "App：正式环境"
        }
        tv_dep.text = appEnv +"\n"+  when (TXSdk.getInstance().txConfig.miniprogramType) {
            TXSdk.Environment.DEV -> {
                "小程序：开发版本"
            }
            TXSdk.Environment.TEST -> {
                "小程序：体验版本"
            }
            else -> "小程序：正式版本"
        }
        var appEnv1 = when (TXSdk.getInstance().environment) {
            TXSdk.Environment.DEV -> "/开发环境"
            TXSdk.Environment.TEST -> "/测试环境"
            else -> "/正式环境"
        }
        check_bt.text = when (TXSdk.getInstance().txConfig.miniprogramType) {
            TXSdk.Environment.DEV -> {
                "开发版本"+appEnv1
            }
            TXSdk.Environment.TEST -> {
                "体验版本"+appEnv1
            }
            else -> "正式版本$appEnv1"
        }
    }


    private fun startSDK() {
        val loginName = et.text.toString()
        val orgAccount = et_account.text.toString()
//        val orgAccount = "gscjg"
        if (loginName.isEmpty()) {
            Toast.makeText(this@MainActivity, "请填入账号！", Toast.LENGTH_SHORT).show()
            return
        }
        if (orgAccount.isEmpty()) {
            Toast.makeText(this@MainActivity, "请填入组织代码！", Toast.LENGTH_SHORT).show()
            return
        }
        val l = System.currentTimeMillis() / 1000
        Log.i("currentTimeMillis", "" + l)
        val encrypt: String = SignUtils.Encrypt(orgAccount + "" + l)
        val businessData = JSONObject().apply {
            put("latitude", 123.1231231)
            put("longitude", 21.123123)
            put("accuracy", 1000)
            put("province", "上海市")
            put("city", "上海市")
            put("adr", "上海市")
        }
        TXSdk.getInstance().startTXVideo(
            this@MainActivity,
            loginName,
            orgAccount,
            encrypt,
            businessData,
            object :
                StartVideoResultOnListener {
                override fun onResultSuccess() {

                }

                override fun onResultFail(errCode: Int, errMsg: String) {
                    Toast.makeText(this@MainActivity, "$errCode-$errMsg", Toast.LENGTH_SHORT).show()
                }

            })

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_config, R.id.check_bt -> {
                XPopup.Builder(this)
                    .setPopupCallback(object : SimpleCallback() {
                        override fun onDismiss() {
                            super.onDismiss()
                            changeUI()

                        }
                    })
                    .hasStatusBarShadow(true)
                    .autoOpenSoftInput(true)
                    .asCustom(CustomFullScreenPopup(this))
                    .show()
            }
            else -> {
            }
        }
    }


}