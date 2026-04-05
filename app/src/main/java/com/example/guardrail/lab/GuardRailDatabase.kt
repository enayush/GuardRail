package com.example.guardrail.lab

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DetectionLog::class, SurveyResponse::class],
    version = 3,
    exportSchema = false
)
abstract class GuardRailDatabase : RoomDatabase() {
    abstract fun detectionDao(): DetectionDao
    abstract fun surveyDao(): SurveyDao
}
