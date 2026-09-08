package com.focustimer.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focustimer.app.TimerPhase
import com.focustimer.app.TimerViewModel

@Composable
fun TimerScreen(viewModel: TimerViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    val minutes = state.secondsLeft / 60
    val seconds = state.secondsLeft % 60

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (state.phase == TimerPhase.WORK) "Работа" else "Отдых",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
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
