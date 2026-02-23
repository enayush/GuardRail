package com.example.guardrail.analysis

import com.example.guardrail.lab.DetectionLog

object TextPatternAnalyzer {

    fun recurringTexts(logs: List<DetectionLog>): Map<String, Int> {
        return logs.groupBy { it.detectedText }
            .mapValues { it.value.size }
    }
}

