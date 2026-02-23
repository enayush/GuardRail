package com.example.guardrail.lab

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detection_logs")
data class DetectionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val packageName: String,
    val patternType: String,
    val detectedText: String,
    val latencyMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

