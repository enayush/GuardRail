package com.example.guardrail.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.guardrail.analysis.DetectionCountReader
import com.example.guardrail.lab.UserSession
import com.example.guardrail.ui.theme.GuardRailTheme
import com.example.guardrail.utils.PermissionState

@Composable
fun StatusScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // State for detection count
    var detectionCount by remember { mutableIntStateOf(0) }

    // State for session ID
    var sessionId by remember { mutableStateOf("") }

    // Load data on composition
    LaunchedEffect(Unit) {
        detectionCount = DetectionCountReader().getTotalDetections(context)
        sessionId = UserSession.getUserId(context)
    }

    // Read service status directly (no state needed, reads on every recomposition)
    val isServiceRunning = PermissionState.isGuardRailServiceRunning(context)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Runtime Status",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Service Status Card
        StatusCard(
            label = "Service Status",
            value = if (isServiceRunning) "Running" else "Not Running",
            valueColor = if (isServiceRunning) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )

        // Detection Count Card
        StatusCard(
            label = "Total Detections",
            value = detectionCount.toString(),
            valueColor = MaterialTheme.colorScheme.onSurface
        )

        // Session ID Card
        StatusCard(
            label = "Session ID",
            value = sessionId.take(6),
            valueColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatusCard(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = valueColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatusScreenPreview() {
    GuardRailTheme {
        StatusScreen()
    }
}

