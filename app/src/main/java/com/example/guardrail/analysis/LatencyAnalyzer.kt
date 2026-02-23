package com.example.guardrail.analysis

import com.example.guardrail.lab.DetectionLog

object LatencyAnalyzer {

    fun averageLatency(logs: List<DetectionLog>): Double {
        if (logs.isEmpty()) return 0.0
        return logs.map { it.latencyMs }.average()
    }

    fun maxLatency(logs: List<DetectionLog>): Long {
        if (logs.isEmpty()) return 0L
        return logs.maxOf { it.latencyMs }
    }

    fun latencyBuckets(logs: List<DetectionLog>): Map<String, Int> {
        val buckets = mutableMapOf(
            "<10ms" to 0,
            "10–25ms" to 0,
            "25–50ms" to 0,
            ">50ms" to 0
        )

        logs.forEach { log ->
            when {
                log.latencyMs < 10 -> buckets["<10ms"] = buckets["<10ms"]!! + 1
                log.latencyMs < 25 -> buckets["10–25ms"] = buckets["10–25ms"]!! + 1
                log.latencyMs < 50 -> buckets["25–50ms"] = buckets["25–50ms"]!! + 1
                else -> buckets[">50ms"] = buckets[">50ms"]!! + 1
            }
        }

        return buckets
    }
}

