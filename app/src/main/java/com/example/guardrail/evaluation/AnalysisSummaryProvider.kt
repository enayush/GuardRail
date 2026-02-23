package com.example.guardrail.evaluation

import android.content.Context
import com.example.guardrail.analysis.DetectionLogReader
import com.example.guardrail.analysis.FrequencyAnalyzer
import com.example.guardrail.analysis.LatencyAnalyzer
import com.example.guardrail.analysis.SnapshotBuilder
import com.example.guardrail.analysis.TextPatternAnalyzer

/**
 * Provides analysis summary by coordinating existing analyzers.
 * No DB writes, no caching - deterministic output only.
 */
class AnalysisSummaryProvider {

    suspend fun loadSummary(context: Context): AnalysisSummary {
        // Read logs
        val logReader = DetectionLogReader(context)
        val logs = logReader.getAllLogs()

        // Build snapshot
        val snapshot = SnapshotBuilder.build(logs)

        // Call existing analyzers
        val detectionsPerApp = FrequencyAnalyzer.detectionsPerApp(logs)
        val detectionsPerSession = FrequencyAnalyzer.detectionsPerSession(logs)
        val meanLatencyMs = LatencyAnalyzer.averageLatency(logs)
        val maxLatencyMs = LatencyAnalyzer.maxLatency(logs)
        val latencyBuckets = LatencyAnalyzer.latencyBuckets(logs)
        val recurringTexts = TextPatternAnalyzer.recurringTexts(logs)

        // Build and return summary
        return AnalysisSummary(
            totalDetections = snapshot.totalDetections,
            detectionsPerApp = detectionsPerApp,
            detectionsPerSession = detectionsPerSession,
            meanLatencyMs = meanLatencyMs,
            maxLatencyMs = maxLatencyMs,
            latencyBuckets = latencyBuckets,
            recurringTexts = recurringTexts
        )
    }
}

