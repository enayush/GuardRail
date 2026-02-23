package com.example.guardrail.analysis

import android.content.Context
import com.example.guardrail.lab.DetectionLog
import com.example.guardrail.lab.LabDatabaseProvider
import kotlinx.coroutines.flow.first

class DetectionLogReader(private val context: Context) {

    suspend fun getAllLogs(): List<DetectionLog> {
        val dao = LabDatabaseProvider.get(context).detectionDao()
        return dao.getAllLogs().first()
    }
}

