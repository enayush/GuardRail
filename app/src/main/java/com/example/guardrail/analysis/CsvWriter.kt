package com.example.guardrail.analysis

import android.content.Context
import java.io.File

fun writeCsvToFile(context: Context, csv: String): File? {
    return try {
        val directory = context.getExternalFilesDir(null)
        val file = File(directory, "guardrail_detections.csv")
        file.writeText(csv)
        file
    } catch (e: Exception) {
        null
    }
}

