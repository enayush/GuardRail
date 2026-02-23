package com.example.guardrail.lab

import android.content.Context

fun buildDetectionLog(
    context: Context,
    packageName: String,
    patternType: String,
    detectedText: String,
    latencyMs: Long
): DetectionLog {
    val userId = UserSession.getUserId(context)
    return DetectionLog(
        userId = userId,
        packageName = packageName,
        patternType = patternType,
        detectedText = detectedText,
        latencyMs = latencyMs,
        timestamp = System.currentTimeMillis()
    )
}

