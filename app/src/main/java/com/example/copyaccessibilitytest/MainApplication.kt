package com.example.copyaccessibilitytest

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.facebook.stetho.Stetho
import timber.log.Timber

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG){
            //初始化Timber
            Timber.plant(Timber.DebugTree())
            Stetho.initializeWithDefaults(this)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)) {
                var intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + packageName))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                applicationContext.startActivity(intent!!)

//                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) !== PackageManager.PERMISSION_GRANTED) {
//                    requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
//                }

            }
            if (!isAccessibilitySettingOn("BaseAccessibilityService", this))
                jump2Setting(this)

            BaseAccessibilityService.init(this)
        }
    }

    fun isAccessibilitySettingOn(accessibilityServiceName: String, context: Context): Boolean {
        var accessibilityEnable: Int = 0
        var serviceName: String = context.packageName + "/" + accessibilityServiceName
        try {
            accessibilityEnable = Settings.Secure.getInt(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0)

            if (accessibilityEnable == 1) {
                var services = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
                var split = TextUtils.SimpleStringSplitter(':')
                split.setString(services)
                while(split.hasNext()) {
                    if (split.next().equals(serviceName)) {
                        Timber.d("Accessibility service enable")
                        return true
                    }
                }
            }
            Timber.d("Accessibility service disable")
        } catch (e: Exception) {
            Timber.e(e.message)
        }
        return false
    }

    fun jump2Setting(context: Context) {
        try {
            var intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent!!)
        } catch (e: Throwable) {
            try {
                var intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e2: Throwable) {
                Timber.e(e2.message)
            }
        }
    }
}