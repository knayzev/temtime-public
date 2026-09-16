package com.focustimer.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
    var variants by remember { mutableStateOf<List<List<ScheduleItem>>>(emptyList()) }
    var selectedIndex by remember { mutableStateOf(0) }
    var editedItems by remember { mutableStateOf<List<ScheduleItem>>(emptyList()) }
    var showCompare by remember { mutableStateOf(false) }

    fun selectVariant(index: Int) {
        selectedIndex = index
        editedItems = variants[index]
    }

    fun generateMoreParams() = generateSchedule(
        answers, prefs.wakeTime, prefs.bedTime, prefs.breakfastTime, prefs.lunchTime, prefs.dinnerTime,
        variant = variants.size
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        StepperHeader(
            currentStep = 1,
            labels = listOf("О себе", "План на день", "Готово"),
            modifier = Modifier.padding(bottom = 20.dp)
        )

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
                    val first = generateSchedule(
                        answers, prefs.wakeTime, prefs.bedTime, prefs.breakfastTime, prefs.lunchTime, prefs.dinnerTime,
                        variant = 0
                    )
                    variants = listOf(first)
                    selectedIndex = 0
                    editedItems = first
                    showCompare = false
                    showResult = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сгенерировать график")
            }
        } else {
            Text(
                "График дня №${selectedIndex + 1}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { if (selectedIndex > 0) selectVariant(selectedIndex - 1) },
                    enabled = selectedIndex > 0
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Предыдущий график")
                }
                variants.indices.forEach { i ->
                    VariantChip(number = i + 1, selected = i == selectedIndex) { selectVariant(i) }
                }
                IconButton(
                    onClick = { if (selectedIndex < variants.lastIndex) selectVariant(selectedIndex + 1) },
                    enabled = selectedIndex < variants.lastIndex
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Следующий график")
                }
            }

            if (showCompare && variants.size > 1) {
                ScheduleCompareTable(variants = variants, modifier = Modifier.padding(top = 16.dp))
            } else {
                Text(
                    "Подстройте время под себя — остальное менять не обязательно",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    editedItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = item.time,
                                onValueChange = { newTime ->
                                    editedItems = editedItems.mapIndexed { i, it ->
                                        if (i == index) it.copy(time = newTime) else it
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.width(90.dp)
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(item.title, fontWeight = FontWeight.Medium)
                                item.tips.forEach { tip ->
                                    Text(
                                        "• $tip",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
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
                        val next = generateMoreParams()
                        variants = variants + listOf(next)
                        selectVariant(variants.lastIndex)
                        showCompare = false
                    },
                    enabled = variants.size < 4,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ещё графики (${4 - variants.size})")
                }

                OutlinedButton(
                    onClick = { showCompare = !showCompare },
                    enabled = variants.size > 1,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (showCompare) "К графику" else "Сравнить графики")
                }
            }

            Button(
                onClick = {
                    prefs.daySchedule = scheduleToText(editedItems)
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
private fun VariantChip(number: Int, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .width(36.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            Text(
                number.toString(),
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ScheduleCompareTable(variants: List<List<ScheduleItem>>, modifier: Modifier = Modifier) {
    val rowCount = variants.firstOrNull()?.size ?: 0
    Column(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        Row {
            Text(
                "",
                modifier = Modifier.width(150.dp),
                fontWeight = FontWeight.Bold
            )
            variants.indices.forEach { i ->
                Text(
                    "№${i + 1}",
                    modifier = Modifier
                        .width(70.dp)
                        .padding(vertical = 6.dp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Divider()
        for (row in 0 until rowCount) {
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    variants[0].getOrNull(row)?.title ?: "",
                    modifier = Modifier.width(150.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                variants.forEach { variant ->
                    Text(
                        variant.getOrNull(row)?.time ?: "—",
                        modifier = Modifier.width(70.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
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
