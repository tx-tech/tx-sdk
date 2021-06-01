package com.txt.video;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;


import com.txt.video.net.bean.TxConfig;
import com.txt.video.net.http.HttpRequestClient;
import com.txt.video.net.http.SystemHttpRequest;
import com.txt.video.net.utils.TxLogUtils;
import com.txt.video.ui.trtc.TICManager;
import com.txt.video.widget.callback.StartVideoResultOnListener;
import com.txt.video.widget.callback.onCreateRoomListener;
import com.txt.video.widget.utils.PermissionConstants;
import com.txt.video.widget.utils.PermissionUtils;
import com.txt.video.widget.utils.Utils;
import com.txt.video.ui.video.VideoActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * Created by JustinWjq
 *
 * @date 2020/8/19.
 * description：暴露功能类
 */
public class TXSdk extends TXSDKApi {
    private static volatile TXSdk singleton = null;

    private boolean isDebug = true;

    private Environment environment = Environment.TEST;

    private String wxKey = "";

    private TxConfig txConfig;

    private boolean isDemo = false;

    private String SDKVersion = "v1.1.2";

    private String terminal = "Android";

    private String wxTransaction = "";

    private String agent;

    private boolean isShare;


    @Override
    public boolean getShare() {
        return isShare;
    }

    @Override
    public void setShare(boolean share) {
        isShare = share;
    }

    @Override
    public String getAgent() {
        return agent;
    }

    @Override
    public void setAgent(String agent) {
        this.agent = agent;
    }

    @Override
    public String getTerminal() {
        return terminal;
    }

    @Override
    public String getSDKVersion() {
        return SDKVersion;
    }

    @Override
    public boolean isDemo() {
        return isDemo;
    }

    @Override
    public void setDemo(boolean demo) {
        isDemo = demo;
    }

    @Override
    public String getWxTransaction() {
        return wxTransaction;
    }

    @Override
    public void setWxTransaction(String wxTransaction) {
        this.wxTransaction = wxTransaction;
    }

    @Override
    public TxConfig getTxConfig() {
        if (txConfig == null) {
            txConfig = new TxConfig();
        }
        return txConfig;
    }

    @Override
    public void setTxConfig(TxConfig txConfig) {
        this.txConfig = txConfig;
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public boolean isDebug() {
        return isDebug;
    }

    @Override
    public void setDebug(boolean debug) {
        isDebug = debug;
    }


    private TXSdk() {

    }

    public Application application;

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public void init(Application application) {
        init(application, environment, isDebug);
    }

    public void init(Application application, Environment en, boolean isDebug) {
        this.application = application;
        this.isDebug = isDebug;
        if (txConfig == null) {
            txConfig = new TxConfig();
        }
        checkoutNetEnv(en);
        Utils.init(application);
    }

    @Override
    public void init(Application application, Environment en, boolean isDebug, TxConfig txConfig) {
        this.txConfig = txConfig;
        init(application, en, isDebug);
    }

    @Override
    public void checkoutNetEnv(Environment en) {
        setEnvironment(en);
        SystemHttpRequest.getInstance().changeIP(en);
    }

    @Override
    public void startTXVideo(final Activity context, final String agent, String orgAccount, String sign, JSONObject businessData, final StartVideoResultOnListener listener) {
        TXManager.getInstance().checkPermission(context, agent, orgAccount, sign, businessData, listener);
    }

    public void startTXVideo(final Activity context, final String agent, String orgAccount, String sign, final StartVideoResultOnListener listener) {
        TXManager.getInstance().checkPermission(context, agent, orgAccount, sign, null, listener);
    }

    @Override
    public void createRoom(final String agent, String orgAccount, String sign, final onCreateRoomListener listener) {
        TXManager.getInstance().createRoom(agent,orgAccount,sign,null,null,listener);
    }

    @Override
    public void createRoom(String agent, String orgAccount, String sign, JSONObject roomInfo, onCreateRoomListener listener) {
        TXManager.getInstance().createRoom(agent,orgAccount,sign,roomInfo,null,listener);
    }

    @Override
    public void createRoom(String agent, String orgAccount, String sign, JSONObject roomInfo, JSONObject businessData, onCreateRoomListener listener) {
        TXManager.getInstance().createRoom(agent,orgAccount,sign,roomInfo,businessData,listener);
    }

    @Override
    public void joinRoom(final Activity context, String roomId, String userName,JSONObject businessData,  final StartVideoResultOnListener listener) {
        TXManager.getInstance().checkPermission(context, roomId, userName, null, businessData, listener);
    }

    public void unInit() {
        if (TICManager.getInstance() != null)
            TICManager.getInstance().unInit();

    }

    public enum Environment {
        DEV,
        TEST,
        RELEASE
    }

    public interface TXSDKErrorCode{
        int TXSDK_ERROR_INVITENUMBER_INVALID = 1;

    }

    public static TXSdk getInstance() {
        if (singleton == null) {
            synchronized (TXSdk.class) {
                if (singleton == null) {
                    singleton = new TXSdk();
                }
            }
        }
        return singleton;
    }

}
