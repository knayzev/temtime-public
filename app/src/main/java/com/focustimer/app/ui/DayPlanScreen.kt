package com.focustimer.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focustimer.app.DayPlan
import com.focustimer.app.PrefsManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DayPlanScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }
    val saved = remember { prefs.dayPlan }

    var tasks by remember { mutableStateOf(saved.tasks) }
    var priority by remember { mutableStateOf(saved.priority) }
    var dontForget by remember { mutableStateOf(saved.dontForget) }
    var lastSavedAt by remember { mutableStateOf(saved.updatedAtMillis) }
    var justSaved by remember { mutableStateOf(false) }

    LaunchedEffect(justSaved) {
        if (justSaved) {
            kotlinx.coroutines.delay(1500)
            justSaved = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
            }
            Text(
                "План на день",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (lastSavedAt > 0L) {
            Text(
                "Сохранено: ${formatPlanTimestamp(lastSavedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 48.dp, top = 4.dp)
            )
        }

        OutlinedTextField(
            value = tasks,
            onValueChange = { tasks = it },
            label = { Text("Что нужно сделать") },
            placeholder = { Text("Список задач на сегодня") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(140.dp)
        )

        OutlinedTextField(
            value = priority,
            onValueChange = { priority = it },
            label = { Text("Что в первую очередь") },
            placeholder = { Text("Самое важное, с чего начать") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(120.dp)
        )

        OutlinedTextField(
            value = dontForget,
            onValueChange = { dontForget = it },
            label = { Text("Что не забыть") },
            placeholder = { Text("Мелочи, звонки, напоминания") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(120.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (justSaved) {
                Text(
                    "Сохранено",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            Button(onClick = {
                val now = System.currentTimeMillis()
                prefs.dayPlan = DayPlan(
                    tasks = tasks,
                    priority = priority,
                    dontForget = dontForget,
                    updatedAtMillis = now
                )
                lastSavedAt = now
                justSaved = true
            }) {
                Text("Сохранить план")
            }
        }
    }
}

private fun formatPlanTimestamp(millis: Long): String =
    SimpleDateFormat("d MMM, HH:mm", Locale("ru")).format(Date(millis))
