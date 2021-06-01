package com.txt.video.ui.video

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.annotation.DrawableRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson
import com.tencent.imsdk.TIMMessage
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory.createWXAPI
import com.tencent.teduboard.TEduBoardController
import com.tencent.teduboard.TEduBoardController.TEduBoardColor
import com.tencent.trtc.TRTCCloudDef
import com.tencent.trtc.TRTCStatistics
import com.txt.video.R
import com.txt.video.TXSdk
import com.txt.video.base.BaseMVPActivity
import com.txt.video.net.bean.FileBean
import com.txt.video.net.utils.TxLogUtils
import com.txt.video.ui.trtc.ConfigHelper
import com.txt.video.ui.trtc.TRTCCloudIView
import com.txt.video.ui.trtc.TRTCCloudManagerListener
import com.txt.video.ui.trtc.remoteuser.TRTCRemoteUserIView
import com.txt.video.ui.trtc.ticimpl.TICCallback
import com.txt.video.ui.trtc.ticimpl.TICMessageListener
import com.txt.video.ui.trtc.ticimpl.utils.MyBoardCallback
import com.txt.video.ui.trtc.videolayout.list.*
import com.txt.video.ui.trtc.videolayout.list.MemberListAdapter.*
import com.txt.video.widget.adapter.PicQuickAdapter
import com.txt.video.widget.callback.onExitDialogListenerCallBack
import com.txt.video.widget.callback.onShareDialogListenerCallBack
import com.txt.video.widget.dialog.CommonDialog
import com.txt.video.widget.dialog.ExitDialog
import com.txt.video.widget.dialog.ShareDialog
import com.txt.video.widget.floatview.FloatingView
import com.txt.video.widget.utils.*
import com.txt.video.widget.view.ScreenView
import kotlinx.android.synthetic.main.tx_activity_video.*
import org.json.JSONObject


class VideoActivity : BaseMVPActivity<Contract.ICollectView, VideoPresenter>(), TRTCCloudIView,
    TRTCCloudManagerListener,
    TRTCRemoteUserIView, Contract.ICollectView, TICMessageListener,
    ScreenView.BigScreenViewCallback {

    private var mMemberListAdapter: MemberListAdapter? = null
    private var isShare = false //显示分享屏幕
    private var isBroad = false //显示白板
    private var isCloseVideo = false //保存当前视频开关的状态

    /**
     * 白板视图控件
     */
    private var mBoardContainer: FrameLayout? = null

    @JvmField
    var mBoard: TEduBoardController? = null
    var mBoardCallback: MyBoardCallback? = null

    private var bigMeetingVideoView: MeetingVideoView? = null
    private var bigMeetingEntity: MemberEntity? = null

    private var mListRv: RecyclerView? = null
    var mViewVideo: MeetingVideoView? = null

    override fun finishPage() {


        val isClearBoard = if (mPresenter.isOwner()) {
            mPresenter.endRecord()
            if (isShare) {
                mPresenter?.stopScreenCapture()
            }
            true
        } else {
            false
        }
        mPresenter?.apply {
            getTicManager().removeIMMessageListener(this@VideoActivity)
            getTicManager().quitClassroom(isClearBoard, null)
            logoutClassRoom()
            exitRoom()
        }


        skipCaller()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        } else {
            finish()
        }
    }

    override fun muteLocalAudio() {
        //开启静音
        val audioConfig = ConfigHelper.getInstance().audioConfig
        audioConfig.isEnableAudio = !audioConfig.isEnableAudio

        mPresenter.getTRTCCloudManager().muteLocalAudio(!audioConfig.isEnableAudio)
        val entity =
            mPresenter.getStringMemberEntityMap()[mPresenter.getTRTCParams().userId]
        entity?.isShowAudioEvaluation = audioConfig.isEnableAudio
        mMemberListAdapter!!.notifyItemChanged(
            mPresenter.getMemberEntityList().indexOf(
                entity
            )
            , VOLUME_SHOW
        )
        tx_business_audio_mute.isSelected = !audioConfig.isEnableAudio
    }

    override fun muteLocalVideo() {
        //关闭视频
        if (isShare) {
            ToastUtils.showShort("屏幕分享时不能操作摄像头")
            return
        }
        val videoConfig = ConfigHelper.getInstance().videoConfig
        videoConfig.isEnableVideo = !videoConfig.isEnableVideo
        val enableVideo = videoConfig.isEnableVideo
        mPresenter.getTRTCCloudManager().muteLocalVideo(!enableVideo)

        val entity =
            mPresenter.getStringMemberEntityMap()[mPresenter.getTRTCParams().userId]
        entity?.isMuteVideo = !enableVideo
        mMemberListAdapter!!.notifyItemChanged(
            mPresenter.getMemberEntityList().indexOf(
                entity
            )
            , VIDEO_CLOSE
        )
        isCloseVideo = !enableVideo
        tx_business_video.isSelected = !enableVideo
    }

    override fun switchCamera() {
        //反转镜头
        mPresenter.getTRTCCloudManager().switchCamera()
        tx_business_switch.isSelected = !mPresenter.getTRTCCloudManager().isFontCamera
    }

    override fun destroyRoom() {
        if (mPresenter.isOwner()) {
            mPresenter.getTicManager().sendGroupTextMessage(
                mPresenter.setIMTextData("end").toString(),
                object : TICCallback<Any> {
                    override fun onSuccess(data: Any) {
                        TxLogUtils.i("txsdk---sendGroupTextMessage:onSuccess---${data}")
                        mPresenter.unitConfig()
                    }

                    override fun onError(module: String?, errCode: Int, errMsg: String?) {
                        TxLogUtils.i("txsdk---sendGroupTextMessage:onError------$errCode---$errMsg")
                        mPresenter.unitConfig()
                    }

                })
        } else {
            mPresenter.unitConfig()
        }
    }


    override fun initViews() {

        mViewVideo = mPresenter.getMemberEntityList()[0].meetingVideoView
        val pageLayoutManager = MeetingPageLM(this)
        pageLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        pageLayoutManager.setPageListener(object : MeetingPageLayoutManager.PageListener {
            override fun onItemVisible(fromItem: Int, toItem: Int) {
                if (fromItem == 0) {
                    processSelfVideoPlay()
                    mPresenter.processVideoPlay(1, toItem)
                } else {
                    mPresenter.processVideoPlay(fromItem, toItem)
                }
            }

            override fun onPageSelect(pageIndex: Int) {
            }

            override fun onPageSizeChanged(pageSize: Int) {

            }

        })


        mListRv = trtc_video_view_layout

        mMemberListAdapter =
            MemberListAdapter(
                this,
                mPresenter.getMemberEntityList(),
                object : MemberListAdapter.ListCallback {
                    override fun onItemClick(position: Int) {

                    }

                    override fun onItemDoubleClick(position: Int) {

                    }

                })

        mListRv?.setHasFixedSize(true)

        mListRv?.layoutManager = pageLayoutManager
        mListRv?.adapter = mMemberListAdapter

        mBoardContainer = findViewById(R.id.board_view_container) as FrameLayout
        initPicAdapter()
        regToWx()
        showInviteBt(true)
        showTitle()
    }

    override fun processSelfVideoPlay() {
        mViewVideo?.refreshParent()
    }


    override fun onSnapshotLocalView(bmp: Bitmap?) {
    }

    override fun onMuteLocalAudio(isMute: Boolean) {
    }

    override fun onStartLinkMic() {
    }

    override fun onMuteLocalVideo(isMute: Boolean) {
        if (isMute) {
//            mTRTCVideoLayout?.updateAudioVolume(mMainUserId, -1)
        } else {
//            mTRTCVideoLayout?.updateAudioVolume(mMainUserId, 0)
        }
    }

    override fun onAudioVolumeEvaluationChange(enable: Boolean) {

    }

    override fun onFirstVideoFrame(userId: String?, streamType: Int, width: Int, height: Int) {
    }


    override fun onUserVideoAvailable(userId: String?, available: Boolean) {
        TxLogUtils.i("onUserVideoAvailable---$userId+available$available")
        onVideoChange(userId!!, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, available)

    }

    override fun onExitRoom(reason: Int) {
        TxLogUtils.i("onExitRoom---reason$reason")
    }

    override fun onScreenCaptureStarted() {
        TxLogUtils.i("onScreenCaptureStarted")
        mPresenter.sendGroupMessage(mPresenter.setIMTextData("startShare").toString())
        mPresenter.setScreenStatus(true)
        isShare = true
        tx_business_screen.isSelected = true
        val entity =
            mPresenter.getStringMemberEntityMap()[mPresenter.getTRTCParams().userId]
        entity?.isScreen = true
        mMemberListAdapter!!.notifyItemChanged(
            mPresenter.getMemberEntityList().indexOf(
                entity
            )
            , VIDEO_SCREEN_CLOSE
        )

        showFloatingWindow()
    }

    override fun onScreenCaptureStopped(reason: Int) {
        TxLogUtils.i("onScreenCaptureStopped---reason$reason")
        mPresenter.sendGroupMessage(mPresenter.setIMTextData("endShare").toString())
        mPresenter.setScreenStatus(false)
        isShare = false
        tx_business_screen.isSelected = false
        hideFloatingWindow()

        val entity =
            mPresenter.getStringMemberEntityMap()[mPresenter.getTRTCParams().userId]
        val index = mPresenter.getMemberEntityList().indexOf(
            entity
        )
        if (isCloseVideo) {
            tx_business_video.isSelected = isCloseVideo
            mPresenter.getTRTCCloudManager().muteLocalVideo(isCloseVideo)
            entity?.isMuteVideo = isCloseVideo
            mMemberListAdapter!!.notifyItemChanged(
                index
                , VIDEO_CLOSE
            )
        } else {
            entity?.isScreen = isCloseVideo
            mMemberListAdapter!!.notifyItemChanged(
                index
                , VIDEO_SCREEN_CLOSE
            )
        }
    }

    override fun onRemoteUserEnterRoom(remoteUserId: String?) {
        TxLogUtils.i("onRemoteUserEnterRoom-----remoteUserId$remoteUserId")

        val contains = remoteUserId?.contains("tic_record")
        if (!contains!!) {
            if (mPresenter.getTRTCRemoteUserManager().remoteAllUser == 1) {

            } else {

            }

            val mMeetingVideoView = MeetingVideoView(this@VideoActivity).apply {
                meetingUserId = remoteUserId
                isNeedAttach = true
                isPlaying = true
            }

            //添加到list中
            val insertIndex = mPresenter.getMemberEntityList().size
            val entity = MemberEntity().apply {
                userId = remoteUserId
                userName = ""
                meetingVideoView = mMeetingVideoView
                isMuteAudio = false
                isMuteVideo = false
                isVideoAvailable = false
                isAudioAvailable = false
                isNeedFresh = true
                isShowAudioEvaluation = false
                isShowOutSide = false
            }

            mPresenter.addMemberEntity(entity)
            mMemberListAdapter!!.notifyItemInserted(insertIndex)
            mPresenter.getRoomInfo(
                remoteUserId,
                TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,
                false,
                entity
            )

        }

    }

    override fun onNetworkQuality(
        localQuality: TRTCCloudDef.TRTCQuality?,
        remoteQuality: ArrayList<TRTCCloudDef.TRTCQuality>?
    ) {
//        TxLogUtils.i("onNetworkQuality--")
    }

    override fun onUserSubStreamAvailable(userId: String?, available: Boolean) {
        TxLogUtils.i("onUserSubStreamAvailable--")
    }


    override fun onRemoteUserLeaveRoom(userId: String?, reason: Int) {
        TxLogUtils.i("onRemoteUserLeaveRoom--userId$userId----reason$reason")
        // 回收分配的渲染的View
        mPresenter.getTRTCRemoteUserManager().removeRemoteUser(userId)
        val index = mPresenter.removeMemberEntity(userId!!)
        if (index >= 0) {
            mMemberListAdapter!!.notifyItemRemoved(index)
        }
        if (mPresenter.getTRTCRemoteUserManager().remoteAllUser == 0) {
            //如果当前没有客户 隐藏
        }

    }

    override fun showTitle() {
        tx_time.text = "会议房间"
    }

    override fun onAudioEffectFinished(effectId: Int, code: Int) {
    }

    override fun onRecvCustomCmdMsg(userId: String?, cmdID: Int, seq: Int, message: ByteArray?) {
    }


    override fun onEnterRoom(elapsed: Long) {
        TxLogUtils.i("onEnterRoom-----耗时$elapsed 毫秒")
        if (elapsed >= 0) {
            mPresenter.startRecord()
            // 发起云端混流
            mPresenter.getTRTCRemoteUserManager().updateCloudMixtureParams()

            val entity =
                mPresenter.getStringMemberEntityMap()[mPresenter.getTRTCParams().userId]
            if (entity != null) {
                mPresenter.getRoomInfo(
                    mPresenter?.getTRTCParams()?.userId!!,
                    TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,
                    available = true,
                    entity = entity
                )
            }

        } else {
            ToastUtils.showShort(R.string.tx_joinroom_error)
            mPresenter.exitRoom()
        }
    }


    private fun onVideoChange(
        userId: String,
        streamType: Int,
        available: Boolean
    ) {

        if (available) {
            if (!mPresenter.getTRTCParams().userId?.equals(userId)!!) { //开启定时器
                tx_icon_invite.visibility = View.GONE
                showInviteBt(true)
            }

        }


        val ownerUserId = mPresenter.getOwnerUserId()

        val entity = mPresenter.getStringMemberEntityMap()[userId]
        entity?.apply {
            isNeedFresh = true
            isVideoAvailable = available
            meetingVideoView.isNeedAttach = available
        }

        if (bigMeetingEntity != null && userId == bigMeetingEntity?.userId) {
            TxLogUtils.i("bigMeetingEntity-------bigMeetingEntity")
            mPresenter.getTRTCRemoteUserManager().setRemoteFillMode(
                bigMeetingEntity?.userId,
                TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,
                false
            )
            mPresenter.getTRTCRemoteUserManager().remoteUserVideoAvailable(
                bigMeetingEntity?.userId,
                TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,
                bigMeetingEntity?.meetingVideoView?.playVideoView
            )
        }
        if (ownerUserId == userId && isStartShare) {
            //显示大屏幕，不需要更新ui
            TxLogUtils.i("bigMeetingEntity-------ownerUserId")
            return
        }
        mMemberListAdapter!!.notifyItemChanged(
            mPresenter.getMemberEntityList().indexOf(
                entity
            )
        )


    }

    override fun onDisConnectOtherRoom(err: Int, errMsg: String?) {
    }

    override fun onUserAudioAvailable(userId: String?, available: Boolean) {
        TxLogUtils.i("onUserAudioAvailable----userId$userId----available$available")
        val entity = mPresenter.getStringMemberEntityMap()[userId]
        if (entity != null) {
            entity.isAudioAvailable = available
            entity.isShowAudioEvaluation = available
            mMemberListAdapter!!.notifyItemChanged(
                mPresenter.getMemberEntityList().indexOf(entity),
                MemberListAdapter.VOLUME_SHOW
            )
            //界面暂时没有变更
        }


    }

    override fun onStatistics(statics: TRTCStatistics?) {
        TxLogUtils.i("onStatistics----statics${statics?.appCpu}")
    }

    override fun onConnectOtherRoom(userID: String?, err: Int, errMsg: String?) {
        TxLogUtils.i("onConnectOtherRoom----userID${userID}---err${err}---errMsg${errMsg}")
    }


    override fun onUserVoiceVolume(
        userVolumes: ArrayList<TRTCCloudDef.TRTCVolumeInfo>?,
        totalVolume: Int
    ) {
        userVolumes?.forEach {
            val entity = mPresenter.getStringMemberEntityMap()!![it.userId]
            entity?.audioVolume = it.volume
            mMemberListAdapter!!.notifyItemChanged(
                mPresenter.getMemberEntityList()!!.indexOf(entity),
                MemberListAdapter.VOLUME
            )
        }
    }

    override fun onError(errCode: Int, errMsg: String?, extraInfo: Bundle?) {
        //-1308 分享屏幕失败
        TxLogUtils.i("onError----：$errCode----：$errMsg")
        if (errCode == -1308) {
            isShare = false
            ToastUtils.showShort(R.string.tx_showscreen_error)
            mPresenter.stopScreenCapture()

        }

    }

    override fun onRecvSEIMsg(userId: String?, data: ByteArray?) {
    }


    /**
     * 显示确认消息
     *
     * @param msg     消息内容
     * @param isError true错误消息（必须退出） false提示消息（可选择是否退出）
     */
    var dialog: ExitDialog? = null
    private fun showExitInfoDialog() {
        val tempBtStr = if (TXSdk.getInstance().txConfig.isShowTemporaryButton) {
            "暂时离开"
        } else {
            "取消"
        }
        val titleStr = if (TXSdk.getInstance().txConfig.isShowTemporaryButton) {
            "请选择暂时离开还是结束会话？"
        } else {
            "请确定是否结束会话？"
        }
        val confirmStr = if (TXSdk.getInstance().txConfig.isShowTemporaryButton) {
            "结束会话"
        } else {
            "确认"
        }
        if (dialog == null) {

            dialog = ExitDialog(
                this,
                tempBtStr,
                "确定离开",
                confirmStr,
                titleStr
            )
            dialog?.setOnConfirmlickListener(object :
                onExitDialogListenerCallBack {
                override fun onConfirm() {
                    TXSdk.getInstance().share = false
                    destroyRoom()
                }

                override fun onTemporarilyLeave() {
                    if (TXSdk.getInstance().txConfig.isShowTemporaryButton) {
                        TXSdk.getInstance().share = true
                        //暂时离开
                        showVideoFloatingWindow()
                    }

                }

            })
        } else {

            dialog?.setTv_cancel(tempBtStr)
            dialog?.setTv_content(titleStr)
            dialog?.setTv_confirm(confirmStr)
        }

        dialog?.show()

    }


    fun skipCaller() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                if (TXSdk.getInstance().isDemo) {
                    Uri.parse("txt://txtvideo:8888/videopage")
                } else {
                    Uri.parse("txt://txtvideo:9999/videopage")
                }

            )
        )
    }

    var dialog1: ExitDialog? = null
    fun showDeleteFileDialog(id: String?) {
        if (dialog1 == null) {
            dialog1 = ExitDialog(
                this,
                resources.getString(R.string.tx_dialog_exit_cancel),
                resources.getString(R.string.tx_dialog_exit_title),
                resources.getString(R.string.tx_dialog_exit_confirm),
                ""
            )
        }

        dialog1?.setOnConfirmlickListener(object :
            onExitDialogListenerCallBack {
            override fun onConfirm() {
                TxLogUtils.i("txsdk---deleteFile-----$id")
                mPresenter.deleteFile(id)
            }

            override fun onTemporarilyLeave() {

            }

        })
        dialog1?.show()

    }


    var mTimerdialog: CommonDialog? = null
    fun showTimerDialog(
        type: String,
        maxRoomTime: Int,
        extendRoomTime: Int,
        notifyExtendTime: Int,
        notifyEndTime: Int
    ) {

        if (mTimerdialog == null) {
            mTimerdialog = CommonDialog(
                this
            )
        }



        mTimerdialog?.setOnConfirmlickListener(object :
            onExitDialogListenerCallBack {
            override fun onConfirm() {
                //
                when (type) {
                    "2" -> {
                        mPresenter.extendTime()
                    }
                    "3" -> {
                        mPresenter.extendTime()
                    }
                    else -> {
                    }
                }
            }

            override fun onTemporarilyLeave() {
                when (type) {
                    "2" -> {
                    }
                    "3" -> {
                        destroyRoom()
                    }
                    else -> {
                    }
                }
            }

            override fun end() {
                destroyRoom()
            }

        })
        mTimerdialog?.show()
        when (type) {
            "1" -> {
                //显示单个
                mTimerdialog?.apply {
                    setTv_content("本次会议有效时间为${maxRoomTime / 60}分钟")
                    setOnlyConfirm("知道了")
                }

            }
            "2" -> {
                //通知延长
                mTimerdialog?.apply {
                    setTv_content("距离会议结束还有${notifyExtendTime / 60}分钟，\n 确认延长有效时间${extendRoomTime / 60}分钟？")
                    setTv_confirm("延长会话")
                    setTv_cancel("取消延长")
                }
            }
            "3" -> {
                //通知结束

                mTimerdialog?.apply {
                    setTv_content("会议有效时间已用尽，\n 确认延长有效时间${extendRoomTime / 60}分钟？")
                    setTv_confirm("延长会话")
                    startCommonTimer(notifyEndTime, "(若${notifyEndTime}s内无操作将自动结束会话)")
                }
            }
            else -> {
            }
        }
    }


    private fun showBroad() {
        startShare()

        mBoardContainer?.visibility = View.VISIBLE

        if (mPresenter.getTRTCRemoteUserManager().remoteAllUser == 0) {
            //如果没有客户
            tx_icon_invite.visibility = View.GONE
        } else {
//                        mTRTCVideoLayout!!.switchVideoViewTolast(0, true)
        }


    }

    private fun restoreBroadTool() {
        isShowPaint = false
        //隐藏
        mBoard?.isDrawEnable = true
        tx_paint.setImageResource(R.drawable.tx_paint_default)
        val eduBoardColor = TEduBoardColor("#f99404")
        mBoard!!.textColor = eduBoardColor
        mBoard!!.toolType = TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_PEN
        tx_board_view_business.visibility = View.GONE
    }

    var shareDialog: ShareDialog? = null
    fun showShareDialog() {
        if (shareDialog == null) {
            shareDialog =
                ShareDialog(this)
            shareDialog?.setOnConfirmlickListener(object :
                onShareDialogListenerCallBack {
                override fun onConfirmWx() {
                    mPresenter.requestWX()
                }

                override fun onConfirmMSG() {
                    sendMSG()
                }

                override fun dismiss() {
                }


            })
        }

        shareDialog?.show()
//        shareDialog?.setShareContent((mPresenter.getMaxRoomUser() - 1).toString())
    }

    var isShareScreen = false //发起文件共享就不需要调用
    private fun startShare() {
        if (!isShareScreen) {
            mPresenter.startShare()
        }
    }


    private val file_code = 10100
    private fun chooseFile() {
        PermissionUtils.permission(
            PermissionConstants.STORAGE
        ).callback(object : PermissionUtils.FullCallback {
            override fun onGranted(permissionsGranted: MutableList<String>?) {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    setType("*/*")
                }
                startActivityForResult(intent, file_code)
            }

            override fun onDenied(
                permissionsDeniedForever: MutableList<String>?,
                permissionsDenied: MutableList<String>?
            ) {

            }

        }).request()
    }


    private val photo_code = 10000
    private fun choosePhoto() {
        PermissionUtils.permission(
            PermissionConstants.STORAGE
        ).callback(object : PermissionUtils.FullCallback {
            override fun onGranted(permissionsGranted: MutableList<String>?) {
                val intent = Intent()
                intent.setAction(Intent.ACTION_PICK)
                intent.setType("image/*")
                startActivityForResult(intent, photo_code)
            }

            override fun onDenied(
                permissionsDeniedForever: MutableList<String>?,
                permissionsDenied: MutableList<String>?
            ) {

            }

        }).request()


    }


    override fun updateAdapter(json: String) {
        this@VideoActivity.runOnUiThread {
            TxLogUtils.i("txsdk---getAgent:onSuccess---$json")
            val allFileBean =
                Gson().fromJson<FileBean>(json, FileBean::class.java)
        }
    }

    override fun uploadFileSuccess() {
        TxLogUtils.i("txsdk---uploadFile:onSuccess")
        mPresenter.update()
    }

    override fun uploadFileFail(msg: String) {
        TxLogUtils.i("txsdk---uploadFile:onFail")
        runOnUiThread { ToastUtils.showShort("$msg") }
    }

    var api: IWXAPI? = null

    override fun sendReq(req: BaseReq) {

        api?.sendReq(req)
    }

    override fun startShareSuccess() {
        isShareScreen = true
    }

    override fun startShareFail() {

    }

    override fun getRoomInfoSuccess(
        json: String?,
        userId: String,
        streamType: Int,
        entity: MemberEntity?,
        available: Boolean
    ) {
        runOnUiThread {
            //如果房间是投屏状态，并且当前是参会人，
            // 这个时候获取到消息是主持人的话，就展示主持人大画面

            mMemberListAdapter!!.notifyItemChanged(
                mPresenter.getMemberEntityList().indexOf(
                    entity
                )
            )


            if (mPresenter.getRoomScreenStatus()) {
                TxLogUtils.i("onVideoChange-----checkItemToBig------${entity?.isHost!!}")
                if (mPresenter.isOwner()) {

                } else {
                    if (entity?.isHost!!) {

                        Handler().postDelayed({ checkItemToBig() }, 3000)
                    }

                }

            }

        }
    }

    override fun showSuccess() {
        runOnUiThread {
            ToastUtils.showShort("延长成功！")
        }
    }

    override fun showFail() {
        runOnUiThread {
            ToastUtils.showShort("延长失败！")
        }
    }

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            api?.registerApp(TXSdk.getInstance().txConfig.wxKey)
        }

    }

    private fun regToWx() {

        api = createWXAPI(this, TXSdk.getInstance().txConfig.wxKey, TXSdk.getInstance().isDebug)
        api?.registerApp(TXSdk.getInstance().txConfig.wxKey)
        registerReceiver(
            broadcastReceiver, IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        TxLogUtils.i("txsdk---requestCode--$requestCode -----resultCode--$resultCode")
        TxLogUtils.i("txsdk---photo_code---${data?.data}")

        if (resultCode == Activity.RESULT_OK && data?.data != null) {
            val data1 = data?.data
            when (requestCode) {
                photo_code, file_code -> {
                    //获取本地图片
                    TxLogUtils.i("txsdk---onActivityResult:data---code---$data1----")
                    mPresenter.uploadFile(data1)
                }

                100 -> {
                    //录取权限开启
                }
                else -> {
                }
            }
        }


    }


    fun onTxClick(v: View?) {
        val id = v?.id
        if (id == R.id.trtc_ib_back) {
            showExitInfoDialog()

        } else if (id == R.id.tv_invite || id == R.id.tx_icon_invite) {
            //邀请好友

            showShareDialog()
        } else if (id == R.id.tx_business_video) {
            muteLocalVideo()
        } else if (id == R.id.tx_business_audio_mute) {
            muteLocalAudio()


        } else if (id == R.id.tx_business_switch) {
           switchCamera()

        } else if (id == R.id.tx_business_share) {
            //文档共享
//            if (mPresenter.isOwner())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                TxLogUtils.i("AndroidSystemUtil.getDevice()", AndroidSystemUtil.getDevice())
                if (!PermissionUtils.isGrantedDrawOverlays()) {
                    TxLogUtils.i("需要打开悬浮窗权限")
                    PermissionUtils.requestDrawOverlays(object :
                        PermissionUtils.SimpleCallback {
                        override fun onGranted() {
                            showVideoFloatingWindow()
                        }

                        override fun onDenied() {
                            ToastUtils.showShort("未开启悬浮窗权限")
                        }
                    })
                } else {
                    TxLogUtils.i("startScreenCapture")
                    showVideoFloatingWindow()
                }

            } else {
                TxLogUtils.i("startScreenCapture")
                showVideoFloatingWindow()

            }
        } else if (id == R.id.tx_business_screen) {
            //投屏 如果关闭摄像头，投屏中自动打开

            if (!mPresenter.isOwner()) {
                ToastUtils.showLong("您目前暂无权限开启投屏")
                return
            }
            if (isShare) {
                //关闭
                mPresenter.stopScreenCapture()
            } else {
                if (isCloseVideo) {
                    mPresenter.getTRTCCloudManager().muteLocalVideo(false)
                    tx_business_video.isSelected = false
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    TxLogUtils.i("AndroidSystemUtil.getDevice()", AndroidSystemUtil.getDevice())
                    if (!PermissionUtils.isGrantedDrawOverlays()) {
                        TxLogUtils.i("需要打开悬浮窗权限")
                        PermissionUtils.requestDrawOverlays(object :
                            PermissionUtils.SimpleCallback {
                            override fun onGranted() {

                                mPresenter.startScreenCapture()
                            }

                            override fun onDenied() {
                                ToastUtils.showShort("未开启悬浮窗权限")
                            }
                        })
                    } else {

                        TxLogUtils.i("startScreenCapture")
                        mPresenter.startScreenCapture()
                    }

                } else {

                    TxLogUtils.i("startScreenCapture")
                    mPresenter.startScreenCapture()
                }

            }


        } else if (id == R.id.tx_paint) {
            //点击画笔
            tx_board_view_business.visibility = if (isShowPaint) {
                //隐藏
                mBoard?.isDrawEnable = true
                tx_paint.setImageResource(R.drawable.tx_paint_default)
                val eduBoardColor = TEduBoardColor("#f99404")
                mBoard!!.textColor = eduBoardColor
                mBoard!!.toolType = TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_PEN
                View.GONE
            } else {
                //展示
                mBoard?.isDrawEnable = true
                tx_paint.setImageResource(R.drawable.tx_paint_check)
                val eduBoardColor = TEduBoardColor("#f99404")
                mBoard!!.textColor = eduBoardColor
                mBoard!!.toolType = TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_PEN

                tx_label.isSelected = true
                tx_eraser.isSelected = false
                tx_zoom.isSelected = false
                View.VISIBLE
            }


            isShowPaint = !isShowPaint
        } else if (id == R.id.tx_label) {
            //画笔
            val eduBoardColor = TEduBoardColor("#f99404")
            mBoard!!.textColor = eduBoardColor
            mBoard!!.toolType = TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_PEN

            tx_label.isSelected = true
            tx_eraser.isSelected = false
            tx_zoom.isSelected = false

        } else if (id == R.id.tx_eraser) {
            //画圆
//            val tEduBoardCursorIcon = TEduBoardController.TEduBoardCursorIcon()
//            tEduBoardCursorIcon.url ="https://spkf-zsxn-prod.s3.cn-north-1.amazonaws.com.cn/pic/micromute.png"
//            mBoard?.setCursorIcon(TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_ERASER,tEduBoardCursorIcon)
            mBoard!!.toolType = TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_ERASER
            tx_label.isSelected = false
            tx_eraser.isSelected = true
            tx_zoom.isSelected = false

        } else if (id == R.id.tx_zoom) {
            //移动
            mBoard!!.toolType = TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_ZOOM_DRAG
            tx_label.isSelected = false
            tx_eraser.isSelected = false
            tx_zoom.isSelected = true
        }
    }

    var picQuickAdapter: PicQuickAdapter? = null


    //初始化缩略图list
    private fun initPicAdapter() {
        val mLllayoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        picQuickAdapter =
            PicQuickAdapter()
        tx_rv.layoutManager = mLllayoutManager
        tx_rv.adapter = picQuickAdapter

        picQuickAdapter?.setOnItemClickListener { adapter, view, position ->
            TxLogUtils.i("txsdk---onItemClick---${boardIdList?.get(position)}")
            mBoard?.gotoBoard(boardIdList?.get(position))
        }
    }

    private fun changeView(
        isCheck: Boolean,
        view: ImageView,
        @DrawableRes checkDra: Int,
        @DrawableRes defaultDra: Int
    ) {
        view.setImageResource(
            if (isCheck) {
                checkDra
            } else {
                defaultDra
            }
        )
    }


    private var isShowPaint = false


    override fun onBackPressed() {
        showExitInfoDialog()
    }


    private var mFloatingWindow: FloatingView? = null

    override fun showFloatingWindow() {


        if (mFloatingWindow == null) {
            mFloatingWindow =
                FloatingView(
                    TXSdk.getInstance().application,
                    R.layout.tx_meeting_screen_capture_floating_window
                )
        }

        mFloatingWindow?.setTouchButtonClickListener(object :
            FloatingView.OnTouchButtonClickListener {
            override fun onClick() {
                val activity = PendingIntent.getActivity(
                    TXSdk.getInstance().application,
                    130,
                    Intent(this@VideoActivity, VideoActivity::class.java),
                    0
                )
                activity.send()
            }

        })
        if (!mFloatingWindow?.isShown!!) {
            mFloatingWindow?.show()

        }
    }

    override fun hideFloatingWindow() {
        mFloatingWindow?.hideView()
        mFloatingWindow = null
    }


    private var mVideoFloatingWindow: FloatingView? = null
    var tv_timer: TextView? = null
    override fun showVideoFloatingWindow() {
        moveTaskToBack(true)
        if (mVideoFloatingWindow == null) {
            mVideoFloatingWindow =
                FloatingView(
                    TXSdk.getInstance().application,
                    R.layout.tx_meeting_video_floating_window
                )
        }

        tv_timer = mVideoFloatingWindow?.findViewById<TextView>(R.id.tv_timer)


        mVideoFloatingWindow?.setTouchButtonClickListener(object :
            FloatingView.OnTouchButtonClickListener {
            override fun onClick() {
                val activity = PendingIntent.getActivity(
                    TXSdk.getInstance().application,
                    130,
                    Intent(this@VideoActivity, VideoActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    },
                    0
                )
                activity.send()
                hideVideoFloatingWindow()
            }

        })
        if (!mVideoFloatingWindow?.isShown!!) {
            mVideoFloatingWindow?.show()

        }
//        skipCaller()
    }

    override fun hideVideoFloatingWindow() {
        mVideoFloatingWindow?.hideView()
        mVideoFloatingWindow = null
    }

    override fun getRemoteUserViewById(userId: String?, steamType: Int): MeetingVideoView? {
//        var view = mTRTCVideoLayout!!.findCloudViewView(userId, steamType)
//        if (view == null) {
//            view = mTRTCVideoLayout!!.allocCloudVideoView(userId, steamType, "")
//        }
        return mViewVideo
    }

    override fun onSnapshotRemoteView(bm: Bitmap?) {
    }

    override fun onRemoteViewStatusUpdate(userId: String?, enable: Boolean) {
    }


    override fun initBoard() {
        mBoard = mPresenter.getTicManager().boardController
        TxLogUtils.i("mBoard-------${mBoard?.isDataSyncEnable}")
        mBoard?.isDrawEnable = false
    }

    override fun joinClassroom() {
        //1、设置白板的回调
        mBoardCallback =
            MyBoardCallback(this)
        mPresenter.joinClassroom(mBoardCallback!!)
    }


    override fun resetBoardLayout() {
        isShareScreen = false
        mBoard?.isDrawEnable = false
        tx_board_view_business.visibility = View.GONE
        tx_paint.visibility = View.GONE
        mBoardContainer?.visibility = View.GONE
        removeBoardView()
    }

    private var boardIdList = ArrayList<String>()

    fun onTEBAddBoard(boardId: List<String>, fileId: String) {
        TxLogUtils.i("onTEBAddBoard------boardId ${boardId.size}---onTEBAddBoard:$fileId")
        boardIdList?.clear()
        boardIdList?.addAll(boardId)
    }

    fun addBoardView() {
        TxLogUtils.i("addBoardView---init")
        val eduBoardColor = TEduBoardColor("#f99404")

        val boardview = mBoard!!.boardRenderView
        val layoutParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        mBoardContainer?.removeAllViews()
        mBoardContainer!!.addView(boardview, layoutParams)
        mBoard?.apply {
            isDrawEnable = true
            textColor = eduBoardColor
            toolType = TEduBoardController.TEduBoardToolType.TEDU_BOARD_TOOL_TYPE_PEN
            boardContentFitMode =
                TEduBoardController.TEduBoardContentFitMode.TEDU_BOARD_CONTENT_FIT_MODE_NONE
        }
        tx_paint.visibility = View.VISIBLE
        tx_board_view_business.visibility = View.GONE
        mPresenter.getTicManager().addIMMessageListener(this)
    }


    //展示录像计时器
    var timer: CountDownTimer? = null
    var mCurrentTimer = 0L
    private fun startTimer() {
        if (timer == null) {
            timer = object : CountDownTimer(60000, 1000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    mCurrentTimer += 1000
                    val formatHMS = DatetimeUtil.getFormatHMS(mCurrentTimer)
                    tx_time.text = formatHMS
                    mVideoFloatingWindow?.takeIf { it.isShown }.apply { tv_timer?.text = formatHMS }
                }

                override fun onFinish() {
                    timer!!.start()

                }
            }

            timer!!.start()
        }


    }

    override fun stopTimer() {
        timer?.cancel()
        timer = null
    }


    private fun removeBoardView() {
        if (mBoard != null) {
            val boardview = mBoard!!.boardRenderView
            if (mBoardContainer != null && boardview != null) {
                mBoardContainer!!.removeView(boardview)
            }
        }
    }


    private fun sendMSG() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionUtils.permission(
                PermissionConstants.SMS
            ).callback(object :
                PermissionUtils.FullCallback {
                override fun onGranted(permissionsGranted: List<String>) {
                    //发送短信
                    sendSystemMSG()
                }

                override fun onDenied(
                    permissionsDeniedForever: List<String>,
                    permissionsDenied: List<String>
                ) {
                    ToastUtils.showShort("发送短信，权限被拒绝！")

                }
            }
            ).request()

        } else {
            sendSystemMSG()
        }

    }

    private fun sendSystemMSG() {
        startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")).apply {
            putExtra(
                "sms_body",
                "请打开微信-搜索【云助理智慧展业】小程序，输入邀请码 ${mPresenter.getInviteNumber()} 进入会议"
            )
        })
    }


    override fun getContentViewId(): Int {

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        return R.layout.tx_activity_video
    }


    override fun createPresenter(): VideoPresenter {

        return VideoPresenter(this, this)
    }


    override fun init(savedInstanceState: Bundle?) {
        val stringExtra = intent.getStringExtra(KEY_DATA)

        mPresenter.setTRTCParams(stringExtra)

        openImmersionBar()
        mPresenter.initVideoSDK()

        initViews()

        mPresenter.enterRoom()
        mPresenter.startLocalPreview(mViewVideo!!)
        mPresenter.loginImRoom()
        showBroad()
        startTimer()
    }


    override fun onLoadFailed() {
    }

    override fun onLoading() {
    }

    override fun onLoadSuccess() {
    }

    override fun onTICRecvTextMessage(fromUserId: String?, text: String?) {
        TxLogUtils.i("onTICRecvTextMessage$text")
    }

    override fun onTICRecvMessage(message: TIMMessage?) {
        TxLogUtils.i("onTICRecvMessage${message?.msgId}")
    }

    var isStartShare = false
    override fun onTICRecvGroupTextMessage(fromUserId: String?, text: String?) {
        TxLogUtils.i("onTICRecvGroupTextMessage$text")
        val jsonObject = JSONObject(text)
        val hasType = jsonObject.has("type")
        if (hasType) {
            val type = jsonObject.getString("type")
            val hasData = jsonObject.has("data")

            when (type) {
                "notifyExtend" -> {
                    if (hasData) {
                        if (mPresenter.isOwner()) {
                            val dataJO = jsonObject.getJSONObject("data")
                            val extendRoomTime = dataJO.getInt("extendRoomTime")
                            val notifyExtendTime = dataJO.getInt("notifyExtendTime")
                            showTimerDialog("2", 0, extendRoomTime, notifyExtendTime, 0)
                        }
                    }

                }
                "notifyEnd" -> {
                    if (hasData) {
                        if (mPresenter.isOwner()) {
                            val dataJO = jsonObject.getJSONObject("data")
                            val extendRoomTime = dataJO.getInt("extendRoomTime")
                            val notifyEndTime = dataJO.getInt("notifyEndTime")
                            showTimerDialog("3", 0, extendRoomTime, 0, notifyEndTime)
                        }
                    }
                }
                // end 关闭视频
                "end" -> {
                    destroyRoom()
                }
                //用户大屏幕显示
                "startShare" -> {
                    isStartShare = true
                    checkItemToBig()
                }
                "endShare" -> {
                    if (bigMeetingEntity != null) {
                        isStartShare = false
                        detach()
                    }
                }
                else -> {
                }
            }


        }


    }

    var mScreenView: ScreenView? = null

    fun checkItemToBig() {
        if (mScreenView==null){
            bigscreen.inflate()
            mScreenView = findViewById<ScreenView>(R.id.view_bigscreen)
        }
        mScreenView?.setBigScreenCallBack(this)
        val audioConfig = ConfigHelper.getInstance().audioConfig
        val videoConfig = ConfigHelper.getInstance().videoConfig
        mScreenView?.muteAudio(!audioConfig.isEnableAudio)
        mScreenView?.muteVideo(!videoConfig.isEnableVideo)


        TxLogUtils.i("checkItemToBig-----checkItemToBig")
        val ownerUserId = mPresenter.getOwnerUserId()
        if (!ownerUserId.isEmpty()) {
            val entity =
                mPresenter.getStringMemberEntityMap()[ownerUserId]
            val meetingVideoView = entity?.meetingVideoView
            if (bigMeetingEntity == null) {
                bigMeetingEntity = MemberEntity()
            }
            bigMeetingEntity?.meetingVideoView = meetingVideoView
            bigMeetingEntity?.userId = entity?.userId
            //没有Parent
            meetingVideoView?.detach()
            meetingVideoView?.addViewToViewGroup(mScreenView?.bigScreenView)
//            entity?.isVideoAvailable = true
//            entity?.isMuteVideo = false
            mScreenView?.visibility = View.VISIBLE
            mPresenter.getTRTCRemoteUserManager().setRemoteFillMode(
                entity?.userId,
                TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,
                false
            )
            entity?.isShowOutSide = true
        }

    }

    fun detach() {
        TxLogUtils.i("detach------")
        val ownerUserId = mPresenter.getOwnerUserId()
        if (ownerUserId.isNotEmpty()) {
            mScreenView?.bigScreenView?.removeAllViews()
            val entity =
                mPresenter.getStringMemberEntityMap()[ownerUserId]
            val mMeetingVideoView = bigMeetingEntity?.meetingVideoView
            entity?.apply {
                meetingVideoView = mMeetingVideoView
                isShowOutSide = false
                isNeedFresh = true
            }

            mScreenView?.visibility = View.GONE
            mPresenter.getTRTCRemoteUserManager().setRemoteFillMode(
                entity?.userId,
                TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG,
                true
            )
            mMemberListAdapter?.notifyItemChanged(
                mPresenter.getMemberEntityList().indexOf(
                    entity
                )
            )
            bigMeetingEntity = null
        }


    }


    override fun onTICRecvCustomMessage(fromUserId: String?, data: ByteArray?) {
    }

    override fun onTICRecvGroupCustomMessage(fromUserId: String?, data: ByteArray?) {
    }

    override fun showInviteBt(isShow: Boolean) {
        tv_invite.visibility = if (TXSdk.getInstance().txConfig.isShowInviteButton) {
            if (isShow) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        } else {
            View.INVISIBLE
        }

    }

    override fun showShareFileBt(isShow: Boolean, isEnable: Boolean) {
        tx_business_share.visibility = if (isShow) {
            View.VISIBLE
        } else {
            View.GONE
        }
        tx_business_share.isEnabled = isEnable
        tx_business_share.isSelected = !isEnable

    }

    companion object {
        const val KEY_ROOM_ID = "room_id"
        const val KEY_USER_ID = "user_id"
        const val KEY_DATA = "tx_data"

        @JvmStatic
        public fun startAc(context: Context, room_id: Int, user_id: String) {

            context.startActivity(Intent(context, VideoActivity::class.java).apply {
                putExtra(KEY_ROOM_ID, room_id)
                putExtra(KEY_USER_ID, user_id)
            })
        }

        @JvmStatic
        public fun startAc(context: Activity, tx_data: String) {
            val intent = Intent(context, VideoActivity::class.java).apply {
                putExtra(KEY_DATA, tx_data)
            }
            context.startActivity(intent)
        }

        @JvmStatic
        public fun startAc(context: Activity) {
            context.startActivity(Intent(context, VideoActivity::class.java).apply {
            })
        }
    }

    override fun onFinishClick() {
        showExitInfoDialog()
    }

    override fun onSwitch() {
        switchCamera()
    }

    override fun onMuteAudioClick() {
        muteLocalAudio()
        val audioConfig = ConfigHelper.getInstance().audioConfig
        mScreenView?.muteAudio(!audioConfig.isEnableAudio)
    }

    override fun onMuteVideoClick() {
        muteLocalVideo()
        val videoConfig = ConfigHelper.getInstance().videoConfig
        mScreenView?.muteVideo(!videoConfig.isEnableVideo)
    }


}