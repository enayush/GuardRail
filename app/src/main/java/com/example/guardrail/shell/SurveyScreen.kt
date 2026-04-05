package com.example.guardrail.shell

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.guardrail.lab.LabDatabaseProvider
import com.example.guardrail.lab.SurveyResponse
import com.example.guardrail.lab.UserSession
import kotlinx.coroutines.launch

@Composable
fun SurveyScreen(modifier: Modifier = Modifier, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // State for all 8 Likert questions (Default to 3 = Neutral)
    var qAwareness by remember { mutableIntStateOf(3) }
    var qLearning by remember { mutableIntStateOf(3) }
    var qHesitation by remember { mutableIntStateOf(3) }
    var qAvoidance by remember { mutableIntStateOf(3) }
    var qInterference by remember { mutableIntStateOf(3) }
    var qFalsePositives by remember { mutableIntStateOf(3) }
    var qTrust by remember { mutableIntStateOf(3) }
    var qRetention by remember { mutableIntStateOf(3) }

    // Text Feedback
    var criticalIncidentText by remember { mutableStateOf("") }
    var generalFeedback by remember { mutableStateOf("") }

    var isSubmitted by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "GuardRail Exit Survey",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Text(
                text = "Please evaluate your experience. For the scales below, 1 = Strongly Disagree and 5 = Strongly Agree.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        // Section 1: Awareness & Learning
        SurveySection("Awareness & Learning") {
            LikertQuestion("1. The app made me more aware of manipulative UI (Dark Patterns).", qAwareness) { if (!isSubmitted) qAwareness = it }
            LikertQuestion("2. I began recognizing dark patterns on my own, even before the overlay appeared.", qLearning) { if (!isSubmitted) qLearning = it }
        }

        // Section 2: Behavioral Impact
        SurveySection("Behavioral Impact") {
            LikertQuestion("3. The warnings caused me to pause and think before interacting with flagged UI.", qHesitation) { if (!isSubmitted) qHesitation = it }
            LikertQuestion("4. I successfully avoided making unintended clicks or decisions because of the warnings.", qAvoidance) { if (!isSubmitted) qAvoidance = it }
        }

        // Section 3: Usability & Interference
        SurveySection("Usability & Interference") {
            LikertQuestion("5. The red overlays significantly interfered with my normal app usage.", qInterference) { if (!isSubmitted) qInterference = it }
            LikertQuestion("6. The system frequently flagged safe, normal UI elements by mistake (False Positives).", qFalsePositives) { if (!isSubmitted) qFalsePositives = it }
        }

        // Section 4: Trust & Adoption
        SurveySection("Trust & Future Use") {
            LikertQuestion("7. I trust the system's ability to accurately identify deceptive content.", qTrust) { if (!isSubmitted) qTrust = it }
            LikertQuestion("8. I would keep this application installed on my primary device.", qRetention) { if (!isSubmitted) qRetention = it }
        }

        // Section 5: Qualitative Data
        SurveySection("Qualitative Feedback") {
            TextQuestion(
                "9. Critical Incident (Optional)",
                "Describe one specific instance where GuardRail changed your action.",
                criticalIncidentText,
                enabled = !isSubmitted
            ) { criticalIncidentText = it }

            Spacer(modifier = Modifier.height(8.dp))

            TextQuestion(
                "10. General Feedback (Optional)",
                "Any bugs, annoyances, or feature requests?",
                generalFeedback,
                enabled = !isSubmitted
            ) { generalFeedback = it }
        }

        // Submit Button
        Button(
            onClick = {
                scope.launch {
                    val userId = UserSession.getUserId(context)
                    val response = SurveyResponse(
                        userId = userId,
                        q_awareness = qAwareness,
                        q_learning = qLearning,
                        q_hesitation = qHesitation,
                        q_avoidance = qAvoidance,
                        q_interference = qInterference,
                        q_false_positives = qFalsePositives,
                        q_trust = qTrust,
                        q_retention = qRetention,
                        critical_incident_text = criticalIncidentText.ifBlank { null },
                        general_feedback = generalFeedback.ifBlank { null }
                    )
                    LabDatabaseProvider.get(context).surveyDao().insert(response)
                    isSubmitted = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp),
            enabled = !isSubmitted
        ) {
            Text(if (isSubmitted) "Data Recorded Successfully" else "Submit Final Survey")
        }
    }
}

@Composable
private fun SurveySection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun LikertQuestion(
    question: String,
    selectedValue: Int,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = question,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            (1..5).forEach { value ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.selectable(
                        selected = selectedValue == value,
                        onClick = { onValueChange(value) },
                        role = Role.RadioButton
                    )
                ) {
                    RadioButton(
                        selected = selectedValue == value,
                        onClick = null // Handled by parent modifier
                    )
                    Text(text = value.toString(), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun TextQuestion(
    title: String,
    subtitle: String,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(text = subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .height(84.dp),
            enabled = enabled,
            maxLines = 3,
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}