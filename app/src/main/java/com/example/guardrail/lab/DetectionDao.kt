package com.example.guardrail.lab

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectionDao {
    @Insert
    suspend fun insertLog(log: DetectionLog)

    @Query("SELECT * FROM detection_logs")
    fun getAllLogs(): Flow<List<DetectionLog>>

    @Query("SELECT COUNT(*) FROM detection_logs")
    suspend fun getDetectionCount(): Int
}

