package com.focustimer.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.focustimer.app.PrefsManager
import com.focustimer.app.TimerPhase
import com.focustimer.app.TimerViewModel

@Composable
fun TimerScreen(viewModel: TimerViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    val minutes = state.secondsLeft / 60
    val seconds = state.secondsLeft % 60
    val context = LocalContext.current
    val categories = remember { PrefsManager(context).categories }

    // Being on this screen counts as noticing the current phase.
    LaunchedEffect(Unit) {
        viewModel.acknowledge()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.escalationActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFCDD2))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Окно пропущено — вас уведомили", color = Color(0xFFB71C1C))
                TextButton(onClick = { viewModel.acknowledge() }) {
                    Text("Я тут")
                }
            }
        }

        Text(
            text = if (state.phase == TimerPhase.WORK) "Работа" else "Отдых",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )

        if (categories.isNotEmpty()) {
            DropdownField(
                label = "Чем занимаетесь",
                selected = state.currentCategory.ifBlank { categories.first() },
                options = categories,
                onSelected = { viewModel.setCategory(it) },
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        Text(
            text = "%02d:%02d".format(minutes, seconds),
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { if (state.isRunning) viewModel.pause() else viewModel.start() }) {
                Text(if (state.isRunning) "Пауза" else "Старт")
            }
            OutlinedButton(onClick = { viewModel.stop() }) {
                Text("Стоп")
            }
        }

        var showComment by remember { mutableStateOf(false) }
        TextButton(
            onClick = { showComment = !showComment },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(if (showComment) "Скрыть комментарий" else "Добавить комментарий")
        }
        if (showComment) {
            OutlinedTextField(
                value = state.currentComment,
                onValueChange = { viewModel.setComment(it) },
                label = { Text("Комментарий к сессии") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (!state.isRunning) {
            Column(modifier = Modifier.padding(top = 32.dp).fillMaxWidth()) {
                Text("Время работы: ${state.workMinutes} мин")
                Slider(
                    value = state.workMinutes.toFloat(),
                    onValueChange = { viewModel.setWorkMinutes(it.toInt()) },
                    valueRange = 5f..100f,
                    steps = 18
                )
                Text("Время отдыха: ${state.restMinutes} мин")
                Slider(
                    value = state.restMinutes.toFloat(),
                    onValueChange = { viewModel.setRestMinutes(it.toInt()) },
                    valueRange = 5f..100f,
                    steps = 18
                )
            }
        }
    }
}
