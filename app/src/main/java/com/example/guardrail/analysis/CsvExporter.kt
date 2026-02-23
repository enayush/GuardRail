package com.example.guardrail.analysis

import com.example.guardrail.lab.DetectionLog

object CsvExporter {

    fun exportLogs(logs: List<DetectionLog>): String {
        val header = "id,userId,packageName,patternType,detectedText,latencyMs,timestamp"
        val rows = logs.map { log ->
            listOf(
                log.id.toString(),
                escapeField(log.userId),
                escapeField(log.packageName),
                escapeField(log.patternType),
                escapeField(log.detectedText),
                log.latencyMs.toString(),
                log.timestamp.toString()
            ).joinToString(",")
        }
        return listOf(header).plus(rows).joinToString("\n")
    }

    private fun escapeField(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }
}

