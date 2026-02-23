package com.example.guardrail.engine

import android.graphics.Rect

/**
 * Represents a text node candidate for dark pattern analysis.
 *
 * @param text The visible text content
 * @param bounds The screen bounds of the text node
 * @param nodeHash A hash identifying the node (for deduplication)
 */
data class DarkTextCandidate(
    val text: String,
    val bounds: Rect,
    val nodeHash: Int
)

