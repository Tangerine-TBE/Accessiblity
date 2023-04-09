package cn.com.dihealth.myapplication

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

/*Android 无障碍*/
class MyAccessibilityService : AccessibilityService() {
    private val TAG = "MyAccessibilityService"
    private  var mainScope :CoroutineScope?= null
    private val  broadCast by lazy {
        BroadcastHandler(this)
    }

    //点击间隔
    private var mInterval = -1L

    //点击坐标xy
    private var mPointX = -1f
    private var mPointY = -1f
    //悬浮窗视图
    private lateinit var mFloatingView: FloatingClickView

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.e("MyAccessibilityService","onAccessibilityEvent")
    }
    companion object {

        //打开悬浮窗
        val ACTION_SHOW = "action_show"

        //自动点击事件 开启/关闭
        val ACTION_PLAY = "action_play"
        val ACTION_STOP = "action_stop"

        //关闭悬浮窗
        val ACTION_CLOSE = "action_close"
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        mFloatingView = FloatingClickView(this)
        broadCast.register()
    }
    private fun startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationBuilder =
                NotificationCompat.Builder(this, NotificationConstants.CHANNEL_ID)
            val notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
            startForeground(-1, notification)

        } else {
            startForeground(-1, Notification())
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun autoClickView(x: Float, y: Float) {
        mainScope?.launch {
                delay(mInterval)
                Log.d(TAG, "auto click x:$x  y:$y")
                val path = Path()
                path.moveTo(x, y)
                val gestureDescription = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 100L, 1000L))
                    .build()
                dispatchGesture(
                    gestureDescription,
                    object : AccessibilityService.GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) {
                            super.onCompleted(gestureDescription)
                            Log.d(TAG, "自动点击完成")
                        }

                        override fun onCancelled(gestureDescription: GestureDescription?) {
                            super.onCancelled(gestureDescription)
                            Log.d(TAG, "自动点击取消")
                        }
                    },
                    null
                )
        }
    }
    private fun autoFlipScreen() {
        mainScope?.launch {
            while (true) {
                delay(mInterval)
                Log.d(TAG, "auto flip screen")
                val path = Path()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        broadCast.unregister()
        mainScope?.cancel()
    }

    override fun onInterrupt() {
        Log.e("MyAccessibilityService","onInterrupt")
    }
    private inner class BroadcastHandler(val context: Context) : BroadcastReceiver() {

        fun register() {
            context.registerReceiver(
                this,
                IntentFilter().apply {
                    addAction(BroadcastConstants.BROADCAST_ACTION_AUTO_CLICK)
                    //息屏关闭自动点击事件
                    addAction(Intent.ACTION_SCREEN_OFF)
                }
            )
        }

        fun unregister() {
            context.unregisterReceiver(this)
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onReceive(p0: Context?, intent: Intent?) {
            intent?.apply {
                when(action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        mFloatingView.remove()
                        mainScope?.cancel()
                    }
                    BroadcastConstants.BROADCAST_ACTION_AUTO_CLICK -> {
                        when (getStringExtra(BroadcastConstants.KEY_ACTION)) {
                            ACTION_SHOW -> {
                                mFloatingView.remove()
                                mainScope?.cancel()
                                mInterval = getLongExtra(BroadcastConstants.KEY_INTERVAL, 5000)
                                mFloatingView.show()
                            }
                            ACTION_PLAY -> {
                                mPointX = getFloatExtra(BroadcastConstants.KEY_POINT_X, 0f)
                                mPointY = getFloatExtra(BroadcastConstants.KEY_POINT_Y, 0f)
                                mainScope = MainScope()
                                autoClickView(mPointX, mPointY)
                            }
                            ACTION_STOP -> {
                                mainScope?.cancel()
                            }
                            ACTION_CLOSE -> {
                                mFloatingView.remove()
                                mainScope?.cancel()
                            }
                            else -> {
                                Log.e(TAG, "action error")
                            }
                        }
                    }
                }
            }
        }
    }

}