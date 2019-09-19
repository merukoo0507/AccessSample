package com.example.copyaccessibilitytest

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import timber.log.Timber

class BaseAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        Timber.i("onServiceConnected")
    }

    override fun onInterrupt() {
        Timber.i("onInterrupt")
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        Timber.i("onAccessibilityEvent: " + p0!!.eventType)

        if (p0!!.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            Timber.d("TYPE_WINDOW_CONTENT_CHANGED")
            val node: AccessibilityNodeInfo ?= if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                rootInActiveWindow
            } else {
                Timber.d("VERSION.SDK_INT < JELLY_BEAN")
                return
            }

            if (node == null) {
                Timber.d("rootNode == null")
                return
            }
            getChildNode(node!!)
        }

    }

    fun getChildNode(node: AccessibilityNodeInfo) {
        for (i in 0 until node.childCount ) {
            var childNode = node.getChild(i)
            if (childNode == null)  return
            if (childNode.text != null) {
                Timber.d("nodeText: " + childNode.text
                        + ", pkgName: " + childNode.packageName
                        + ", isClickable: " + childNode.isCheckable)
            }
            if (childNode.childCount != 0) {
                Timber.d("childCnt: " + childNode.childCount)
                getChildNode(childNode)
            }
        }

    }

    fun performAction(node: AccessibilityNodeInfo?, actionText: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            with(actionText.toLowerCase()) {
                when {
                    contains("back") -> {
                        Timber.d("back")
                        performGlobalAction(GLOBAL_ACTION_BACK)
                    }
                    contains("home") -> {
                        performGlobalAction(GLOBAL_ACTION_BACK)
                    }
                    contains("click") -> {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }

                    else ->
                        Timber.d("else action")

                }
            }
        } else {
            Timber.d("VERSION.SDK_INT < JELLY_BEAN")
        }
    }

    fun findViewByText(text: String, clickable: Boolean): AccessibilityNodeInfo? {
        val accessibilityNodeInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            rootInActiveWindow ?: return null
        } else  return null

        val nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text)
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (nodeInfo in nodeInfoList) {
                if (nodeInfo != null && nodeInfo.isClickable == clickable) {
                    return nodeInfo
                }
            }
        }
        return null
    }

    companion object {
        private var mContext: Context ?= null
        private var mInstance: BaseAccessibilityService ?= null


        fun init(context: Context) {
            mContext = context
        }

        fun getInstance(): BaseAccessibilityService {
            if (mInstance == null) {
                mInstance = BaseAccessibilityService()
            }
            return  mInstance!!
        }
    }
}
