package cn.com.dihealth.myapplication.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings

object AccessibilitySettingUtils {

    fun jumpToAccessibilitySetting(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        context.startActivity(intent)
    }



}