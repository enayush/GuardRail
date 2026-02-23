package com.example.guardrail.analysis

import com.example.guardrail.lab.DetectionLog

object FrequencyAnalyzer {

    fun detectionsPerApp(logs: List<DetectionLog>): Map<String, Int> {
        return logs.groupBy { it.packageName }
            .mapValues { it.value.size }
    }

    fun detectionsPerSession(logs: List<DetectionLog>): Map<String, Int> {
        return logs.groupBy { it.userId }
            .mapValues { it.value.size }
    }
}

