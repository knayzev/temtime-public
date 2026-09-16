package com.focustimer.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun IntroScreen(onContinue: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HeroGlyph(emoji = "🌱", size = 112.dp)

        Text(
            "Добро пожаловать в EPV",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp)
        )
        Text(
            "Это не просто таймер, а инструмент самодисциплины",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Каждая отмеченная сессия, привычка и план на день — честный учёт того, " +
                        "что вы на самом деле делаете.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Со временем этот учёт складывается в понятную картину: где вы теряете время, " +
                        "что помогает фокусироваться, а что мешает. Регулярность важнее интенсивности — " +
                        "маленькие ежедневные шаги надёжнее редких рывков.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        Text(
            "Мы дадим структуру и напоминания — результат создаёте именно вы, шаг за шагом.",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp)
        )

        Button(
            onClick = onContinue,
            shape = RoundedCornerShape(16.dp),
            contentPadding = ButtonDefaults.ContentPadding,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        ) {
            Text("Начать", style = MaterialTheme.typography.titleMedium)
        }
    }
}
