package com.txt.video.widget.floatview

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.ActivityManager
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import android.widget.PopupWindow
import com.txt.video.net.utils.TxLogUtils.i

/**
 * Created by JustinWjq
 *
 * @date 2020/9/2.
 * description：
 */
/**
 * 悬浮球，点击可以弹出菜单
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class FloatingView : FrameLayout,
    View.OnClickListener {
    private var mContext: Context
    private var mWindowManager: WindowManager? = null
    private var mGestureDetector: GestureDetector ? = null
    private var mLayoutParams: WindowManager.LayoutParams? = null
    private var mLastX = 0f
    private var mLastY = 0f
    private var mPopupWindow: PopupWindow? = null
    private var mTapOutsideTime: Long = 0
    private var mIsShowing = false

    constructor(context: Context) : super(context) {
        mContext = context
//        mGestureDetector = GestureDetector(context, this)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        mContext = context
//        mGestureDetector = GestureDetector(context, this)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        mContext = context
//        mGestureDetector = GestureDetector(context, this)
    }

    /**
     * 悬浮球
     *
     * @param context   建议使用application context避免activity泄漏
     * @param viewResId Resid
     */
    constructor(context: Context, viewResId: Int) : super(context) {
        mContext = context
        View.inflate(context, viewResId, this)
//        mGestureDetector = GestureDetector(context, this)

    }

    @SuppressLint("ClickableViewAccessibility")
    @JvmOverloads
    fun showView(
        view: View?,
        width: Int = WindowManager.LayoutParams.WRAP_CONTENT,
        height: Int = WindowManager.LayoutParams.WRAP_CONTENT
    ) {
        mWindowManager =
            mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        //TYPE_TOAST仅适用于4.4+系统，假如要支持更低版本使用TYPE_SYSTEM_ALERT(需要在manifest中声明权限)
        //7.1（包含）及以上系统对TYPE_TOAST做了限制
        val type: Int
        type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        mLayoutParams = WindowManager.LayoutParams(type)
        mLayoutParams!!.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        mLayoutParams!!.flags =
            mLayoutParams!!.flags or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        //layoutParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS; //no limit适用于超出屏幕的情况，若添加此flag需要增加边界检测逻辑
        mLayoutParams!!.width = width
        mLayoutParams!!.height = height
        mLayoutParams!!.gravity = Gravity.RIGHT
        mLayoutParams!!.format = PixelFormat.TRANSLUCENT
        mWindowManager!!.addView(view, mLayoutParams)
        this.setOnTouchListener(FloatingListener(mLayoutParams!!,mWindowManager!!,this))
        this.setOnClickListener(this)
    }

    fun hideView() {
        if (null != mWindowManager) {
            mWindowManager!!.removeViewImmediate(this)
        }
        mWindowManager = null
    }


    /**
     * 设置弹出菜单
     *
     * @param id resource id，根据resource id inflate 菜单
     */
    fun setPopupWindow(id: Int) {
        mPopupWindow = PopupWindow(this)
        mPopupWindow!!.width = ViewGroup.LayoutParams.WRAP_CONTENT
        mPopupWindow!!.height = ViewGroup.LayoutParams.WRAP_CONTENT
        mPopupWindow!!.isTouchable = true
        mPopupWindow!!.isOutsideTouchable = true
        mPopupWindow!!.isFocusable = false
        mPopupWindow!!.setBackgroundDrawable(BitmapDrawable())
        mPopupWindow!!.contentView = LayoutInflater.from(context).inflate(id, null)
        mPopupWindow!!.setTouchInterceptor(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                mPopupWindow!!.dismiss()
                mTapOutsideTime = System.currentTimeMillis()
                return@OnTouchListener true
            }
            false
        })
    }

    /**
     * 获取 popupWindow 顶层 view
     */
    val popupView: View
        get() = mPopupWindow!!.contentView

    /**
     * 注册点击回调
     */
    fun setOnPopupItemClickListener(listener: OnClickListener?) {
        if (mPopupWindow == null) return
        val layout = mPopupWindow!!.contentView as ViewGroup
        for (i in 0 until layout.childCount) {
            layout.getChildAt(i).setOnClickListener(listener)
        }
    }

    /**
     * 显示悬浮球
     */
    fun show() {
        if (!mIsShowing) {
            showView(this)
        }
        mIsShowing = true
    }

    /**
     * 关闭悬浮球
     */
    fun dismiss() {
        if (mIsShowing) {
            hideView()
        }
        mIsShowing = false
        // 清空 listener
        val layout = mPopupWindow!!.contentView as ViewGroup
        for (i in 0 until layout.childCount) {
            layout.getChildAt(i).setOnClickListener(null)
        }
    }

    @SuppressLint("MissingPermission")
    fun setTopApp(context: Context) {
        //获取ActivityManager
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        //获得当前运行的task(任务)
        var taskInfoList: List<ActivityManager.RunningTaskInfo>? =
            null
        if (activityManager != null) {
            taskInfoList = activityManager.getRunningTasks(100)
        }
        if (taskInfoList != null) {
            for (taskInfo in taskInfoList) {
                //找到本应用的 task，并将它切换到前台
                if (taskInfo.topActivity != null && taskInfo.topActivity.packageName == context.packageName) {
                    activityManager.moveTaskToFront(taskInfo.id, 0)
                    break
                }
            }
        }
    }

    private class FloatingListener(val wmParams: WindowManager.LayoutParams,
                                   val mWindowManager: WindowManager,
                                   val mFloatingLayout: View
    ) : View.OnTouchListener {
        //开始触控的坐标，移动时的坐标（相对于屏幕左上角的坐标）
        private var mTouchStartX = 0  //开始触控的坐标，移动时的坐标（相对于屏幕左上角的坐标）
        private var mTouchStartY = 0  //开始触控的坐标，移动时的坐标（相对于屏幕左上角的坐标）
        private var mTouchCurrentX = 0  //开始触控的坐标，移动时的坐标（相对于屏幕左上角的坐标）
        private var mTouchCurrentY = 0

        //开始时的坐标和结束时的坐标（相对于自身控件的坐标）
        private var mStartX = 0  //开始时的坐标和结束时的坐标（相对于自身控件的坐标）
        private var mStartY = 0  //开始时的坐标和结束时的坐标（相对于自身控件的坐标）
        private var mStopX = 0  //开始时的坐标和结束时的坐标（相对于自身控件的坐标）
        private var mStopY = 0

        //判断悬浮窗口是否移动，这里做个标记，防止移动后松手触发了点击事件
        private var isMove = false

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    isMove = false
                    mTouchStartX = event.rawX.toInt()
                    mTouchStartY = event.rawY.toInt()
                    mStartX = event.x.toInt()
                    mStartY = event.y.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    mTouchCurrentX = event.rawX.toInt()
                    mTouchCurrentY = event.rawY.toInt()
                    wmParams.x -= mTouchCurrentX - mTouchStartX
                    wmParams.y += mTouchCurrentY - mTouchStartY
                    mWindowManager.updateViewLayout(mFloatingLayout, wmParams)
                    mTouchStartX = mTouchCurrentX
                    mTouchStartY = mTouchCurrentY
                }
                MotionEvent.ACTION_UP -> {
                    mStopX = event.x.toInt()
                    mStopY = event.y.toInt()

                    if (Math.abs(mStartX - mStopX) >= 1 || Math.abs(mStartY - mStopY) >= 1) {
                        isMove = true
                    }
                }
                else -> {
                }
            }
            return isMove
        }


    }

    override fun onClick(v: View) {
        if (mTouchButtonClickListener != null) {
            mTouchButtonClickListener!!.onClick()
        }
    }
    private var mTouchButtonClickListener: OnTouchButtonClickListener? = null
    fun setTouchButtonClickListener(touchButtonClickListener: OnTouchButtonClickListener?) {
        mTouchButtonClickListener = touchButtonClickListener
    }

   public interface OnTouchButtonClickListener {
        fun onClick()
    }
}