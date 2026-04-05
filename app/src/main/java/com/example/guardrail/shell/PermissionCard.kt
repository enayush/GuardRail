package com.example.guardrail.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.guardrail.ui.theme.GuardRailTheme

@Composable
fun PermissionCard(
    title: String,
    description: String,
    granted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Status and Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Text
                Text(
                    text = if (granted) "Granted" else "Not Granted",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (granted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )

                // Button
                if (!granted) {
                    Button(onClick = onClick) {
                        Text("Open Settings")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionCardPreviewGranted() {
    GuardRailTheme {
        PermissionCard(
            title = "Accessibility Service",
            description = "Required to detect dark patterns in real-time",
            granted = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionCardPreviewNotGranted() {
    GuardRailTheme {
        PermissionCard(
            title = "Overlay Permission",
            description = "Allows app to display alerts over other apps",
            granted = false,
            onClick = {}
        )
    }
}

