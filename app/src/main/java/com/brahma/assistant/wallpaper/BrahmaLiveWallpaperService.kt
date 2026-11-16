package com.brahma.assistant.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import kotlin.math.sin

class BrahmaLiveWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return BrahmaQuantumCoreEngine()
    }

    inner class BrahmaQuantumCoreEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private var phase = 0f

        private val backgroundPaint = Paint().apply {
            color = Color.parseColor("#050811")
            style = Paint.Style.FILL
        }

        private val orbCorePaint = Paint().apply {
            color = Color.parseColor("#00E5FF")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        private val ringPaint = Paint().apply {
            color = Color.parseColor("#7C4DFF")
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }

        private val textPaint = Paint().apply {
            color = Color.parseColor("#80FFFFFF")
            textSize = 36f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        private val drawRunnable = Runnable { drawFrame() }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                handler.post(drawRunnable)
            } else {
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunnable)
        }

        override fun onTouchEvent(event: MotionEvent?) {
            super.onTouchEvent(event)
            if (event?.action == MotionEvent.ACTION_DOWN) {
                // Interactive ripple pulse upon touch
                phase += 1.5f
            }
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    val width = canvas.width.toFloat()
                    val height = canvas.height.toFloat()
                    val centerX = width / 2f
                    val centerY = height / 2f

                    // Draw deep dark neural void
                    canvas.drawRect(0f, 0f, width, height, backgroundPaint)

                    // Draw breathing Quantum Core Orb
                    val pulse = (sin(phase.toDouble()) * 20f).toFloat()
                    val baseRadius = 140f + pulse

                    // Outer orbit rings
                    canvas.drawCircle(centerX, centerY, baseRadius + 40f, ringPaint)
                    canvas.drawCircle(centerX, centerY, baseRadius, orbCorePaint)

                    // Subtitle status
                    canvas.drawText("BRAHMA QUANTUM CORE // ONLINE", centerX, centerY + baseRadius + 90f, textPaint)

                    phase += 0.05f
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            handler.removeCallbacks(drawRunnable)
            if (visible) {
                handler.postDelayed(drawRunnable, 33) // ~30 FPS loop
            }
        }
    }
}
