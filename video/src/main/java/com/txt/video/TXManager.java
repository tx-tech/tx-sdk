package com.txt.video;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.txt.video.net.http.HttpRequestClient;
import com.txt.video.net.http.SystemHttpRequest;
import com.txt.video.net.utils.TxLogUtils;
import com.txt.video.ui.video.VideoActivity;
import com.txt.video.widget.callback.StartVideoResultOnListener;
import com.txt.video.widget.callback.onCreateRoomListener;
import com.txt.video.widget.utils.PermissionConstants;
import com.txt.video.widget.utils.PermissionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by JustinWjq
 *
 * @date 2021/2/23.
 * description：
 */
public class TXManager {
    private static volatile TXManager singleton = null;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean isQucikEnter = false;

    public void setQucikEnter(boolean qucikEnter) {
        isQucikEnter = qucikEnter;
    }

    public boolean isQuickEnter() {
        return isQucikEnter;
    }

    public static TXManager getInstance() {
        if (singleton == null) {
            synchronized (TXManager.class) {
                if (singleton == null) {
                    singleton = new TXManager();
                }
            }
        }
        return singleton;
    }

    public void checkPermission(final Activity context, final String agent, final String orgAccount, final String sign, final JSONObject businessData, final StartVideoResultOnListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionUtils.permission(
                    PermissionConstants.CAMERA,
                    PermissionConstants.MICROPHONE,
                    PermissionConstants.PHONE
            ).callback(new PermissionUtils.FullCallback() {

                           @Override
                           public void onGranted(List<String> permissionsGranted) {
                               TxLogUtils.i("txsdk---joinRoom---- ");
                               TxLogUtils.i("txsdk---onGranted---- " + permissionsGranted);
                               if (permissionsGranted.contains("android.permission.CAMERA") && permissionsGranted.contains("android.permission.RECORD_AUDIO")) {
                                   if (sign == null) {
                                       enterRoom(context,agent, orgAccount,businessData, listener);
                                   } else {
                                       enterRoom(context, agent, orgAccount, sign, businessData, listener);
                                   }
                               } else {
                                   listener.onResultFail(10000, "视频权限或音频权限未申请！");
                               }


                           }

                           @Override
                           public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                               TxLogUtils.i("txsdk---permissionsDeniedForever ---- " + permissionsDeniedForever);
                               TxLogUtils.i("txsdk---permissionsDenied ---- " + permissionsDenied);
                               if (permissionsDenied.contains("android.permission.CAMERA") || permissionsDenied.contains("android.permission.RECORD_AUDIO")) {
                                   listener.onResultFail(10000, "视频权限或音频权限未申请！");
                               } else {

                               }

                           }
                       }
            ).request();
        } else {
            TxLogUtils.i("txsdk---joinRoom----23以下 ");
            enterRoom(context, agent, orgAccount, sign, businessData, listener);
        }

    }

    private String mCacheJson = "";
    //创建会议
    public void enterRoom(final Activity context, String agent, String orgAccount, String sign, JSONObject businessData, final StartVideoResultOnListener listener) {
        if (TXSdk.getInstance().getShare()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onResultSuccess();
                    VideoActivity.startAc(context, mCacheJson);

                }
            });
        } else {
            SystemHttpRequest.getInstance().startAgent(agent, orgAccount, sign, businessData, new HttpRequestClient.RequestHttpCallBack() {
                @Override
                public void onSuccess(final String json) {
                    mCacheJson = json;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setQucikEnter(true);
                            listener.onResultSuccess();
                            VideoActivity.startAc(context, json);

                        }
                    });


                }

                @Override
                public void onFail(final String err, final int code) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResultFail(code, err);

                        }
                    });
                }
            });
        }
    }

    //加入房间
    public void enterRoom(final Activity context,String roomId, String userName,final JSONObject businessData, final StartVideoResultOnListener listener) {

        SystemHttpRequest.getInstance().startUser(roomId, userName,businessData, new HttpRequestClient.RequestHttpCallBack() {

                    @Override
                    public void onSuccess(final String json) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                setQucikEnter(false);
                                listener.onResultSuccess();
                                VideoActivity.startAc(context, json);

                            }
                        });

                    }

                    @Override
                    public void onFail(final String err, final int code) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onResultFail(code, err);

                            }
                        });
                    }
                }
        );
    }

    public void createRoom(final String agent, String orgAccount, String sign,JSONObject roomInfo, JSONObject businessData, final onCreateRoomListener listener) {
        SystemHttpRequest.getInstance().createRoom(agent, orgAccount, sign,roomInfo,businessData, new HttpRequestClient.RequestHttpCallBack() {

            @Override
            public void onSuccess(final String json) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String inviteNumber = jsonObject.optString("inviteNumber");
                            String serviceId = jsonObject.optString("serviceId");
                            if (inviteNumber.isEmpty()) {
                                listener.onResultFail(0,"");
                            } else {
                                listener.onResultSuccess(inviteNumber,serviceId);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            listener.onResultFail(0,"");
                        }

                    }
                });
            }

            @Override
            public void onFail(String err, int code) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onResultFail(0,"");

                    }
                });
            }
        });
    }

}
