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

//        FloatView.getInstance()
//        FloatView.init(this)

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
            Timber.d("trigger!")
//            BaseAccessibilityService.getInstance().performAction("click send")
        }

        commandTrigger.setOnClickListener{
            Timber.d("send!")
//            BaseAccessibilityService.getInstance().performAction("click TRIGGER")
        }
    }
}
