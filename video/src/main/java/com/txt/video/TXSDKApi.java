package com.txt.video;

import android.app.Activity;
import android.app.Application;

import com.txt.video.net.bean.TxConfig;
import com.txt.video.widget.callback.StartVideoResultOnListener;
import com.txt.video.widget.callback.onCreateRoomListener;

import org.json.JSONObject;

/**
 * author ：Justin
 * time ：2021/2/25.
 * des ：SDK api说明
 */
public abstract class TXSDKApi {

    /**
     * @note
     */
    public abstract boolean getShare();

    /**
     * @note
     */
    public abstract void setShare(boolean share);


    /**
     * @note
     */
    public abstract String getAgent();

    /**
     * @note
     */
    public abstract void setAgent(String agent);


    /**
     * @note
     */
    public abstract String getTerminal();

    /**
     * @note
     */
    public abstract String getSDKVersion();

    /**
     * @note
     */
    public abstract boolean isDemo();

    /**
     * @note
     */
    public abstract void setDemo(boolean demo);


    /**
     * @note
     */
    public abstract String getWxTransaction();

    /**
     * @param wxTransaction
     *
     * @note
     */
    public abstract void setWxTransaction(String wxTransaction);


    /**
     * 获取可配置信息
     *
     * @note
     */
    public abstract TxConfig getTxConfig();


    /**
     * 设置可配置信息（详情见TxConfig）
     *
     * @param txConfig
     *
     *
     * @note
     */
    public abstract void setTxConfig(TxConfig txConfig);

    /**
     * 获取环境
     *
     * @note
     */
    public abstract TXSdk.Environment getEnvironment();

    /**
     * 设置环境
     *
     * @param environment
     *
     * @note
     */
    public abstract void setEnvironment(TXSdk.Environment environment);

    /**
     * 获取debug状态
     *
     *
     * @note
     */
    public abstract boolean isDebug();

    /**
     * 设置debug状态
     *
     * @param debug
     *
     * @note
     */
    public abstract void setDebug(boolean debug);


    /**
     * 初始化
     *
     * @param application
     * @param en
     * @param isDebug
     * @param txConfig
     *
     * @note
     */
    public abstract void init(Application application, TXSdk.Environment en, boolean isDebug, TxConfig txConfig);


    /**
     * 切换环境
     *
     * @param en
     *
     * @note
     */
    public abstract void checkoutNetEnv(TXSdk.Environment en);

    /**
     * 开始视频
     *
     * @param context
     *
     * @param agent
     * @param orgAccount
     * @param sign
     * @param businessData  额外字段
     * @param listener 操作回调
     *
     * @note
     */
    public abstract void startTXVideo(final Activity context, final String agent, String orgAccount, String sign, JSONObject businessData, final StartVideoResultOnListener listener);


    /**
     * 预约会议
     *
     * @param agent
     *
     * @param orgAccount
     * @param sign
     * @param listener 操作回调
     * @note
     */
    public abstract void createRoom(final String agent, String orgAccount, String sign, final onCreateRoomListener listener);

    /**
     * 预约会议
     *
     * @param agent
     *
     * @param orgAccount
     * @param sign
     * @param roomInfo 房间信息
     * @param listener 操作回调
     * @note
     */
    public abstract void createRoom(final String agent, String orgAccount, String sign,JSONObject roomInfo,  final onCreateRoomListener listener);

    /**
     * 预约会议
     *
     * @param agent
     *
     * @param orgAccount
     * @param sign
     * @param roomInfo 房间信息
     * @param businessData  附加数据
     * @param listener 操作回调
     * @note
     */
    public abstract void createRoom(final String agent, String orgAccount, String sign,JSONObject roomInfo,JSONObject businessData,  final onCreateRoomListener listener);


    /**
     * 加入会议
     *
     * @param context
     *
     * @param roomId 邀请码
     * @param userName
     * @param businessData 额外字段
     * @param listener 操作回调
     * @note
     */
    public abstract void joinRoom(final Activity context, String roomId, String userName,JSONObject businessData, final StartVideoResultOnListener listener);
}
