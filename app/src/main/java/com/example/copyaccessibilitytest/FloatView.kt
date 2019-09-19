package com.example.copyaccessibilitytest

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.FrameLayout
import android.widget.TextView
import timber.log.Timber
import java.util.ArrayList

class FloatView : RecognitionListener {

    private fun initFloatingWindow() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext)
        mSpeechRecognizer!!.setRecognitionListener(this)

        mRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mRecognizerIntent!!.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en")
        mRecognizerIntent!!.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mContext!!.packageName)
        mRecognizerIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        mSpeechRecognizer!!.startListening(mRecognizerIntent)

        mFloatingTextFrameLayout = View.inflate(mContext, R.layout.floating_text, null) as FrameLayout
        mFloatingTextFrameLayout!!.setOnClickListener({
            Timber.d("Click mFloatingTextFrameLayout.")
        })

        mFloatingTextView = mFloatingTextFrameLayout!!.findViewById(R.id.floating_text_view)
    }

    private fun setUpFloatingTextLayoutParams() {
        mFloatingLayoutParams = LayoutParams()
        mFloatingLayoutParams!!.type = LayoutParams.TYPE_APPLICATION_OVERLAY
        mFloatingLayoutParams!!.format = PixelFormat.TRANSLUCENT
        mFloatingLayoutParams!!.flags = (LayoutParams.FLAG_NOT_FOCUSABLE
                or LayoutParams.FLAG_NOT_TOUCH_MODAL)
        mFloatingLayoutParams!!.width = LayoutParams.WRAP_CONTENT
        mFloatingLayoutParams!!.height = LayoutParams.WRAP_CONTENT
        mFloatingLayoutParams!!.gravity = Gravity.CENTER_VERTICAL or Gravity.BOTTOM
    }

    private fun addFloatingView(frameLayout: FrameLayout?, flParams: LayoutParams?) {
        Timber.d("addFloatingView.")
        try {
            var wm: WindowManager = mContext!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.addView(frameLayout, flParams)
        } catch (e: Exception) {
            Timber.d("add window view err: " + e.message)
        }
    }

    private fun removeFloatingView(frameLayout: FrameLayout) {
        Timber.d("removeFloatingView.")
        try {
            var wm: WindowManager = mContext!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.removeView(frameLayout)
        } catch (e: Exception) {
            Timber.d("remove window view err: " + e.message)
        }
    }

    private fun stopRecognition() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onReadyForSpeech(p0: Bundle?) {
        Timber.d("onReadyForSpeech")
    }

    override fun onRmsChanged(p0: Float) {
//        Timber.d("onRmsChanged: " + p0)
    }

    override fun onBufferReceived(p0: ByteArray?) {
        Timber.d("onBufferReceived")
    }

    override fun onPartialResults(p0: Bundle?) {
        Timber.d("onPartialResults")
    }

    override fun onEvent(p0: Int, p1: Bundle?) {
        Timber.d("onEvent")
    }

    override fun onBeginningOfSpeech() {
        Timber.d("onBeginningOfSpeech")
    }

    override fun onEndOfSpeech() {
        Timber.d("onEndOfSpeech")
    }

    override fun onError(p0: Int) {
        val message: String
        when (p0) {
            SpeechRecognizer.ERROR_AUDIO -> message = "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> message = "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> message = "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> message = "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> message = "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> message = "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> message = "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> message = "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> message = "No speech input"
            else -> message = "Didn't understand, please try again."
        }
        Timber.d("Error: $message")
//        stopRecognition()
    }

    override fun onResults(p0: Bundle?) {
        var resList: ArrayList<String>?
                = p0!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var text: String = ""
        for (result in resList!!) {
            text += result + "\n"
        }
        Timber.d("onResults: " + text)
        mFloatingTextView!!.setText(text)
//        mHandler!!.postDelayed(mStopRecogRunnable, 3000)
    }

    companion object {
        private var mContext: Context ?= null
        private var mFloatingTextFrameLayout: FrameLayout ?= null
        private var mFloatingTextView: TextView ?= null
        private var mFloatingLayoutParams: LayoutParams ?=null
        private var mInstance: FloatView ?= null
//        private var mHandler: Handler ?= null
//        private var mStopRecogRunnable: Runnable ?= null
        private var mSpeechRecognizer: SpeechRecognizer ?= null
        private var mRecognizerIntent: Intent ?= null

        fun init(context: Context) {
            mContext = context
//            mHandler = Handler()
//            mStopRecogRunnable = Runnable { stopRecognition() }

            mInstance!!.initFloatingWindow()
            mInstance!!.setUpFloatingTextLayoutParams()
            mInstance!!.addFloatingView(mFloatingTextFrameLayout, mFloatingLayoutParams)
        }

        fun getInstance(): FloatView {
            if (mInstance == null) {
                mInstance = FloatView()
            }
            return  mInstance!!
        }
    }
}