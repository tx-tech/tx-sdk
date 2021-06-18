package com.txt.video.ui.video

import android.net.Uri
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.trtc.TRTCCloudDef
import com.txt.video.base.IBaseView
import com.txt.video.ui.trtc.TICManager
import com.txt.video.ui.trtc.TRTCCloudManager
import com.txt.video.ui.trtc.remoteuser.TRTCRemoteUserManager
import com.txt.video.ui.trtc.ticimpl.utils.MyBoardCallback
import com.txt.video.ui.trtc.videolayout.list.MeetingVideoView
import com.txt.video.ui.trtc.videolayout.list.MemberEntity
import org.json.JSONObject

/**
 * Created by JustinWjq
 * @date 2019-12-23.
 * description：
 */
public class Contract {

    interface ICollectView : IBaseView {
        fun hideFloatingWindow()

        fun showFloatingWindow()

        fun processSelfVideoPlay()

        fun joinClassroom()

        fun initBoard()

        fun updateAdapter(json: String)

        fun uploadFileSuccess()

        fun uploadFileFail(msg: String)

        fun sendReq(req: BaseReq)

        fun startShareSuccess()

        fun startShareFail()

        fun getRoomInfoSuccess(
            json: String?,
            userId: String,
            streamType: Int,
            entity: MemberEntity?,
            available: Boolean
        )

        fun showSuccess()

        fun showFail()

        fun showInviteBt(isShow: Boolean)

        fun showShareFileBt(isShow: Boolean, isEnable: Boolean)

        fun showTitle()

        fun showVideoFloatingWindow()

        fun hideVideoFloatingWindow()

        fun resetBoardLayout()

        fun destroyRoom()

        fun initViews()

        fun stopTimer()

        fun finishPage()

        fun muteLocalAudio()

        fun muteLocalVideo()

        fun switchCamera()

        fun initBottomBt()
    }


    interface ICollectPresenter {
        fun initMembersData()

        fun addMemberEntity(entity: MemberEntity)

        fun removeMemberEntity(userId: String): Int

        fun getTRTCParams(): TRTCCloudDef.TRTCParams

        fun setTRTCParams(stringExtra: String)

        fun getMemberEntityList(): ArrayList<MemberEntity>

        fun getStringMemberEntityMap(): HashMap<String, MemberEntity>

        fun getRoomId(): Int

        fun getServiceId(): String

        fun getAgentId(): String

        fun getGroupId(): String

        fun getMaxRoomUser(): Int

        fun getMaxRoomTime(): Int

        fun getInviteNumber(): String

        fun startRecord()

        fun endRecord()

        fun initVideoSDK()

        fun enterRoom()

        fun loginImRoom()

        fun logoutClassRoom()

        fun startLocalPreview(viewVideo: MeetingVideoView)

        fun stopLocalPreview()

        fun startScreenCapture()

        fun stopScreenCapture()

        fun getTRTCCloudManager(): TRTCCloudManager

        fun getTRTCRemoteUserManager(): TRTCRemoteUserManager

        fun getTicManager(): TICManager

        fun exitRoom()

        fun initTICManager()

        fun joinClassroom(mBoardCallback: MyBoardCallback)

        fun sendGroupMessage(msg: String)

        fun update()

        fun uploadFile(data1: Uri?)

        fun requestWX()

        fun startShare()

        fun setScreenStatus(screenStatus: Boolean)

        fun deleteFile(id: String?)

        fun getRoomInfo(
            userId: String,
            streamType: Int,
            entity1: Boolean,
            entity: MemberEntity?
        )

        fun processVideoPlay(fromItem: Int, toItem: Int)

        fun setIMTextData(type: String): JSONObject

        fun extendTime()

        fun isOwner(): Boolean

        fun getOwnerUserId(): String

        fun getRoomScreenStatus(): Boolean

        fun unitConfig()
    }
}