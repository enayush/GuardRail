package com.example.guardrail.evaluation

/**
 * Pure data holder for analysis summary.
 * Contains aggregated statistics for survey and export purposes.
 */
data class AnalysisSummary(
    val totalDetections: Int,
    val detectionsPerApp: Map<String, Int>,
    val detectionsPerSession: Map<String, Int>,
    val meanLatencyMs: Double,
    val maxLatencyMs: Long,
    val latencyBuckets: Map<String, Int>,
    val recurringTexts: Map<String, Int>
)

