package com.example.guardrail.engine

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.os.Handler
import android.os.Looper

/**
 * OverlayEngine is responsible for drawing red border overlays on screen
 * for confirmed dark pattern candidates.
 *
 * Responsibilities:
 * - Draw red border overlays using candidate.bounds
 * - Support drawing multiple overlays at once
 * - Track overlays internally for cleanup
 *
 * Does NOT:
 * - Traverse nodes
 * - Score text
 * - Compute signatures
 */
class OverlayEngine(private val context: Context) {

    companion object {
        private const val TAG = "OverlayEngine"
        private const val BORDER_WIDTH = 8 // Border width in pixels
        private const val BORDER_COLOR = Color.RED
        private const val BORDER_ALPHA = 200 // 0-255, where 255 is fully opaque
        private const val AUTO_CLEAR_DELAY = 3000L // Fallback auto-clear after 3 seconds
    }

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    // Track all active overlay views for cleanup
    private val activeOverlays = mutableListOf<View>()

    // Handler for auto-clear timer (fallback safety net)
    private val handler = Handler(Looper.getMainLooper())
    private var autoClearRunnable: Runnable? = null

    /**
     * Draw overlays for a list of confirmed dark pattern candidates.
     * Clears existing overlays before drawing new ones.
     * All overlays are drawn in one batch.
     *
     * @param candidates List of DarkTextCandidate objects to draw overlays for
     */
    fun drawOverlays(candidates: List<DarkTextCandidate>) {
        // Clear existing overlays first
        clearOverlays()

        if (candidates.isEmpty()) {
            Log.d(TAG, "No candidates to draw overlays for")
            return
        }

        Log.d(TAG, "Drawing ${candidates.size} overlays in batch")

        // Draw an overlay for each candidate (batch processing)
        candidates.forEach { candidate ->
            drawOverlayForCandidate(candidate)
        }

        Log.d(TAG, "Successfully drew ${activeOverlays.size} overlays on screen")

        // Schedule fallback auto-clear as safety net
        scheduleAutoClear()
    }

    /**
     * Draw a single overlay for a candidate using its bounds.
     *
     * @param candidate The DarkTextCandidate to draw an overlay for
     */
    private fun drawOverlayForCandidate(candidate: DarkTextCandidate) {
        try {
            val bounds = candidate.bounds

            // Validate bounds
            if (bounds.width() <= 0 || bounds.height() <= 0) {
                Log.w(TAG, "Invalid bounds for candidate: $bounds")
                return
            }

            // Create overlay view with red border
            val overlayView = FrameLayout(context).apply {
                // Set border using a background drawable
                setBackgroundColor(Color.TRANSPARENT)
                // Draw border by setting padding and background
                val borderDrawable = android.graphics.drawable.GradientDrawable().apply {
                    setStroke(BORDER_WIDTH, BORDER_COLOR)
                    setColor(Color.TRANSPARENT)
                    alpha = BORDER_ALPHA
                }
                background = borderDrawable
            }

            // Configure window layout parameters
            val params = WindowManager.LayoutParams(
                bounds.width(),
                bounds.height(),
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = bounds.left
                y = bounds.top
            }

            // Add overlay to window manager
            windowManager.addView(overlayView, params)

            // Track overlay for cleanup
            activeOverlays.add(overlayView)

            Log.v(TAG, "Drew overlay at bounds: $bounds for text: \"${candidate.text}\"")

        } catch (e: Exception) {
            Log.e(TAG, "Error drawing overlay for candidate: ${e.message}", e)
        }
    }

    /**
     * Clear all active overlays from the screen.
     */
    fun clearOverlays() {
        // Cancel any pending auto-clear timer
        cancelAutoClear()

        if (activeOverlays.isEmpty()) {
            return
        }

        Log.d(TAG, "Clearing ${activeOverlays.size} overlays")

        activeOverlays.forEach { overlay ->
            try {
                windowManager.removeView(overlay)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay: ${e.message}", e)
            }
        }

        activeOverlays.clear()
    }

    /**
     * Schedule fallback auto-clear timer (safety net).
     * Overlays will automatically clear after AUTO_CLEAR_DELAY milliseconds.
     */
    private fun scheduleAutoClear() {
        // Cancel any existing timer first
        cancelAutoClear()

        autoClearRunnable = Runnable {
            Log.d(TAG, "Auto-clear timer triggered (fallback safety net)")
            clearOverlays()
        }

        handler.postDelayed(autoClearRunnable!!, AUTO_CLEAR_DELAY)
        Log.v(TAG, "Scheduled auto-clear in ${AUTO_CLEAR_DELAY}ms")
    }

    /**
     * Cancel the auto-clear timer if it's pending.
     */
    private fun cancelAutoClear() {
        autoClearRunnable?.let {
            handler.removeCallbacks(it)
            autoClearRunnable = null
            Log.v(TAG, "Cancelled auto-clear timer")
        }
    }

    /**
     * Destroy the overlay engine and clean up all resources.
     * Should be called when the service is destroyed.
     */
    fun destroy() {
        Log.d(TAG, "Destroying OverlayEngine")
        cancelAutoClear()
        clearOverlays()
    }

    /**
     * Legacy method for backward compatibility.
     * Draws an overlay for a single text node.
     *
     * @deprecated Use drawOverlays(List<DarkTextCandidate>) instead
     */
    @Deprecated("Use drawOverlays with DarkTextCandidate list")
    fun addOverlayForTextNode(
        node: android.view.accessibility.AccessibilityNodeInfo,
        text: String,
        rootNode: android.view.accessibility.AccessibilityNodeInfo
    ) {
        try {
            val bounds = android.graphics.Rect()
            node.getBoundsInScreen(bounds)

            val candidate = DarkTextCandidate(text, bounds, text.hashCode())
            drawOverlayForCandidate(candidate)

        } catch (e: Exception) {
            Log.e(TAG, "Error in legacy addOverlayForTextNode: ${e.message}", e)
        }
    }
}








