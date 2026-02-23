package com.example.guardrail.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.guardrail.analysis.DetectionCountReader
import com.example.guardrail.ui.theme.GuardRailTheme
import com.example.guardrail.utils.PermissionIntents
import com.example.guardrail.utils.PermissionState

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    var detectionCount by remember { mutableStateOf<Int?>(null) }

    // Load detection count on composition and when refreshed
    LaunchedEffect(refreshKey) {
        detectionCount = DetectionCountReader().getTotalDetections(context)
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Permission cards section with refresh key
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Setup Required Permissions",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "GuardRail needs the following permissions to protect you from dark patterns:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Use refreshKey to force recomposition when refresh button is clicked
            if (refreshKey >= 0) {
                // 1. Accessibility Service Card
                PermissionCard(
                    title = "Accessibility Service",
                    description = "Required to detect dark patterns in real-time",
                    granted = PermissionState.isAccessibilityEnabled(context),
                    onClick = { PermissionIntents.openAccessibilitySettings(context) }
                )

                // 2. Overlay Permission Card
                PermissionCard(
                    title = "Overlay Permission",
                    description = "Allows app to display alerts over other apps",
                    granted = PermissionState.isOverlayGranted(context),
                    onClick = { PermissionIntents.openOverlaySettings(context) }
                )

                // 3. Battery Optimization Ignore Card
                PermissionCard(
                    title = "Battery Optimization Ignore",
                    description = "Prevents app from being stopped in background",
                    granted = PermissionState.isBatteryOptimizationIgnored(context),
                    onClick = { PermissionIntents.openBatteryOptimizationSettings(context) }
                )
            }

            // Service Status Display
            Text(
                text = "Service Status:",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = if (PermissionState.isGuardRailServiceRunning(context)) {
                    "GuardRail Service is Running"
                } else {
                    "GuardRail Service is Not Running"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (PermissionState.isGuardRailServiceRunning(context)) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }

        // Refresh button at bottom
        FloatingActionButton(
            onClick = { refreshKey++ },
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Refresh permission status"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GuardRailTheme {
        HomeScreen()
    }
}



