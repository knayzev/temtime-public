package com.focustimer.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.core.content.ContextCompat
import com.focustimer.app.PrefsManager

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }

    var soundEnabled by remember { mutableStateOf(prefs.soundEnabled) }
    var vibrationEnabled by remember { mutableStateOf(prefs.vibrationEnabled) }
    var keepScreenOn by remember { mutableStateOf(prefs.keepScreenOn) }

    var telegramEnabled by remember { mutableStateOf(prefs.telegramEnabled) }
    var telegramToken by remember { mutableStateOf(prefs.telegramBotToken) }
    var telegramChatId by remember { mutableStateOf(prefs.telegramChatId) }

    var autoCallEnabled by remember { mutableStateOf(prefs.autoCallEnabled) }
    var autoCallNumber by remember { mutableStateOf(prefs.autoCallNumber) }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        autoCallEnabled = granted
        prefs.autoCallEnabled = granted
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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

        Divider(modifier = Modifier.padding(vertical = 16.dp))
        Text(
            "Если пропустили окно таймера",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            "Если через 1 минуту после смены этапа вы не открыли приложение — сработает выбранное ниже",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        SettingRow("Сообщение в Telegram", telegramEnabled) {
            telegramEnabled = it
            prefs.telegramEnabled = it
        }
        if (telegramEnabled) {
            OutlinedTextField(
                value = telegramToken,
                onValueChange = {
                    telegramToken = it
                    prefs.telegramBotToken = it
                },
                label = { Text("Токен бота (от @BotFather)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
            OutlinedTextField(
                value = telegramChatId,
                onValueChange = {
                    telegramChatId = it
                    prefs.telegramChatId = it
                },
                label = { Text("Chat ID") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp)
            )
        }

        SettingRow("Автозвонок", autoCallEnabled) { checked ->
            if (checked) {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    autoCallEnabled = true
                    prefs.autoCallEnabled = true
                } else {
                    callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                }
            } else {
                autoCallEnabled = false
                prefs.autoCallEnabled = false
            }
        }
        if (autoCallEnabled) {
            OutlinedTextField(
                value = autoCallNumber,
                onValueChange = {
                    autoCallNumber = it
                    prefs.autoCallNumber = it
                },
                label = { Text("Номер телефона") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
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
