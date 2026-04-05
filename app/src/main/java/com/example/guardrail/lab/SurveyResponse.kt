package com.example.guardrail.lab

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Research-grade Survey Entity based on HCI constructs.
 * All quantitative questions use a 5-point Likert Scale (1 = Strongly Disagree, 5 = Strongly Agree).
 */
@Entity(tableName = "survey_responses")
data class SurveyResponse(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,

    // Construct 1: Awareness & Learning
    val q_awareness: Int,
    val q_learning: Int,

    // Construct 2: Behavioral Impact
    val q_hesitation: Int,
    val q_avoidance: Int,

    // Construct 3: System Interference & Usability (Reverse-coded)
    val q_interference: Int,
    val q_false_positives: Int,

    // Construct 4: Trust & Adoption (TAM)
    val q_trust: Int,
    val q_retention: Int,

    // Qualitative Data
    val critical_incident_text: String?,
    val general_feedback: String?,

    val timestamp: Long = System.currentTimeMillis()
)