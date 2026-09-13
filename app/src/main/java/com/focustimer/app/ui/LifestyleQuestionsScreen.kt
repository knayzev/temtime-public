package com.focustimer.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focustimer.app.PrefsManager

@Composable
fun LifestyleQuestionsScreen(onComplete: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }

    val initialAnswers: Map<String, List<String>> = LIFESTYLE_QUESTIONS.associate { q ->
        q.id to (prefs.lifestyleAnswers[q.id] ?: emptyList())
    }
    var answers by remember { mutableStateOf(initialAnswers) }

    var showResult by remember { mutableStateOf(false) }
    var regenerateCount by remember { mutableStateOf(0) }
    var scheduleItems by remember { mutableStateOf<List<ScheduleItem>>(emptyList()) }
    var showCustomInput by remember { mutableStateOf(false) }
    var customSchedule by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Немного о вашем режиме", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Это поможет предложить подходящий график дня",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        if (!showResult) {
            LIFESTYLE_QUESTIONS.forEach { question ->
                QuestionBlock(
                    question = question,
                    selected = answers[question.id] ?: emptyList(),
                    onChange = { updated -> answers = answers + (question.id to updated) }
                )
                Divider(modifier = Modifier.padding(vertical = 16.dp))
            }

            Button(
                onClick = {
                    prefs.lifestyleAnswers = answers
                    scheduleItems = generateSchedule(
                        answers, prefs.wakeTime, prefs.bedTime, prefs.breakfastTime, prefs.lunchTime, prefs.dinnerTime, 0
                    )
                    regenerateCount = 0
                    showCustomInput = false
                    showResult = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сгенерировать график")
            }
        } else {
            Text("Предложенный график дня", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (showCustomInput) {
                OutlinedTextField(
                    value = customSchedule,
                    onValueChange = { customSchedule = it },
                    label = { Text("Ваш график") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp)
                        .padding(top = 12.dp)
                )
            } else {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    scheduleItems.forEach { item ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text("${item.time} — ${item.title}", fontWeight = FontWeight.Medium)
                            item.tips.forEach { tip ->
                                Text(
                                    "• $tip",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        regenerateCount++
                        scheduleItems = generateSchedule(
                            answers, prefs.wakeTime, prefs.bedTime, prefs.breakfastTime, prefs.lunchTime, prefs.dinnerTime,
                            variant = regenerateCount
                        )
                        showCustomInput = false
                    },
                    enabled = regenerateCount < 3,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ещё варианты (${3 - regenerateCount})")
                }

                OutlinedButton(
                    onClick = { showCustomInput = !showCustomInput },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (showCustomInput) "К предложенному" else "Свой вариант")
                }
            }

            Button(
                onClick = {
                    prefs.daySchedule = if (showCustomInput && customSchedule.isNotBlank()) {
                        customSchedule
                    } else {
                        scheduleToText(scheduleItems)
                    }
                    prefs.isOnboarded = true
                    onComplete()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text("Сохранить и продолжить")
            }

            TextButton(
                onClick = { showResult = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Изменить ответы")
            }
        }
    }
}

@Composable
private fun QuestionBlock(question: LifestyleQuestion, selected: List<String>, onChange: (List<String>) -> Unit) {
    Text(
        question.question,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    question.options.forEach { option ->
        val checked = option in selected
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChange(if (checked) selected - option else selected + option) }
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { onChange(if (it) selected + option else selected - option) }
            )
            Text(option)
        }
    }

    val customEntries = selected.filter { it !in question.options }
    customEntries.forEach { custom ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("• $custom")
            IconButton(onClick = { onChange(selected - custom) }) {
                Icon(Icons.Default.Close, contentDescription = "Удалить $custom")
            }
        }
    }

    var customText by remember(question.id) { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = customText,
            onValueChange = { customText = it },
            label = { Text("Свой вариант") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        TextButton(
            onClick = {
                val trimmed = customText.trim()
                if (trimmed.isNotEmpty() && trimmed !in selected) {
                    onChange(selected + trimmed)
                }
                customText = ""
            },
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Text("Добавить")
        }
    }
}
