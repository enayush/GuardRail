package com.example.guardrail.lab

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SurveyDao {
    @Insert
    suspend fun insert(response: SurveyResponse)

    @Query("SELECT * FROM survey_responses")
    suspend fun getAll(): List<SurveyResponse>
}

