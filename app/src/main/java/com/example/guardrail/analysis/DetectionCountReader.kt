package com.example.guardrail.analysis

import android.content.Context
import com.example.guardrail.lab.LabDatabaseProvider

class DetectionCountReader {

    suspend fun getTotalDetections(context: Context): Int {
        val database = LabDatabaseProvider.get(context)
        val dao = database.detectionDao()
        return dao.getDetectionCount()
    }
}

