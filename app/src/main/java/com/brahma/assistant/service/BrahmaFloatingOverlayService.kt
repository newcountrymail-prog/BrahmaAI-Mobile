package com.brahma.assistant.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.brahma.assistant.BrahmaApplication
import com.brahma.assistant.MainActivity

class BrahmaFloatingOverlayService : Service() {

    companion object {
        private const val TAG = "BrahmaOverlayService"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, BrahmaFloatingOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BrahmaFloatingOverlayService::class.java))
        }
    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        setupFloatingOrb()
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, BrahmaApplication.CHANNEL_ID_OVERLAY)
            .setContentTitle("Brahma AI HUD Active")
            .setContentText("Persistent low-latency floating orb is online.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun setupFloatingOrb() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            160,
            160,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 200
        }

        val rootContainer = FrameLayout(this).apply {
            val icon = ImageView(context).apply {
                setImageResource(android.R.drawable.ic_dialog_info)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
            addView(icon)
        }

        rootContainer.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isClick = false

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                val motionEvent = event ?: return false
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = motionEvent.rawX
                        initialTouchY = motionEvent.rawY
                        isClick = true
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (motionEvent.rawX - initialTouchX).toInt()
                        val dy = (motionEvent.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isClick = false
                        }
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager?.updateViewLayout(rootContainer, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isClick) {
                            // Tap opens main dashboard
                            val launchIntent = Intent(this@BrahmaFloatingOverlayService, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            }
                            startActivity(launchIntent)
                        }
                        return true
                    }
                }
                return false
            }
        })

        try {
            windowManager?.addView(rootContainer, params)
            overlayView = rootContainer
            Log.i(TAG, "Floating Orb added to WindowManager.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add floating overlay", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
    }
}
