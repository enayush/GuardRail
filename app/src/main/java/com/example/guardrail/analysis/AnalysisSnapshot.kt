package com.example.guardrail.analysis

import com.example.guardrail.lab.DetectionLog

data class AnalysisSnapshot(
    val logs: List<DetectionLog>,
    val totalDetections: Int,
    val uniqueApps: Int,
    val uniqueSessions: Int
)

