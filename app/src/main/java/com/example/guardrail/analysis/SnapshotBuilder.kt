package com.example.guardrail.analysis

import com.example.guardrail.lab.DetectionLog

object SnapshotBuilder {

    suspend fun build(logs: List<DetectionLog>): AnalysisSnapshot {
        val totalDetections = logs.size
        val uniqueApps = logs.map { it.packageName }.distinct().size
        val uniqueSessions = logs.map { it.userId }.distinct().size

        return AnalysisSnapshot(
            logs = logs,
            totalDetections = totalDetections,
            uniqueApps = uniqueApps,
            uniqueSessions = uniqueSessions
        )
    }
}

