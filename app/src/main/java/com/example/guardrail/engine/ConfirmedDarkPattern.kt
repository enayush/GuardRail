package com.example.guardrail.engine

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Represents a confirmed dark pattern after scoring.
 *
 * @param candidate The original DarkTextCandidate
 * @param node The AccessibilityNodeInfo for overlay drawing
 * @param score The classification score from the model
 */
data class ConfirmedDarkPattern(
    val candidate: DarkTextCandidate,
    val node: AccessibilityNodeInfo,
    val score: Double
)

