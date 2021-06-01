package com.txt.video.ui.video

import android.content.Context
import android.net.Uri
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject
import com.tencent.teduboard.TEduBoardController
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef
import com.txt.video.R
import com.txt.video.TXManager
import com.txt.video.TXSdk
import com.txt.video.base.BasePresenter
import com.txt.video.net.http.HttpRequestClient
import com.txt.video.net.http.SystemHttpRequest
import com.txt.video.net.utils.TxLogUtils
import com.txt.video.ui.trtc.ConfigHelper
import com.txt.video.ui.trtc.TICClassroomOption
import com.txt.video.ui.trtc.TICManager
import com.txt.video.ui.trtc.TRTCCloudManager
import com.txt.video.ui.trtc.remoteuser.TRTCRemoteUserManager
import com.txt.video.ui.trtc.ticimpl.TICCallback
import com.txt.video.ui.trtc.ticimpl.utils.MyBoardCallback
import com.txt.video.ui.trtc.videolayout.list.MeetingVideoView
import com.txt.video.ui.trtc.videolayout.list.MemberEntity
import org.json.JSONObject

/**
 * Created by JustinWjq
 * @date 2020/10/20.
 * description：
 */
public class VideoPresenter(val context: Context, val activity: VideoActivity) :
    BasePresenter<Contract.ICollectView>(), Contract.ICollectPresenter {
    private var service_id: String? = null
    private var group_Id: String? = null
    private var user_id: String? = null
    private var agentName: String? = null
    private var mMainUserId = "" //当前业务员Id
    private var room_id = 0 //当前业务员Id
    private var mMaxRoomUser = 0 //最大人数
    private var mMaxRoomTime = 0 //最长时间
    private var mInviteNumber = "" //邀请码

    private var mMemberEntityList: ArrayList<MemberEntity>? = null

    private var mStringMemberEntityMap: HashMap<String, MemberEntity>? = null

    private var mTRTCParams: TRTCCloudDef.TRTCParams? = null // 进房参数

    private val mIsCustomCaptureAndRender = false


    private val mIsAudioEarpieceMode = false
    private var mTRTCCloud: TRTCCloud? = null
    private var mTRTCCloudManager: TRTCCloudManager? = null

    private val mReceivedVideo = true
    private val mReceivedAudio = true
    private val mVolumeType = 0
    private var mTRTCRemoteUserManager: TRTCRemoteUserManager? = null


    override fun getMemberEntityList(): ArrayList<MemberEntity> = mMemberEntityList!!

    override fun getStringMemberEntityMap(): HashMap<String, MemberEntity> =
        mStringMemberEntityMap!!

    override fun getRoomId(): Int = room_id!!

    override fun getServiceId(): String = service_id!!

    override fun getAgentId(): String = user_id!!

    override fun getGroupId(): String = group_Id!!

    override fun getMaxRoomUser(): Int = mMaxRoomUser

    override fun getMaxRoomTime(): Int = mMaxRoomTime
    override fun getInviteNumber(): String = mInviteNumber


    override fun initMembersData() {
        mStringMemberEntityMap = HashMap()
        mMemberEntityList = ArrayList()


        //创建自己的 MemberEntity
        val entity = MemberEntity()
        val meetingVideoView = MeetingVideoView(context)
//        meetingVideoView.setSelfView(true)
        meetingVideoView.setMeetingUserId(mTRTCParams?.userId)
//        meetingVideoView.setListener(mMeetingViewClick)
        meetingVideoView.setNeedAttach(true)
        entity.meetingVideoView = meetingVideoView
        entity.isShowAudioEvaluation = true
        entity.isAudioAvailable = true
        entity.isVideoAvailable = true
        entity.isMuteAudio = false
        entity.isMuteVideo = false
        entity.userId = mTRTCParams?.userId
        addMemberEntity(entity)

    }

    override fun addMemberEntity(entity: MemberEntity) {
        mMemberEntityList?.add(entity)
        mStringMemberEntityMap?.put(entity.userId, entity)
    }


    override fun removeMemberEntity(userId: String): Int {
        val entity = mStringMemberEntityMap?.remove(userId)
        if (entity != null) {
            val i = mMemberEntityList!!.indexOf(entity)
            mMemberEntityList?.remove(entity)
            return i
        }
        return -1
    }


    override fun getTRTCParams(): TRTCCloudDef.TRTCParams {
        return mTRTCParams!!
    }

    override fun setTRTCParams(stringExtra: String) {
        val jsonObject = JSONObject(stringExtra)
        val sdkAppIdStr = jsonObject.getInt("sdkAppId")
        service_id = jsonObject.getString("serviceId")
        room_id = jsonObject.getInt("roomId")
        group_Id = jsonObject.getString("groupId")
        var agentSigStr = ""
        if (TXManager.getInstance().isQuickEnter) {
            user_id = jsonObject.getString("agentId")
            agentSigStr = jsonObject.getString("agentSig")
            jsonObject.apply {
                mMaxRoomTime = getInt("maxRoomTime")
                mMaxRoomUser = getInt("maxRoomUser")
                mInviteNumber = getString("inviteNumber")
                TXSdk.getInstance().agent = getString("agentName")
            }

        } else {
            user_id = jsonObject.getString("userId")
            agentSigStr = jsonObject.getString("userSig")
            var userRole = jsonObject.getString("userRole")
        }


        val trtcParams = TRTCCloudDef.TRTCParams().apply {
            sdkAppId = sdkAppIdStr
            userId = user_id
            roomId = room_id
            userSig = agentSigStr
            role = TRTCCloudDef.TRTCRoleAnchor
        }
        this.mTRTCParams = trtcParams
    }


    override fun startRecord() {
        SystemHttpRequest.getInstance()
            .startRecord(getServiceId(), object : HttpRequestClient.RequestHttpCallBack {
                override fun onSuccess(json: String?) {

                }

                override fun onFail(err: String?, code: Int) {

                }

            })
    }

    override fun endRecord() {
        SystemHttpRequest.getInstance()
            .endRecord(getServiceId(), object : HttpRequestClient.RequestHttpCallBack {
                override fun onSuccess(json: String?) {

                }

                override fun onFail(err: String?, code: Int) {
                }

            })
    }


    override fun initVideoSDK() {

        val videoConfig = ConfigHelper.getInstance().videoConfig
        val audioConfig = ConfigHelper.getInstance().audioConfig

        audioConfig.isEnableAudio = true
        audioConfig.isAudioEarpieceMode = mIsAudioEarpieceMode
        videoConfig.isCurIsMix = false
        videoConfig.apply {
            isEnableVideo = true
            isPublishVideo = true
            isVideoFillMode = true
            isEnableGSensorMode = true
        }

        mTRTCCloud = TRTCCloud.sharedInstance(context)
        mTRTCCloudManager =
            TRTCCloudManager(context, mTRTCCloud, getTRTCParams(), 0).apply {
                setViewListener(activity)
                setTRTCListener(activity)
                initTRTCManager(false, mReceivedVideo, mReceivedAudio)
                setSystemVolumeType(mVolumeType)
            }


        if (mIsAudioEarpieceMode) {
            mTRTCCloudManager!!.enableAudioHandFree(false)
        }
        mTRTCRemoteUserManager =
            TRTCRemoteUserManager(mTRTCCloud, activity, mIsCustomCaptureAndRender)
        mTRTCRemoteUserManager?.setMixUserId(getTRTCParams().userId)

        initTICManager()
        initMembersData()
    }

    override fun enterRoom() {

        mTRTCCloudManager?.setSystemVolumeType(mVolumeType)

        mTRTCCloudManager!!.enterRoom()


    }

    override fun loginImRoom() {
        TxLogUtils.i("txsdk---loginImRoom---${getTRTCParams()?.userId}----${getTRTCParams()?.userSig}")
        mTicManager?.login(getTRTCParams().userId,
            getTRTCParams().userSig, object : TICCallback<Any> {
                override fun onSuccess(data: Any?) {
                    TxLogUtils.i("txsdk---im_login: onSuccess---$data")
                    view.joinClassroom()
                }

                override fun onError(module: String?, errCode: Int, errMsg: String?) {
                    TxLogUtils.i("txsdk---im_login: onError---$errCode----$errMsg")
                }

            })
    }

    override fun logoutClassRoom() {
        mTicManager?.logout(object : TICCallback<Any> {
            override fun onSuccess(data: Any?) {

            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {

            }
        })

        getTicManager().unInit()
    }


    private var mTicManager: TICManager? = null
    override fun initTICManager() {
        mTicManager = TICManager.getInstance()
        mTicManager?.init(
            activity, getTRTCParams().sdkAppId,
            TICManager.TICDisableModule.TIC_DISABLE_MODULE_TRTC
        )
        view.initBoard()

    }

    override fun joinClassroom(mBoardCallback: MyBoardCallback) {

        //2、如果用户希望白板显示出来时，不使用系统默认的参数，就需要设置特性缺省参数，如是使用默认参数，则填null。
        val initParam = TEduBoardController.TEduBoardInitParam()
        initParam.brushColor = TEduBoardController.TEduBoardColor(255, 0, 0, 255)
        initParam.smoothLevel = 0f //用于指定笔迹平滑级别，默认值0.1，取值[0, 1]
        initParam.timSync = true
        val classroomOption = TICClassroomOption()
        classroomOption.classId = getTRTCParams().roomId
        classroomOption.boardCallback = mBoardCallback
        classroomOption.boardInitPara = initParam

        mTicManager?.joinClassroom(classroomOption, object : TICCallback<String> {
            override fun onSuccess(data: String?) {
                TxLogUtils.i("txsdk---joinClassroom:onSuccess---$data")
//                joinClass()
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                TxLogUtils.i("txsdk---joinClassroom:onError---$errCode----$errMsg")
            }

        })
    }


    override fun startLocalPreview(viewVideo: MeetingVideoView) {
        view.processSelfVideoPlay()
        // 开始采集声音
        mTRTCCloudManager!!.startLocalAudio()
        // 开启本地预览
        mTRTCCloudManager!!.setLocalPreviewView(viewVideo.localPreviewView)
        mTRTCCloudManager!!.startLocalPreview()
    }


    override fun stopLocalPreview() {
        // 关闭本地预览
        mTRTCCloudManager!!.stopLocalPreview()
    }

    /**
     * 退房
     */
    override fun exitRoom() {
        stopLocalPreview()
        // 退房设置为非录制状态
        ConfigHelper.getInstance().audioConfig.isRecording = false
        mTRTCCloudManager!!.exitRoom()
        mTRTCCloudManager!!.destroy()
        mTRTCRemoteUserManager!!.destroy()
    }


    //开始屏幕分享
    override fun startScreenCapture() {


        val encParams = TRTCCloudDef.TRTCVideoEncParam()
        encParams.apply {
            videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_1280_720
            videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT
            videoFps = 10
            enableAdjustRes = false
            videoBitrate = 1200
        }

        val params = TRTCCloudDef.TRTCScreenShareParams()
        mTRTCCloudManager?.stopLocalPreview()
        mTRTCCloudManager?.startScreenCapture(encParams, params)


    }

    //停止屏幕分享
    override fun stopScreenCapture() {

        mTRTCCloudManager?.stopScreenCapture()
        mTRTCCloudManager?.startLocalPreview()
    }

    override fun getTRTCCloudManager(): TRTCCloudManager = mTRTCCloudManager!!
    override fun getTRTCRemoteUserManager(): TRTCRemoteUserManager = mTRTCRemoteUserManager!!
    override fun getTicManager(): TICManager = mTicManager!!

    override fun sendGroupMessage(msg: String) {
        mTicManager!!.sendGroupTextMessage(msg, object : TICCallback<Any> {
            override fun onSuccess(data: Any) {
                TxLogUtils.i("txsdk---sendGroupTextMessage:onSuccess------$data")
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                TxLogUtils.i("txsdk---sendGroupTextMessage:onError------$errCode---$errMsg")
            }

        })
    }

    override fun update() {
        SystemHttpRequest.getInstance()
            .getAgent(
                TXSdk.getInstance().agent,
                object : HttpRequestClient.RequestHttpCallBack {
                    override fun onSuccess(json: String?) {
                        //获取到文件列表
                        view.updateAdapter(json!!)

                    }

                    override fun onFail(err: String?, code: Int) {
                        TxLogUtils.i("txsdk---getAgent:onFail---$err---$code")
                    }

                })

    }


    override fun uploadFile(data1: Uri?) {
        TxLogUtils.i("txsdk---uploadFile---$data1")
        SystemHttpRequest.getInstance().uploadLogFile(data1, TXSdk.getInstance().agent,
            { size, time ->


            }, object :
                SystemHttpRequest.onRequestCallBack {
                override fun onSuccess() {
                    view.uploadFileSuccess()

                }

                override fun onFail(msg: String?) {
                    view.uploadFileFail(msg!!)
                }

            }
            ,
            { totalLength, currentLength ->


            }
        )
    }


    override fun requestWX() {
        val shareJO = JSONObject()
        shareJO.apply {
            put("version", TXSdk.getInstance().sdkVersion)
            put("terminal", TXSdk.getInstance().terminal)
            put("title", TXSdk.getInstance().txConfig.miniprogramTitle)
        }
        val webpage = WXMiniProgramObject().apply {
            webpageUrl = "http://www.qq.com"
            miniprogramType = when (TXSdk.getInstance().txConfig.miniprogramType) {
                TXSdk.Environment.DEV -> {
                    WXMiniProgramObject.MINIPROGRAM_TYPE_TEST
                }
                TXSdk.Environment.TEST -> {
                    WXMiniProgramObject.MINIPROGRAM_TYPE_PREVIEW
                }
                TXSdk.Environment.RELEASE -> {
                    WXMiniProgramObject.MINIPTOGRAM_TYPE_RELEASE
                }
            }

            userName = TXSdk.getInstance().txConfig.userName
            path =
                "/pages/index/index?serviceId=${getServiceId()}&agentId=${getAgentId()}&inviteNumber=${getInviteNumber()}&shareConfig=${shareJO}"
        }

        val msg = WXMediaMessage(webpage).apply {
            title = TXSdk.getInstance().txConfig.miniprogramCard
            description = ""
            thumbData = context.resources.openRawResource(R.raw.tx_icon_miniprogram).readBytes()
        }
        TXSdk.getInstance().wxTransaction = "miniProgram${System.currentTimeMillis()}"
        val req = SendMessageToWX.Req().apply {
            transaction = TXSdk.getInstance().wxTransaction
            message = msg
            scene = SendMessageToWX.Req.WXSceneSession
        }

        view.sendReq(req)
    }


    override fun startShare() {

        SystemHttpRequest.getInstance()
            .startShare(
                getServiceId(),
                object : HttpRequestClient.RequestHttpCallBack {
                    override fun onSuccess(json: String?) {

                        view.startShareSuccess()
                    }

                    override fun onFail(err: String?, code: Int) {
                        view.startShareFail()
                    }

                })
    }

    override fun setScreenStatus(screenStatus: Boolean) {
        SystemHttpRequest.getInstance()
            .screenStatus(
                getServiceId(),
                screenStatus,
                object : HttpRequestClient.RequestHttpCallBack {
                    override fun onSuccess(json: String?) {

                    }

                    override fun onFail(err: String?, code: Int) {
                    }

                })
    }

    override fun deleteFile(id: String?) {
        SystemHttpRequest.getInstance()
            .deleteFile(
                id,
                TXSdk.getInstance().agent,
                object : HttpRequestClient.RequestHttpCallBack {
                    override fun onSuccess(json: String?) {
                        update()
                    }

                    override fun onFail(err: String?, code: Int) {

                    }

                })
    }

    private var mSelfisOwner: Boolean = false
    private var mOwnerUserId: String = ""
    private var mRoomScreenStatus: Boolean = false
    //判断自己是不是主持人
    override fun isOwner(): Boolean = mSelfisOwner!!
    //拿到房间的主持人userid
    override fun getOwnerUserId(): String = mOwnerUserId
    //获取房间的屏幕状态
    override fun getRoomScreenStatus(): Boolean = mRoomScreenStatus

    override fun unitConfig() {
        view?.apply {
            hideFloatingWindow()
            hideVideoFloatingWindow()
            resetBoardLayout()
            stopTimer()
            finishPage()
        }


    }

    override fun getRoomInfo(
        userId: String,
        streamType: Int,
        available: Boolean,
        entity: MemberEntity?
    ) {
        SystemHttpRequest.getInstance()
            .getRoomInfo(
                getServiceId(),
                object : HttpRequestClient.RequestHttpCallBack {
                    override fun onSuccess(json: String?) {
                        val jsonObject = JSONObject(json)
                        val jsonArray = jsonObject.getJSONArray("userInfo")//获取房间信息
                        mRoomScreenStatus = jsonObject.getBoolean("screenStatus")//获取房间信息
                        var personUserName = ""
                        for (i in 0..jsonArray.length()) {
                            //获取房间人员的信息
                            val personJb = jsonArray.getJSONObject(i)
                            val personUserId = personJb.getString("userId")
                            val userRoleStr = personJb.getString("userRole")
                            //找到对应的人员
                            if (userId.equals(personUserId)) {
                                personUserName = personUserId
                                if (entity != null) {
//                                    entity.isNeedFresh = true
//                                    entity.isVideoAvailable = available
//                                    entity.meetingVideoView.isNeedAttach = available
                                    val isOwner = userRoleStr == "owner"

                                    if (isOwner){
                                        mOwnerUserId = personUserId
                                    }
                                    //判断自己是否是主持人
                                    if (getTRTCParams().userId == userId) {

                                        mSelfisOwner = isOwner
                                    }
                                    entity.isHost = isOwner
                                    entity.userName = if (entity.isHost) {
                                        "业务员"
                                    } else {
                                        personUserName
                                    }


                                    view.getRoomInfoSuccess(
                                        json,
                                        userId,
                                        streamType,
                                        entity,
                                        available
                                    )

                                }
                                return
                            }
                        }


                    }

                    override fun onFail(err: String?, code: Int) {

                    }

                })
    }

    private var mVisibleVideoStreams: ArrayList<MemberEntity>? = null

    /**
     * 处理页面中需要展示的item
     * 如果滑动到新的页面，旧的页面所有item需要停止播放
     * 新的页面根据是否打开了video来判断需要播放页面
     *
     * @param fromItem
     * @param toItem
     */
    override fun processVideoPlay(fromItem: Int, toItem: Int) {

        val oldUserIds: MutableList<String> =
            java.util.ArrayList()
        val newUserIds: MutableList<String> =
            java.util.ArrayList()
        val needStopIds: MutableList<String> =
            java.util.ArrayList()
        val newVisibleStream = ArrayList<MemberEntity>()
        if (mVisibleVideoStreams == null) {
            mVisibleVideoStreams = ArrayList<MemberEntity>()
        }
        for (i in fromItem..toItem) {
            newUserIds.add(getMemberEntityList()!![i].userId)
            newVisibleStream.add(getMemberEntityList()!![i])
        }
        for (entity in mVisibleVideoStreams!!) {
            oldUserIds.add(entity.userId)
            if (!newUserIds.contains(entity.userId)) {
                needStopIds.add(entity.userId)
            }
        }
        for (entity in newVisibleStream) {
            if (entity.isShowOutSide) {
                continue
            }
            val meetingVideoView = entity.meetingVideoView
            if (meetingVideoView==null){
                continue
            }

            meetingVideoView?.refreshParent()
            if (entity.isNeedFresh) {
                entity.isNeedFresh = false
                if (!entity.isMuteVideo && entity.isVideoAvailable) {
                    TxLogUtils.i("processVideoPlay--$fromItem----$toItem")
                    meetingVideoView.isPlaying = true
                    getTRTCRemoteUserManager().remoteUserVideoAvailable(
                        entity.userId,
                        TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,
                        entity.meetingVideoView.playVideoView
                    )

                } else {
                    if (meetingVideoView.isPlaying) {
                        meetingVideoView.isPlaying = false
                        getTRTCRemoteUserManager().remoteUserVideoUnavailable(
                            entity.userId,
                            TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG
                        )
                    }
                }
                continue
            }
            if (!oldUserIds.contains(entity.userId)) {
                if (!entity.isMuteVideo && entity.isVideoAvailable) {
                    if (null!=meetingVideoView&&!meetingVideoView.isPlaying) {
                        TxLogUtils.i("oldUserIds-processVideoPlay--$fromItem----$toItem")
                        meetingVideoView.isPlaying = true
                        getTRTCRemoteUserManager().remoteUserVideoAvailable(
                            entity.userId,
                            TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,
                            entity.meetingVideoView.playVideoView
                        )
                        meetingVideoView.refreshParent()
                    }
                } else {
                    if (null!=meetingVideoView&&meetingVideoView.isPlaying) {
                        meetingVideoView.isPlaying = false
                        getTRTCRemoteUserManager().remoteUserVideoUnavailable(
                            entity.userId,
                            TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG
                        )
                    }
                }
            } else {
                if (entity.isMuteVideo || !entity.isVideoAvailable) {
                    if (null!=meetingVideoView&&meetingVideoView.isPlaying) {
                        meetingVideoView.isPlaying = false
                        getTRTCRemoteUserManager()?.remoteUserVideoUnavailable(
                            entity.userId,
                            TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG
                        )
                    }
                }
            }
        }
        for (id in needStopIds) {
            val entity = getStringMemberEntityMap()!![id]
            entity?.meetingVideoView?.setPlayingWithoutSetVisible(false)
            getTRTCRemoteUserManager().remoteUserVideoUnavailable(
                entity?.userId,
                TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG
            )
        }
        mVisibleVideoStreams = newVisibleStream!!
    }

    override fun setIMTextData(type: String): JSONObject = JSONObject().apply {
        put("serviceId", getServiceId())
        put("type", type)
        put("agentId", getAgentId())
    }

    override fun extendTime() {
        SystemHttpRequest.getInstance()
            .extendTime(
                getServiceId(),
                object : HttpRequestClient.RequestHttpCallBack {
                    override fun onSuccess(json: String?) {
                        view.showSuccess()
                    }

                    override fun onFail(err: String?, code: Int) {
                        view.showFail()
                    }

                })
    }


}