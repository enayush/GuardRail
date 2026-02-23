package com.example.guardrail.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.guardrail.lab.LabDatabaseProvider
import com.example.guardrail.lab.SurveyResponse
import com.example.guardrail.lab.UserSession
import com.example.guardrail.ui.theme.GuardRailTheme
import kotlinx.coroutines.launch

@Composable
fun SurveyScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Survey state
    var q_helpfulness by remember { mutableIntStateOf(3) }
    var q_intrusiveness by remember { mutableIntStateOf(3) }
    var q_changed_decision by remember { mutableStateOf(false) }
    var q_trust by remember { mutableIntStateOf(3) }
    var freeText by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "User Survey",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Please share your experience with GuardRail",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Question 1: Helpfulness
        QuestionCard(
            question = "1. Did GuardRail help you notice deceptive UI?",
            selectedValue = q_helpfulness,
            onValueChange = { q_helpfulness = it },
            enabled = !isSubmitted
        )

        // Question 2: Intrusiveness
        QuestionCard(
            question = "2. Were the overlays intrusive?",
            selectedValue = q_intrusiveness,
            onValueChange = { q_intrusiveness = it },
            enabled = !isSubmitted
        )

        // Question 3: Changed Decision (Yes/No)
        YesNoQuestionCard(
            question = "3. Did it change any decision you made?",
            selectedValue = q_changed_decision,
            onValueChange = { q_changed_decision = it },
            enabled = !isSubmitted
        )

        // Question 4: Trust
        QuestionCard(
            question = "4. How much do you trust the detection?",
            selectedValue = q_trust,
            onValueChange = { q_trust = it },
            enabled = !isSubmitted
        )

        // Question 5: Optional Feedback
        FeedbackCard(
            value = freeText,
            onValueChange = { freeText = it },
            enabled = !isSubmitted
        )

        // Submit Button
        Button(
            onClick = {
                scope.launch {
                    val userId = UserSession.getUserId(context)
                    val response = SurveyResponse(
                        userId = userId,
                        q_helpfulness = q_helpfulness,
                        q_intrusiveness = q_intrusiveness,
                        q_changed_decision = q_changed_decision,
                        q_trust = q_trust,
                        freeText = freeText.ifBlank { null },
                        timestamp = System.currentTimeMillis()
                    )

                    val db = LabDatabaseProvider.get(context)
                    db.surveyDao().insert(response)
                    isSubmitted = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isSubmitted
        ) {
            Text(
                text = if (isSubmitted) "Submitted" else "Submit Survey",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (isSubmitted) {
            Text(
                text = "Thank you for your feedback!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun QuestionCard(
    question: String,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    enabled: Boolean,
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
                text = question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "1 = Strongly Disagree, 5 = Strongly Agree",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Radio buttons for 1-5
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                (1..5).forEach { value ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedValue == value,
                                onClick = { if (enabled) onValueChange(value) },
                                role = Role.RadioButton,
                                enabled = enabled
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedValue == value,
                            onClick = null,
                            enabled = enabled
                        )
                        Text(
                            text = value.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YesNoQuestionCard(
    question: String,
    selectedValue: Boolean,
    onValueChange: (Boolean) -> Unit,
    enabled: Boolean,
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
                text = question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            // Radio buttons for Yes/No
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Yes option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedValue,
                            onClick = { if (enabled) onValueChange(true) },
                            role = Role.RadioButton,
                            enabled = enabled
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedValue,
                        onClick = null,
                        enabled = enabled
                    )
                    Text(
                        text = "Yes",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // No option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = !selectedValue,
                            onClick = { if (enabled) onValueChange(false) },
                            role = Role.RadioButton,
                            enabled = enabled
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !selectedValue,
                        onClick = null,
                        enabled = enabled
                    )
                    Text(
                        text = "No",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackCard(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
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
                text = "5. Optional Feedback",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Share any additional thoughts or suggestions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                enabled = enabled,
                placeholder = { Text("Enter your feedback (optional)") },
                maxLines = 5
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SurveyScreenPreview() {
    GuardRailTheme {
        SurveyScreen()
    }
}


