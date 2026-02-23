package com.example.guardrail.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.guardrail.evaluation.ExportBundleBuilder
import com.example.guardrail.ui.theme.GuardRailTheme
import kotlinx.coroutines.launch

@Composable
fun EvaluationScreen(
    onViewAnalysis: () -> Unit,
    onTakeSurvey: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Evaluation",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Review your data, take surveys, and export your information",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Button 1: View Analysis
        Button(
            onClick = onViewAnalysis,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "View Analysis",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Button 2: Take Survey
        Button(
            onClick = onTakeSurvey,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Take Survey",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Button 3: Export My Data
        Button(
            onClick = {
                isExporting = true
                exportMessage = null
                scope.launch {
                    try {
                        val exportFile = ExportBundleBuilder.buildExport(context)
                        ExportBundleBuilder.shareExport(context, exportFile)
                        exportMessage = "Export created successfully"
                    } catch (e: Exception) {
                        exportMessage = "Export failed: ${e.message}"
                    } finally {
                        isExporting = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isExporting
        ) {
            Text(
                text = if (isExporting) "Exporting..." else "Export My Data",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Export status message
        exportMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (message.contains("success")) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EvaluationScreenPreview() {
    GuardRailTheme {
        EvaluationScreen(
            onViewAnalysis = {},
            onTakeSurvey = {}
        )
    }
}


