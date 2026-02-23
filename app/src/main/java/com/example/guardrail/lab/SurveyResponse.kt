package com.example.guardrail.lab

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for survey responses.
 * Append-only, no relations, no foreign keys.
 */
@Entity(tableName = "survey_responses")
data class SurveyResponse(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val q_helpfulness: Int, // 1–5
    val q_intrusiveness: Int, // 1–5
    val q_changed_decision: Boolean,
    val q_trust: Int, // 1–5
    val freeText: String?,
    val timestamp: Long = System.currentTimeMillis()
)

