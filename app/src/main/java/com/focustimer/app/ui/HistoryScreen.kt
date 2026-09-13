package com.focustimer.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focustimer.app.PrefsManager
import com.focustimer.app.SessionRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }
    var history by remember { mutableStateOf(prefs.getHistory()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("История", style = MaterialTheme.typography.headlineMedium)

        if (history.isEmpty()) {
            Text(
                "Пока пусто — здесь появится история ваших сессий работы и отдыха",
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(history, key = { it.id }) { entry ->
                    HistoryRow(
                        entry = entry,
                        onCommentChange = { newComment ->
                            prefs.updateHistoryComment(entry.id, newComment)
                            history = prefs.getHistory()
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: SessionRecord, onCommentChange: (String) -> Unit) {
    var comment by remember(entry.id) { mutableStateOf(entry.comment) }
    val phaseLabel = if (entry.phase == "WORK") "Работа" else "Отдых"
    val dateText = remember(entry.startTimeMillis) {
        SimpleDateFormat("dd MMM, HH:mm", Locale("ru")).format(Date(entry.startTimeMillis))
    }
    val minutes = entry.durationSeconds / 60
    val seconds = entry.durationSeconds % 60

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = phaseLabel + if (entry.interrupted) " (прервано)" else "",
                fontWeight = FontWeight.Bold
            )
            Text(dateText, style = MaterialTheme.typography.bodySmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("%d:%02d".format(minutes, seconds), style = MaterialTheme.typography.bodyMedium)
            if (entry.category.isNotBlank()) {
                Text(
                    "• ${entry.category}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        OutlinedTextField(
            value = comment,
            onValueChange = {
                comment = it
                onCommentChange(it)
            },
            label = { Text("Комментарий") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}
