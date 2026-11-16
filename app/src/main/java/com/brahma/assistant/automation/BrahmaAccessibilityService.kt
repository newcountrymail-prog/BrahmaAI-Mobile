package com.brahma.assistant.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class BrahmaAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "BrahmaAccessibility"

        @Volatile
        var instance: BrahmaAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "Brahma Autonomous Screen Automation Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Can monitor foreground package transitions or UI changes
    }

    override fun onInterrupt() {
        Log.w(TAG, "Brahma Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    /**
     * Traverses the active window and clicks on the node matching targetText.
     */
    fun findAndClickText(targetText: String, fuzzyMatch: Boolean = true): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(targetText)

        if (nodes.isNullOrEmpty()) {
            return traverseAndClick(root, targetText, fuzzyMatch)
        }

        for (node in nodes) {
            if (performClickOnNode(node)) {
                return true
            }
        }

        return false
    }

    private fun traverseAndClick(node: AccessibilityNodeInfo?, target: String, fuzzy: Boolean): Boolean {
        if (node == null) return false

        val text = node.text?.toString().orEmpty()
        val desc = node.contentDescription?.toString().orEmpty()

        val isMatch = if (fuzzy) {
            text.contains(target, ignoreCase = true) || desc.contains(target, ignoreCase = true)
        } else {
            text.equals(target, ignoreCase = true) || desc.equals(target, ignoreCase = true)
        }

        if (isMatch && performClickOnNode(node)) {
            return true
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (traverseAndClick(child, target, fuzzy)) {
                return true
            }
        }

        return false
    }

    private fun performClickOnNode(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable) {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        var parent = node.parent
        while (parent != null) {
            if (parent.isClickable) {
                return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            parent = parent.parent
        }
        return false
    }

    /**
     * Performs a programmatic swipe gesture on screen.
     */
    fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 300L): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(startX, startY)
                lineTo(endX, endY)
            }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
                .build()

            return dispatchGesture(gesture, null, null)
        }
        return false
    }

    fun triggerGlobalBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)
    fun triggerGlobalHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)
    fun triggerGlobalRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)
}
