package com.example.guardrail.lab

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object LabWriter {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun write(context: Context, log: DetectionLog) {
        scope.launch {
            try {
                val dao = LabDatabaseProvider.get(context).detectionDao()
                dao.insertLog(log)
            } catch (e: Exception) {
                // Silently catch all exceptions to prevent app crashes
            }
        }
    }
}

