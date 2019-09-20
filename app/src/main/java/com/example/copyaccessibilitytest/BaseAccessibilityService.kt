package com.example.copyaccessibilitytest

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import timber.log.Timber

class BaseAccessibilityService : AccessibilityService() {
    private var mNodeList: MutableList<AccessibilityNodeInfo> = mutableListOf()

    override fun onServiceConnected() {
        Timber.i("onServiceConnected")
    }

    override fun onInterrupt() {
        Timber.i("onInterrupt")
        FloatView.getInstance().stopRecognition()
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        Timber.i("onAccessibilityEvent: " + p0!!.eventType)

        if (p0!!.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            || p0!!.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            Timber.i("onAccessibilityEvent: " + p0!!.eventType)

            if (rootInActiveWindow != null
                && rootInActiveWindow.childCount > 0) {
                mNodeList = mutableListOf()
                getChildNode(rootInActiveWindow)
            } else {
                Timber.d("rootInActiveWindow == null")
            }
        }

    }

    fun getChildNode(node: AccessibilityNodeInfo) {
        for (i in 0 until node.childCount ) {
            var childNode = node.getChild(i)
            if (childNode == null)  continue
            if (childNode.text != null) {
                mNodeList.add(childNode)

                Timber.d("nodeText: " + childNode.text
                        + ", pkgName: " + childNode.packageName
                        + ", isClickable: " + childNode.isCheckable
                        + ", childCnt: " + childNode.childCount)
            }
            if (childNode.childCount != 0) {
                getChildNode(childNode)
            }
        }

    }

    fun performAction(text: String) {
        var node = findViewByText(text.toLowerCase())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if(node != null) {
                with(text.toLowerCase()) {
                    when {
                        contains("click") -> {
                            Timber.d("click")
                            node!!.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }
                        else ->
                            Timber.d("else action")
                    }
                }
            } else {
                with(text.toLowerCase()) {
                    when {
                        contains("back") -> {
                            Timber.d("back")
                            performGlobalAction(GLOBAL_ACTION_BACK)
                        }
                        contains("home") -> {
                            Timber.d("home")
                            performGlobalAction(GLOBAL_ACTION_HOME)
                        }
                        else ->
                            Timber.d("else action")
                    }
                }
            }
        } else {
            Timber.d("VERSION.SDK_INT < JELLY_BEAN")
        }
    }

    fun findViewByText(text: String): AccessibilityNodeInfo? {
        Timber.d("findViewByText")
        if (rootInActiveWindow == null) {
            Timber.d("rootInActiveWindow == null")
            return null
        }
        getChildNode(rootInActiveWindow)
        Timber.d("mNodeList.size: " + mNodeList.size)
        for (node in mNodeList) {
            if (text.contains(node.text.toString().toLowerCase())) {
                return node
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
