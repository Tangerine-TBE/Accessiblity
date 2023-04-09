package cn.com.dihealth.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView

class FloatingClickView(private  val mContext: Context) :FrameLayout(mContext){
    private lateinit var mWindowManager: FloatingManager
    private var mParams: WindowManager.LayoutParams? = null

    private lateinit var mView: View

    //按下坐标
    private var mTouchStartX = -1f
    private var mTouchStartY = -1f

    val STATE_CLICKING = "state_clicking"
    val STATE_NORMAL = "state_normal"
    private var mCurrentState = STATE_NORMAL

    private var ivIcon: AppCompatImageView? = null

    init {
        initView()
    }
    private fun initView() {
        mView = LayoutInflater.from(context).inflate(R.layout.view_floating_click, null)
        ivIcon = mView.findViewById(R.id.iv_icon)
        mWindowManager = FloatingManager.getInstance(mContext)
        initListener()
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        mView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mTouchStartX = event.rawX
                    mTouchStartY = event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    mParams?.let {
                        it.x += (event.rawX - mTouchStartX).toInt()
                        it.y += (event.rawY - mTouchStartY).toInt()
                        mWindowManager.updateView(mView, it)
                    }
                    mTouchStartX = event.rawX
                    mTouchStartY = event.rawY
                }
            }
            false
        }

        mView.setOnClickListener {

            val location = IntArray(2)
            it.getLocationOnScreen(location)

            val intent = Intent().apply {
                action = BroadcastConstants.BROADCAST_ACTION_AUTO_CLICK
                when (mCurrentState) {
                    STATE_NORMAL -> {
                        mCurrentState = STATE_CLICKING
                        putExtra(BroadcastConstants.KEY_ACTION, MyAccessibilityService.ACTION_PLAY)
                        putExtra(BroadcastConstants.KEY_POINT_X, (location[0] - 1).toFloat())
                        putExtra(BroadcastConstants.KEY_POINT_Y, (location[1] - 1).toFloat())
                        ivIcon?.setImageResource(R.drawable.arm)
                    }
                    STATE_CLICKING -> {
                        mCurrentState = STATE_NORMAL
                        putExtra(BroadcastConstants.KEY_ACTION, MyAccessibilityService.ACTION_STOP)
                        ivIcon?.setImageResource(R.drawable.amr1)
                    }
                }
            }

            context.sendBroadcast(intent)
        }
    }


    fun show() {
        mParams = WindowManager.LayoutParams()
        mParams?.apply {
            gravity = Gravity.CENTER
            //总是出现在应用程序窗口之上
            type = if (Build.VERSION.SDK_INT >= 26) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            //设置图片格式，效果为背景透明
            format = PixelFormat.RGBA_8888

            flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH

            width = LayoutParams.WRAP_CONTENT
            height = LayoutParams.WRAP_CONTENT
            if (mView.isAttachedToWindow) {
                mWindowManager.removeView(mView)
            }
            mWindowManager.addView(mView, this)
        }
    }

    fun remove() {
        mCurrentState = STATE_NORMAL
        ivIcon?.setImageResource(R.drawable.amr1)
        mWindowManager.removeView(mView)
    }


}