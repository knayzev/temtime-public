package com.focustimer.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
fun OnboardingScreen(onComplete: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }

    var bedTime by remember { mutableStateOf(prefs.bedTime) }
    var breakfastTime by remember { mutableStateOf(prefs.breakfastTime) }
    var lunchTime by remember { mutableStateOf(prefs.lunchTime) }
    var dinnerTime by remember { mutableStateOf(prefs.dinnerTime) }
    var workHours by remember { mutableStateOf(prefs.workHoursPerDay) }
    var mealsPerDay by remember { mutableStateOf(prefs.mealsPerDay) }
    var waterUnit by remember { mutableStateOf(prefs.waterUnit) }
    var waterCount by remember { mutableStateOf(prefs.waterCount) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            "Расскажите о своём дне",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Это поможет давать более точные советы",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        TimePickerRow(
            label = "Во сколько ложитесь спать",
            timeText = bedTime,
            onTimeChange = { bedTime = it; prefs.bedTime = it }
        )
        TimePickerRow(
            label = "Во сколько завтракаете",
            timeText = breakfastTime,
            onTimeChange = { breakfastTime = it; prefs.breakfastTime = it }
        )
        TimePickerRow(
            label = "Когда обедаете",
            timeText = lunchTime,
            onTimeChange = { lunchTime = it; prefs.lunchTime = it }
        )
        TimePickerRow(
            label = "Когда ужинаете",
            timeText = dinnerTime,
            onTimeChange = { dinnerTime = it; prefs.dinnerTime = it }
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        StepperRow(
            label = "Часов работы в день",
            value = workHours,
            range = 1..16,
            onValueChange = { workHours = it; prefs.workHoursPerDay = it }
        )
        StepperRow(
            label = "Приёмов пищи в день",
            value = mealsPerDay,
            range = 1..6,
            onValueChange = { mealsPerDay = it; prefs.mealsPerDay = it }
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        DropdownField(
            label = "Считать воду в",
            selected = waterUnit,
            options = listOf("Бутылки", "Чай/кофе"),
            onSelected = { waterUnit = it; prefs.waterUnit = it }
        )
        StepperRow(
            label = if (waterUnit == "Бутылки") "Бутылок воды в день" else "Чашек чая/кофе в день",
            value = waterCount,
            range = 0..15,
            onValueChange = { waterCount = it; prefs.waterCount = it }
        )

        Button(
            onClick = {
                prefs.isOnboarded = true
                onComplete()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        ) {
            Text("Готово")
        }
    }
}

@Composable
private fun StepperRow(label: String, value: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (value > range.first) onValueChange(value - 1) }) {
                Icon(Icons.Default.Remove, contentDescription = "Меньше")
            }
            Text(
                "$value",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(onClick = { if (value < range.last) onValueChange(value + 1) }) {
                Icon(Icons.Default.Add, contentDescription = "Больше")
            }
        }
    }
}
