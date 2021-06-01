package com.txt.video.net.http;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.txt.video.net.bean.TxBean;
import com.txt.video.net.constant.Constant;
import com.txt.video.net.utils.SPUtils;
import com.txt.video.net.utils.TxLogUtils;
import com.txt.video.TXSdk;
import com.txt.video.widget.utils.UriUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SystemHttpRequest {

    private static volatile SystemHttpRequest singleton = null;

    private SystemHttpRequest() {
    }

    public static SystemHttpRequest getInstance() {
        if (singleton == null) {
            synchronized (SystemHttpRequest.class) {
                if (singleton == null) {

                    singleton = new SystemHttpRequest();
                }
            }
        }
        return singleton;
    }

    //    private String IP = "https://developer.ikandy.cn:60723";
    private String IP = "https://video-sells-test.ikandy.cn";

    private String TAG = SystemHttpRequest.class.getSimpleName();

    private Context mContext;

    public void initContext(Context context) {
        mContext = context;
    }


    public void changeIP(TXSdk.Environment environment) {
        switch (environment) {
            case DEV:
                IP = "https://developer.ikandy.cn:60723";
                break;
            case RELEASE:
                IP = "https://video-sells.cloud-ins.cn";
                break;
            default:
                IP = "https://video-sells-test.ikandy.cn";
        }

    }

    public void uploadFile(String filePath, String serviceId,
                           HttpRequestClient.RequestHttpCallBack callback, HttpRequestClient.UploadProgressListener back) {
        StringBuilder builder = new StringBuilder(IP + "/api/file");

        TxLogUtils.i("filePath" + filePath);
        TxLogUtils.i("serviceId" + serviceId);
        if (TextUtils.isEmpty(filePath)) {
            callback.onFail("upload file is null", -2);
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            callback.onFail("upload file is null", -2);
            return;
        }
        Map<String, String> stringStringMap = new HashMap<>();
        stringStringMap.put("agent", serviceId);
        HttpRequestClient.getIntance().postFile(builder.toString(), stringStringMap, file, callback, back);
    }

    public void cancleClient() {
        HttpRequestClient.getIntance().cancleClient();
    }


    //上传图片文件
    public void uploadLogFile(Uri fileName, String serviceId, onFileCallBack onFileCallBack, final onRequestCallBack callBack, HttpRequestClient.UploadProgressListener back) {
        try {
            File file = UriUtils.uri2File(fileName);
            TxLogUtils.i("fileName: " + fileName);
            if (fileName == null || fileName.equals("")) {
                TxLogUtils.d(TAG, "uploadLogFile: file is null");
                if (callBack != null) {
                    callBack.onFail("文件为空！");
                }
                return;
            }
            //        File file = new File(fileName);
            if (null == file || !file.exists()) {
                if (callBack != null) {
                    callBack.onFail("文件为空或者不存在！！！");
                }
                return;
            }
            TxLogUtils.i("uploadLogFile: " + file.getAbsolutePath());
            uploadFile(file.getAbsolutePath(), serviceId, new HttpRequestClient.RequestHttpCallBack() {
                @Override
                public void onSuccess(String json) {
                    TxLogUtils.i("onSuccess: : onSuccess" + json);
                    if (callBack != null) {
                        callBack.onSuccess();
                    }
                }

                @Override
                public void onFail(String err, int code) {
                    TxLogUtils.i("err" + err + "---" + code);
                    if (callBack != null) {
                        callBack.onFail(err);
                    }

                }
            }, back);
        } catch (Exception e) {
            if (callBack != null) {
                callBack.onFail("获取不到文件权限！");
            }
        }

    }


    public void postRequest(String api, String json, HttpRequestClient.RequestHttpCallBack callback) {
        String authorization = (String) SPUtils.get(mContext, Constant.Authorization, "");
        HttpRequestClient.getIntance().post(IP + api, json, "Bearer " + authorization, callback);
    }


    //代理人进入房间
    public void startAgent(String loginName, String orgAccount, String sign, JSONObject businessData, HttpRequestClient.RequestHttpCallBack callback) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("agent", loginName);
            jsonObject.put("orgAccount", orgAccount);
            jsonObject.put("sign", sign);

            if (null != businessData) {
                jsonObject.put("extraData", businessData);
            }

        } catch (Exception e) {

        }

        HttpRequestClient.getIntance().post(IP + "/api/serviceRoom/startAgent", jsonObject.toString(), "", callback);
    }

    public void createRoom(String loginName, String orgAccount, String sign, HttpRequestClient.RequestHttpCallBack callback) {
        createRoom(loginName, orgAccount, sign, null, null, callback);
    }

    public void createRoom(String loginName, String orgAccount, String sign, JSONObject roomInfo, HttpRequestClient.RequestHttpCallBack callback) {
        createRoom(loginName, orgAccount, sign, roomInfo, null, callback);
    }

    //创建房间
    public void createRoom(String loginName, String orgAccount, String sign, JSONObject roomInfo, JSONObject businessData, HttpRequestClient.RequestHttpCallBack callback) {
        JSONObject jsonObject = new JSONObject();


        try {
            jsonObject.put("account", loginName);
            jsonObject.put("orgAccount", orgAccount);
            jsonObject.put("sign", sign);

            if (null != roomInfo) {
                jsonObject.put("roomInfo", roomInfo);
            }

            if (null != businessData ) {
                jsonObject.put("extraData", businessData);
            }
        } catch (Exception e) {

        }

        HttpRequestClient.getIntance().post(IP + "/api/serviceRoom/create", jsonObject.toString(), "", callback);
    }

    //用户进入房间
    public void startUser(String roomId, String userId, JSONObject businessData, HttpRequestClient.RequestHttpCallBack callback) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("inviteNumber", roomId);
            jsonObject.put("userId", userId);
            if (null != businessData) {
                jsonObject.put("extraData", businessData);
            }
        } catch (Exception e) {

        }

        HttpRequestClient.getIntance().post(IP + "/api/serviceRoom/startUser", jsonObject.toString(), "", callback);
    }


    public void startShare(String serviceId, HttpRequestClient.RequestHttpCallBack callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serviceId", serviceId);
        } catch (Exception e) {

        }
        HttpRequestClient.getIntance().post(IP + "/api/serviceRoom/startShare", jsonObject.toString(), "", callback);

    }


    public void startRecord(String serviceId, HttpRequestClient.RequestHttpCallBack callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serviceId", serviceId);
        } catch (Exception e) {

        }
        HttpRequestClient.getIntance().post(IP + "/api/serviceRoom/startRecord", jsonObject.toString(), "", callback);

    }

    public void deleteFile(String fileId, String agentId, HttpRequestClient.RequestHttpCallBack callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", fileId);
            jsonObject.put("agentId", agentId);
        } catch (Exception e) {

        }
        HttpRequestClient.getIntance().delete(IP + "/api/file", jsonObject.toString(), "", callback);

    }

    public void endRecord(String serviceId, HttpRequestClient.RequestHttpCallBack callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serviceId", serviceId);
        } catch (Exception e) {

        }
        HttpRequestClient.getIntance().post(IP + "/api/serviceRoom/endRecord", jsonObject.toString(), "", callback);
    }

    public void screenStatus(String serviceId, boolean screenStatus, HttpRequestClient.RequestHttpCallBack callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serviceId", serviceId);
            jsonObject.put("screenStatus", screenStatus);
        } catch (Exception e) {

        }
        HttpRequestClient.getIntance().post(IP + "/api/serviceRoom/screenStatus", jsonObject.toString(), "", callback);
    }


    //获取文件列表接口
    public void getAgent(String agent, HttpRequestClient.RequestHttpCallBack callback) {

        HttpRequestClient.getIntance().get(IP + "/api/files/list?agent=" + agent + "&pageSize=100", callback);
    }

    public void getRoomInfo(String serviceId, HttpRequestClient.RequestHttpCallBack callback) {

        HttpRequestClient.getIntance().get(IP + "/api/serviceRoom/roomInfo/" + serviceId, callback);
    }

    public void extendTime(String serviceId, HttpRequestClient.RequestHttpCallBack callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serviceId", serviceId);
        } catch (Exception e) {

        }
        HttpRequestClient.getIntance().post(IP + "/api/serviceRoom/extendTime", jsonObject.toString(), "", callback);

    }

    public interface onRequestCallBack {
        public void onSuccess();

        public void onFail(String msg);
    }

    public interface onFileCallBack {
        public void onFile(long size, int time);


    }
}
