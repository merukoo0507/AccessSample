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

class BaseAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        Timber.i("onServiceConnected")
        mInstance = this
        FloatView.getInstance(this)
    }

    override fun onInterrupt() {
        Timber.i("onInterrupt")
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
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
            mNodeList = mutableListOf<AccessibilityNodeInfo>()
            getChildNode(rNode!!)

            if (rootInActiveWindow != null
                && mWindowId != rootInActiveWindow.windowId) {
                Timber.d("mWindowId: " + rootInActiveWindow.windowId)

                mWindowId = rootInActiveWindow.windowId
                FloatView.getInstance(this).addTextView2Ndoe(mNodeList)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getChildNode(node: AccessibilityNodeInfo) {
        for (i in 0 until node.childCount ) {
            var childNode = node.getChild(i)
            if (childNode == null)  return
            if (childNode.isClickable) {    //(childNode.text != null) {
                Timber.d("nodeText: " + childNode.text
                        + ", isClickable: " + childNode.isClickable
                        + ", pkgName: " + childNode.packageName)
                mNodeList.add(childNode)
//                FloatView.getInstance(this).addTextView2Ndoe(childNode)
            }
            if (childNode.childCount != 0) {
//                Timber.d("childCnt: " + childNode.childCount)
                getChildNode(childNode)
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun performAction(text: String) {
        Timber.d("pf: " + text)

        with(text.toLowerCase()) {
            when {
                contains("click") -> {
                    clickAction(text)
                }
                contains("back") || contains("返回") -> {
                    Timber.d("pf: back")
                    performGlobalAction(GLOBAL_ACTION_BACK)
                }
                contains("home") || contains("首頁") -> {
                    Timber.d("pf: home")
                    performGlobalAction(GLOBAL_ACTION_HOME)
                }
                contains("backward") || contains("下滑") -> {
                    Timber.d("pf: backward")
                    performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
                }
                contains("forward") || contains("上滑") -> {
                    Timber.d("pf: forward")
                    performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                }
                else -> {
                    Timber.d("else action")
                    clickAction(text)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun clickAction(text: String) {
        val strs = text.split(",").toTypedArray()
        if (rNode == null) {
            Timber.d("(rNode == null")
            return
        }
        for (str in strs) {
            if (str.toLowerCase().equals("click"))  continue
            Timber.d("pf: str = " + str)
            var nodeList = rNode!!.findAccessibilityNodeInfosByText(str)
            for(node in nodeList) {
                Timber.d("pf: ACTION_CLICK")
                if (node.isClickable) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                } else{
                    Timber.d("order: " + node.drawingOrder)
                    if (node.parent.isClickable)
                        node.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun findViewByText(text: String): AccessibilityNodeInfo? {
        val rNode = this.rootInActiveWindow

        val nodeInfoList = rNode.findAccessibilityNodeInfosByText(text)
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (nodeInfo in nodeInfoList) {
                if (nodeInfo != null) { // && nodeInfo.isClickable == clickable) {
                    return nodeInfo
                }
            }
        }
        return null
    }

    companion object {
        private var mInstance: BaseAccessibilityService ?= null
        var rNode: AccessibilityNodeInfo ?= null
        var mNodeList: MutableList<AccessibilityNodeInfo> = mutableListOf<AccessibilityNodeInfo>()
        var mWindowId: Int = 0

        fun getInstance(): BaseAccessibilityService {
            if (mInstance == null) {
                Timber.d("BaseAccessibilityService.getInstance() == null")
                throw Exception("BaseAccessibilityService.getInstance() == null")
            }
            return  mInstance!!
        }
    }
}
