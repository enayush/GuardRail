package com.example.guardrail.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.guardrail.evaluation.AnalysisSummary
import com.example.guardrail.evaluation.AnalysisSummaryProvider
import com.example.guardrail.ui.theme.GuardRailTheme

@Composable
fun AnalysisScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var summary by remember { mutableStateOf<AnalysisSummary?>(null) }

    // Load summary on composition
    LaunchedEffect(Unit) {
        val provider = AnalysisSummaryProvider()
        summary = provider.loadSummary(context)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Analysis Summary",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        summary?.let { data ->
            // Summary Stats
            SummaryCard(
                totalDetections = data.totalDetections,
                meanLatencyMs = data.meanLatencyMs,
                maxLatencyMs = data.maxLatencyMs
            )

            // Top Apps Section
            SectionCard(title = "Top Apps by Detection Count") {
                val topApps = data.detectionsPerApp
                    .entries
                    .sortedByDescending { it.value }
                    .take(5)

                if (topApps.isEmpty()) {
                    Text(
                        text = "No data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    topApps.forEachIndexed { index, entry ->
                        if (index > 0) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        KeyValueRow(key = entry.key, value = entry.value.toString())
                    }
                }
            }

            // Latency Buckets Section
            SectionCard(title = "Latency Distribution") {
                val buckets = listOf("<10ms", "10–25ms", "25–50ms", ">50ms")
                buckets.forEachIndexed { index, bucket ->
                    if (index > 0) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                    KeyValueRow(
                        key = bucket,
                        value = data.latencyBuckets[bucket]?.toString() ?: "0"
                    )
                }
            }

            // Top Recurring Texts Section
            SectionCard(title = "Top Recurring Detected Texts") {
                val topTexts = data.recurringTexts
                    .entries
                    .sortedByDescending { it.value }
                    .take(5)

                if (topTexts.isEmpty()) {
                    Text(
                        text = "No data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    topTexts.forEachIndexed { index, entry ->
                        if (index > 0) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        KeyValueRow(key = entry.key, value = entry.value.toString())
                    }
                }
            }
        } ?: run {
            // Loading state
            Text(
                text = "Loading analysis...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummaryCard(
    totalDetections: Int,
    meanLatencyMs: Double,
    maxLatencyMs: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            KeyValueRow(key = "Total Detections", value = totalDetections.toString())
            HorizontalDivider()
            KeyValueRow(key = "Mean Latency", value = "%.2f ms".format(meanLatencyMs))
            HorizontalDivider()
            KeyValueRow(key = "Max Latency", value = "$maxLatencyMs ms")
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun KeyValueRow(
    key: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnalysisScreenPreview() {
    GuardRailTheme {
        AnalysisScreen()
    }
}

