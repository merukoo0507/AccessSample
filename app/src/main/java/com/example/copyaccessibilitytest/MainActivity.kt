package com.example.copyaccessibilitytest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            }
        }

        voiceTrigger.setOnClickListener{
            Timber.d("pf: trigger!")
            BaseAccessibilityService.getInstance().matchActionString("send,test")
        }

        layout.setOnClickListener{
            Timber.d("pf: send!")
//            BaseAccessibilityService.getInstance().performAction("click TRIGGER")
        }

        back.setOnClickListener{
            Timber.d("pf: back!")
            BaseAccessibilityService.getInstance().matchActionString("back")
        }

        home.setOnClickListener{
            Timber.d("pf: 首頁!")
            BaseAccessibilityService.getInstance().matchActionString("首頁")
        }

        scrollUp.setOnClickListener{
            Timber.d("pf: scrollUp!")
            BaseAccessibilityService.getInstance().matchActionString("scroll up")
        }

        scrollDown.setOnClickListener{
            Timber.d("pf: scrollDown!")
            BaseAccessibilityService.getInstance().matchActionString("scroll down")
        }
    }
}
