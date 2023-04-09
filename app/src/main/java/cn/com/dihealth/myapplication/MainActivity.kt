package cn.com.dihealth.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import cn.com.dihealth.myapplication.databinding.ActivityMainBinding
import cn.com.dihealth.myapplication.utils.AccessibilitySettingUtils
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private val TAG = javaClass::class.java.canonicalName
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNotification()
        initListener()
        startAutoClickService()
    }

    private fun initListener() {
        binding.btnAccessibility.setOnClickListener {
            checkAccessibility()
        }

        binding.btnFloatingWindow.setOnClickListener {
            checkFloatingWindow()
        }

        binding.btnShowWindow.setOnClickListener {
            hideKeyboard()
            if (TextUtils.isEmpty(binding.etInterval.text.toString())) {
                Snackbar.make(binding.etInterval, "请输入间隔", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showFloatingWindow(binding.etInterval.text.toString().toLong())
        }

        binding.btnCloseWindow.setOnClickListener {
            closeFloatWindow()
        }

        binding.btnTest.setOnClickListener {
            Log.d(TAG, "btn_test on click")
        }
    }

    /**
     * 跳转设置开启无障碍
     */
    private fun checkAccessibility() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    /**
     * 跳转设置顶层悬浮窗
     */
    private fun checkFloatingWindow() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "已开启", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivity(intent)
            }
        }
    }

    private fun startAutoClickService() {
        val intent = Intent(this, MyAccessibilityService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun showFloatingWindow(interval: Long) {
        sendBroadcast(Intent().apply {
            action = BroadcastConstants.BROADCAST_ACTION_AUTO_CLICK
            putExtra(BroadcastConstants.KEY_ACTION, MyAccessibilityService.ACTION_SHOW)
            putExtra(BroadcastConstants.KEY_INTERVAL, interval)
        })
    }

    private fun closeFloatWindow() {
        sendBroadcast(Intent().apply {
            action = BroadcastConstants.BROADCAST_ACTION_AUTO_CLICK
            putExtra(BroadcastConstants.KEY_ACTION, MyAccessibilityService.ACTION_CLOSE)
        })
    }


    private fun initNotification() {
        //注册渠道id
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = NotificationConstants.CHANNEl_NAME
            val descriptionText = NotificationConstants.CHANNEL_DES
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(NotificationConstants.CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
            channel.enableLights(true)
            channel.lightColor = Color.GREEN
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        val intent = Intent(this, MyAccessibilityService::class.java)
        stopService(intent)
        super.onDestroy()
    }

    //收起输入法
    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive && currentFocus != null) {
            imm.hideSoftInputFromWindow(
                currentFocus!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }


}