package com.focustimer.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun IntroScreen(onContinue: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Добро пожаловать в EPV",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Это не просто таймер, а инструмент самодисциплины. Каждая отмеченная сессия, " +
                "привычка и график дня — честный учёт того, что вы на самом деле делаете.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            "Со временем этот учёт складывается в понятную картину: где вы теряете время, " +
                "что помогает вам фокусироваться, а что мешает. Регулярность важнее интенсивности — " +
                "маленькие ежедневные шаги надёжнее, чем редкие рывки.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            "Мы дадим структуру и напоминания — но результат создаёте именно вы, шаг за шагом.",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        ) {
            Text("Начать")
        }
    }
}
