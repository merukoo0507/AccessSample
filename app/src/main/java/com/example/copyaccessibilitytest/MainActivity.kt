package com.example.copyaccessibilitytest

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

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
            Timber.d("voice trigger!")
            FloatView.getInstance().triggerRecognition()
        }

        commandTrigger.setOnTouchListener(View.OnTouchListener{ view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_DOWN -> {
                    Timber.d("command trigger!")
                    BaseAccessibilityService.getInstance().performAction("home")
                }
                MotionEvent.ACTION_UP -> {
                    //view.performClick()
                }
            }
            return@OnTouchListener true
        })
    }
}
