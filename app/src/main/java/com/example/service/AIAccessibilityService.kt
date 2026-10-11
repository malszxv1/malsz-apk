package com.example.service

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.lang.ref.WeakReference

class AIAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = WeakReference(this)
        Log.i(TAG, "Malsz AIAccessibilityService connected successfully")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val source = event.source ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_FOCUSED,
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                if (source.isEditable) {
                    lastFocusedNode = WeakReference(source)
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Malsz AIAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        lastFocusedNode = null
    }

    companion object {
        private const val TAG = "AIAccessibilityService"
        private var instance: WeakReference<AIAccessibilityService>? = null
        private var lastFocusedNode: WeakReference<AccessibilityNodeInfo>? = null

        fun isRunning(): Boolean = instance?.get() != null

        fun getFocusedFieldHint(): String? {
            val node = lastFocusedNode?.get() ?: return null
            val hint = node.hintText?.toString()
            val text = node.text?.toString()
            val contentDesc = node.contentDescription?.toString()
            return hint ?: contentDesc ?: text
        }

        fun insertTextIntoFocusedNode(text: String): Boolean {
            val node = lastFocusedNode?.get()
            if (node == null) {
                Log.w(TAG, "No editable field currently focused")
                return false
            }

            // Safety rule: Never tamper with password fields
            if (node.isPassword) {
                Log.w(TAG, "Target field is a password field. Skipping auto-fill for safety.")
                return false
            }

            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
    }
}
