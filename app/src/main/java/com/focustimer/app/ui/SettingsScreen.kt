package com.focustimer.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.focustimer.app.PrefsManager

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }

    var soundEnabled by remember { mutableStateOf(prefs.soundEnabled) }
    var vibrationEnabled by remember { mutableStateOf(prefs.vibrationEnabled) }
    var keepScreenOn by remember { mutableStateOf(prefs.keepScreenOn) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Настройки", style = MaterialTheme.typography.headlineMedium)

        SettingRow("Звук по окончании этапа", soundEnabled) {
            soundEnabled = it
            prefs.soundEnabled = it
        }
        SettingRow("Вибрация по окончании этапа", vibrationEnabled) {
            vibrationEnabled = it
            prefs.vibrationEnabled = it
        }
        SettingRow("Не выключать экран во время таймера", keepScreenOn) {
            keepScreenOn = it
            prefs.keepScreenOn = it
        }
    }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.padding(end = 16.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
