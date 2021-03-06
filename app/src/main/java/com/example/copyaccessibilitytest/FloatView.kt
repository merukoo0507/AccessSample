package com.example.copyaccessibilitytest

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import timber.log.Timber
import java.util.ArrayList

class FloatView(context: Context) : RecognitionListener {
    init {
        mContext = context
        mHandler = Handler()

        initFloatingWindow()
        setUpFloatingLayoutParams()
        addFloatingView(mButtonFrameLayout, mButtonLayoutParams)
    }

    private fun initFloatingWindow() {
        mStopRecogRunnable = Runnable { stopRecognition() }
        mListenRunnable = Runnable {
            mSpeechRecognizer!!.startListening(mRecognizerIntent) }

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext)
        mSpeechRecognizer!!.setRecognitionListener(this)

        mRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mRecognizerIntent!!.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-TW")
        mRecognizerIntent!!.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mContext!!.packageName)
        mRecognizerIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        mFloatingTextFrameLayout = View.inflate(mContext, R.layout.floating_text, null) as FrameLayout
        mFloatingTextView = mFloatingTextFrameLayout!!.findViewById(R.id.floating_text_view)

        mButtonFrameLayout = View.inflate(mContext, R.layout.floating_button, null) as FrameLayout
        mButtonView = mButtonFrameLayout!!.findViewById(R.id.button)
        mButtonView!!.setOnClickListener({
            if (mRecogAlive) {
                mSpeechRecognizer!!.stopListening()
                return@setOnClickListener
            }
            Timber.d("Click mFloatingButton.")
            mRecogAlive = true
            mSpeechRecognizer!!.startListening(mRecognizerIntent)

            mFloatingTextView!!.setText(R.string.wait_for_speech)
            addFloatingView(mFloatingTextFrameLayout, mFloatingLayoutParams)
        })
        mTagsFrameLayout = FrameLayout(mContext!!)
    }

    fun addTextView2Ndoe(nodes : MutableList<AccessibilityNodeInfo>) {
        removeFloatingView(mTagsFrameLayout)
        mTagsFrameLayout = FrameLayout(mContext!!)

        for (i in 0..(nodes.size-1)) {
            var rect = Rect()
            nodes.get(i).getBoundsInScreen(rect)
            Timber.d("addTextView2Ndoe " + i
                    + "--id:" + nodes.get(i).viewIdResourceName
                    + ", text:" + nodes.get(i).text
                    + ", drawingOrder: " + nodes.get(i).drawingOrder
                    + ", viewIdResourceName: " + nodes.get(i).viewIdResourceName
                    + ", actionList: " + nodes.get(i).actionList
                    + ", (" + rect.left + "," + rect.top + ")")

            var tv = TextView(mContext)
            tv.setText(""+i)
            var lparams = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

            lparams.leftMargin = rect.left
            lparams.topMargin = rect.top
            tv.setLayoutParams(lparams)
            tv.setBackgroundColor(Color.argb(0.8f, 0.0f, 0.0f, 0.0f))
            tv.setTextColor(Color.WHITE)
            tv.setPadding(2, 2, 2, 2)


            mTagsFrameLayout!!.addView(tv)
        }
        addFloatingView(mTagsFrameLayout, mTagLayoutParams)
    }

    private fun setUpFloatingLayoutParams() {
        //Speech text
        mFloatingLayoutParams = LayoutParams()
        mFloatingLayoutParams!!.type = LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        mFloatingLayoutParams!!.format = PixelFormat.TRANSLUCENT
        mFloatingLayoutParams!!.flags = (LayoutParams.FLAG_NOT_FOCUSABLE
                or LayoutParams.FLAG_NOT_TOUCH_MODAL
                or LayoutParams.FLAG_KEEP_SCREEN_ON
                or LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        mFloatingLayoutParams!!.width = LayoutParams.WRAP_CONTENT
        mFloatingLayoutParams!!.height = LayoutParams.WRAP_CONTENT
        mFloatingLayoutParams!!.gravity = Gravity.CENTER_VERTICAL or Gravity.BOTTOM

        //Button
        mButtonLayoutParams = LayoutParams()
        mButtonLayoutParams!!.type = LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        mButtonLayoutParams!!.format = PixelFormat.TRANSLUCENT
        mButtonLayoutParams!!.flags = (LayoutParams.FLAG_NOT_FOCUSABLE
                or LayoutParams.FLAG_NOT_TOUCH_MODAL
                or LayoutParams.FLAG_KEEP_SCREEN_ON
                or LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        mButtonLayoutParams!!.width = LayoutParams.WRAP_CONTENT
        mButtonLayoutParams!!.height = LayoutParams.WRAP_CONTENT
        mButtonLayoutParams!!.gravity = Gravity.CENTER_HORIZONTAL or Gravity.RIGHT

        //Tag
        mTagLayoutParams = LayoutParams()
        mTagLayoutParams!!.type = LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        mTagLayoutParams!!.format = PixelFormat.TRANSLUCENT
        mTagLayoutParams!!.flags = (LayoutParams.FLAG_NOT_FOCUSABLE
                or LayoutParams.FLAG_NOT_TOUCH_MODAL
                or LayoutParams.FLAG_NOT_TOUCHABLE
                or LayoutParams.FLAG_KEEP_SCREEN_ON
                or LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        mTagLayoutParams!!.width = LayoutParams.MATCH_PARENT
        mTagLayoutParams!!.height = LayoutParams.MATCH_PARENT

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

    private fun removeFloatingView(frameLayout: FrameLayout?) {
        Timber.d("removeFloatingView.")
        try {
            var wm: WindowManager = mContext!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.removeViewImmediate(frameLayout)
        } catch (e: Exception) {
            Timber.d("remove window view err: " + e.message)
        }
    }

    fun stopRecognition() {
        Timber.d("stopRecognition")
        mRecogAlive = false
        removeFloatingView(mFloatingTextFrameLayout!!)
    }

    fun reStartRecognition() {
        Timber.d("reStartRecognition")
        mFloatingTextView!!.setText("Speech...")
        mSpeechRecognizer!!.startListening(mRecognizerIntent)
    }

    fun setSpeechText(text: String) {
        mFloatingTextView!!.setText(text)
        mHandler!!.postDelayed(FloatView.mStopRecogRunnable, FloatView.mDelay)
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
        Timber.d("Error: $p0, $message")
        mFloatingTextView!!.setText(message)
        mHandler!!.postDelayed(mStopRecogRunnable, mDelay)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onResults(p0: Bundle?) {
        var resList: ArrayList<String>?
                = p0!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var text = ""
        for (result in resList!!) {
            text += result + ","
        }
        Timber.d("onResults: " + text)
        BaseAccessibilityService.getInstance().matchActionString(text)

        text = resList.get(0)
        mFloatingTextView!!.setText(text)
    }

    companion object {
        private var mContext: Context ?= null
        private var mInstance: FloatView ?= null
        var mHandler: Handler ?= null

        private var mSpeechRecognizer: SpeechRecognizer ?= null
        private var mRecognizerIntent: Intent ?= null

        //Speech text
        private var mFloatingTextFrameLayout: FrameLayout ?= null
        private var mFloatingTextView: TextView ?= null
        private var mFloatingLayoutParams: LayoutParams ?=null
        //Button
        private var mButtonFrameLayout: FrameLayout ?= null
        private var mButtonView: Button?= null
        private var mButtonLayoutParams: LayoutParams ?=null
        //Tag
        private var mTagsFrameLayout: FrameLayout ?= null
        private var mTagLayoutParams: LayoutParams ?=null

        private var mListenRunnable: Runnable ?= null
        var mStopRecogRunnable: Runnable ?= null
        var mDelay: Long = 1500
        private var mRecogAlive: Boolean = false

        fun getInstance(context: Context): FloatView {
            if (mInstance == null) {
                mInstance = FloatView(context)
            }
            return  mInstance!!
        }
    }
}