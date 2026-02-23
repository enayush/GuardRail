package com.example.guardrail.evaluation

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.guardrail.analysis.CsvExporter
import com.example.guardrail.analysis.DetectionLogReader
import com.example.guardrail.lab.LabDatabaseProvider
import com.example.guardrail.lab.SurveyResponse
import com.example.guardrail.lab.UserSession
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Builds export bundle with all detection and survey data.
 * No network, deterministic filenames, writes to external files dir.
 */
object ExportBundleBuilder {

    suspend fun buildExport(context: Context): File {
        val timestamp = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val dateString = dateFormat.format(Date(timestamp))

        // Create export directory
        val exportDir = File(context.getExternalFilesDir(null), "exports")
        exportDir.mkdirs()

        // Deterministic filename
        val zipFile = File(exportDir, "guardrail_export_$dateString.zip")

        // Load all data
        val userId = UserSession.getUserId(context)
        val logReader = DetectionLogReader(context)
        val logs = logReader.getAllLogs()
        val db = LabDatabaseProvider.get(context)
        val surveyResponses = db.surveyDao().getAll()
        val summaryProvider = AnalysisSummaryProvider()
        val summary = summaryProvider.loadSummary(context)

        // Create ZIP bundle
        ZipOutputStream(zipFile.outputStream().buffered()).use { zip ->
            // 1. detection_logs.csv
            val detectionLogsCsv = exportDetectionLogs(logs)
            zip.putNextEntry(ZipEntry("detection_logs.csv"))
            zip.write(detectionLogsCsv.toByteArray())
            zip.closeEntry()

            // 2. analysis_summary.txt
            val summaryText = exportAnalysisSummary(summary)
            zip.putNextEntry(ZipEntry("analysis_summary.txt"))
            zip.write(summaryText.toByteArray())
            zip.closeEntry()

            // 3. survey_responses.csv
            val surveyResponsesCsv = exportSurveyResponses(surveyResponses)
            zip.putNextEntry(ZipEntry("survey_responses.csv"))
            zip.write(surveyResponsesCsv.toByteArray())
            zip.closeEntry()

            // 4. metadata.txt
            val metadata = exportMetadata(userId, timestamp)
            zip.putNextEntry(ZipEntry("metadata.txt"))
            zip.write(metadata.toByteArray())
            zip.closeEntry()
        }

        return zipFile
    }

    /**
     * Share export file using Android's share sheet.
     * Lets user choose app (WhatsApp, Drive, Email, etc).
     * No auto-send, no background upload.
     */
    fun shareExport(context: Context, file: File) {
        // Create content URI using FileProvider for security
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        // Create share intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "GuardRail Export Data")
            putExtra(Intent.EXTRA_TEXT, "GuardRail detection and survey data export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Create chooser to let user pick app
        val chooserIntent = Intent.createChooser(shareIntent, "Share GuardRail Export")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // Launch chooser
        context.startActivity(chooserIntent)
    }

    private fun exportDetectionLogs(logs: List<com.example.guardrail.lab.DetectionLog>): String {
        return CsvExporter.exportLogs(logs)
    }

    private fun exportAnalysisSummary(summary: AnalysisSummary): String {
        val sb = StringBuilder()
        sb.appendLine("=== GuardRail Analysis Summary ===")
        sb.appendLine()

        sb.appendLine("## Overall Statistics")
        sb.appendLine("Total Detections: ${summary.totalDetections}")
        sb.appendLine("Mean Latency: ${"%.2f".format(summary.meanLatencyMs)} ms")
        sb.appendLine("Max Latency: ${summary.maxLatencyMs} ms")
        sb.appendLine()

        sb.appendLine("## Detections Per App")
        if (summary.detectionsPerApp.isEmpty()) {
            sb.appendLine("No data")
        } else {
            summary.detectionsPerApp.entries
                .sortedByDescending { it.value }
                .forEach { (app, count) ->
                    sb.appendLine("  $app: $count")
                }
        }
        sb.appendLine()

        sb.appendLine("## Detections Per Session")
        if (summary.detectionsPerSession.isEmpty()) {
            sb.appendLine("No data")
        } else {
            summary.detectionsPerSession.entries
                .sortedByDescending { it.value }
                .forEach { (session, count) ->
                    sb.appendLine("  ${session.take(8)}: $count")
                }
        }
        sb.appendLine()

        sb.appendLine("## Latency Distribution")
        val bucketOrder = listOf("<10ms", "10–25ms", "25–50ms", ">50ms")
        bucketOrder.forEach { bucket ->
            val count = summary.latencyBuckets[bucket] ?: 0
            sb.appendLine("  $bucket: $count")
        }
        sb.appendLine()

        sb.appendLine("## Top Recurring Texts")
        if (summary.recurringTexts.isEmpty()) {
            sb.appendLine("No data")
        } else {
            summary.recurringTexts.entries
                .sortedByDescending { it.value }
                .take(10)
                .forEach { (text, count) ->
                    val truncatedText = if (text.length > 50) text.take(47) + "..." else text
                    sb.appendLine("  $truncatedText: $count")
                }
        }

        return sb.toString()
    }

    private fun exportSurveyResponses(responses: List<SurveyResponse>): String {
        val header = "id,userId,q_helpfulness,q_intrusiveness,q_changed_decision,q_trust,freeText,timestamp"
        val rows = responses.map { response ->
            listOf(
                response.id.toString(),
                escapeField(response.userId),
                response.q_helpfulness.toString(),
                response.q_intrusiveness.toString(),
                response.q_changed_decision.toString(),
                response.q_trust.toString(),
                escapeField(response.freeText ?: ""),
                response.timestamp.toString()
            ).joinToString(",")
        }
        return listOf(header).plus(rows).joinToString("\n")
    }

    private fun exportMetadata(userId: String, exportTime: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val dateString = dateFormat.format(Date(exportTime))

        val sb = StringBuilder()
        sb.appendLine("=== GuardRail Export Metadata ===")
        sb.appendLine()
        sb.appendLine("User ID: $userId")
        sb.appendLine("Export Time: $dateString")
        sb.appendLine("Export Timestamp: $exportTime")
        sb.appendLine("Export Version: 1.0")

        return sb.toString()
    }

    private fun escapeField(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }
}


