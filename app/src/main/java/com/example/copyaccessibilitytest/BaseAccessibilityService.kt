package com.example.copyaccessibilitytest

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import androidx.annotation.RequiresApi
import timber.log.Timber
import java.lang.Integer.parseInt

class BaseAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        Timber.i("onServiceConnected")
        mInstance = this
        getVoiceArrays()
        FloatView.getInstance(this)
    }

    override fun onInterrupt() {
        Timber.i("onInterrupt")
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        Timber.i("onAccessibilityEvent: " + p0!!.eventType)

        if (p0!!.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            || p0!!.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            || p0!!.eventType == AccessibilityEvent.WINDOWS_CHANGE_ADDED) {
            Timber.d("TYPE_WINDOW_CONTENT_CHANGED")
            rNode = rootInActiveWindow
            if (rNode == null) {
                Timber.d("rootNode == null")
                return
            }
            mLastNodeList = mNodeList
            mNodeList = mutableListOf()
            getChildNode(rNode!!)

            Timber.d("lnSize: " + mLastNodeList.size
                    + ", nSize: " + mNodeList.size
                    + ", windowId: " + rNode!!.windowId)
            if (mLastNodeList.size != mNodeList.size) {
                Timber.d("mWindowId: " + rNode!!.windowId)
                FloatView.getInstance(this).addTextView2Ndoe(mNodeList)
            }
        }
    }

    fun getChildNode(node: AccessibilityNodeInfo) {
        for (i in 0 until node.childCount ) {
            var childNode = node.getChild(i)
            if (childNode == null)  return
            if ((childNode.isClickable || childNode.isScrollable)
                && childNode.isVisibleToUser) {    //(childNode.text != null) {
                Timber.d("nodeText: " + childNode.text
                        + ", isClickable: " + childNode.isClickable
                        + ", pkgName: " + childNode.packageName)
                mNodeList.add(childNode)
            }
            if (childNode.childCount != 0) {
//                Timber.d("childCnt: " + childNode.childCount)
                getChildNode(childNode)
            }
        }

    }
    fun getVoiceArrays() {
        var voiceArrays = getResources().getStringArray(R.array.voiceArray)
        mStringCategory = mutableListOf()
        for (va in voiceArrays) {
            var strArray : List<String> = va.split(",")

            Timber.d("getVoiceArrays: " + strArray)
            mStringCategory!!.add(strArray)
        }
    }

    fun matchActionString(text: String) {
        val strs = text.split(",").toTypedArray()
        for (str in strs) {
            for (i in 0 until mStringCategory!!.size) {
                for (j in 0 until mStringCategory!!.get(i).size) {
                    if (str.equals(mStringCategory!!.get(i).get(j))) {
                        FloatView.getInstance(this).setSpeechText(str)
                        performAction(mStringCategory!!.get(i).get(0))
                        return
                    }
                }
            }
        }

        Timber.d("pf: else action")
        clickAction(text)
    }

    fun performAction(text: String) {
        Timber.d("performAction pf: " + text)

        with(text.toLowerCase()) {
            when {
                contains("backward") -> {
                    Timber.d("pf: backward")
                    scrollAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
                    return
                }
                contains("forward") -> {
                    Timber.d("pf: forward")
                    scrollAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    return
                }
                contains("click") -> {
                    clickAction(text)
                    return
                }
                contains("back") -> {
                    Timber.d("pf: back")
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    return
                }
                contains("home") -> {
                    Timber.d("pf: home")
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    return
                }
                else -> {
//                    Timber.d("else action")
//                    clickAction(text)
                }
            }
        }
    }

    fun scrollAction(action: Int) {
        for (node in mNodeList) {
            if (node.isScrollable) {
                node.performAction(action)
            }
        }
    }

    fun clickAction(text: String) {
        val strs = text.split(",").toTypedArray()
        if (rNode == null) {
            Timber.d("(rNode == null")
            return
        }
        for (str in strs) {
            if (str.toLowerCase().equals("click"))  continue
            Timber.d("pf: str = " + str)
            var numeric = true
            var num = -1
            try {
                num = parseInt(str)
                Timber.d("pf: num = " + num)
            } catch (e: NumberFormatException) {
                Timber.d("pf: numeric = false")
                numeric = false
            }

            if (numeric) {
                Timber.d("pf: numeric")
                mNodeList.get(num).performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return
            }

            var nodeList = rNode!!.findAccessibilityNodeInfosByText(str)
            for(node in nodeList) {
                if (node.isClickable) {
                    Timber.d("pf: ACTION_CLICK")
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                } else if (node.parent.isClickable) {
                    Timber.d("pf: parent ACTION_CLICK")
                    node.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
        FloatView.mHandler!!.postDelayed(FloatView.mStopRecogRunnable, FloatView.mDelay)
    }

    companion object {
        private var mInstance: BaseAccessibilityService ?= null
        var rNode: AccessibilityNodeInfo ?= null
        var mNodeList = mutableListOf<AccessibilityNodeInfo>()
        var mLastNodeList = mutableListOf<AccessibilityNodeInfo>()
        var mStringCategory : MutableList< List<String> > ?= null

        fun getInstance(): BaseAccessibilityService {
            if (mInstance == null) {
                Timber.d("BaseAccessibilityService.getInstance() == null")
                throw Exception("BaseAccessibilityService.getInstance() == null")
            }
            return  mInstance!!
        }
    }
}
