package com.focustimer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.focustimer.app.ui.ProfileScreen
import com.focustimer.app.ui.SettingsScreen
import com.focustimer.app.ui.TimerScreen
import com.focustimer.app.ui.theme.FocusTimerTheme

class MainActivity : ComponentActivity() {
    private val timerViewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusTimerTheme {
                AppRoot(timerViewModel)
            }
        }
    }
}

@Composable
fun AppRoot(timerViewModel: TimerViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Таймер", "Настройки", "Профиль")

    val context = LocalContext.current
    val view = LocalView.current
    val prefs = remember { PrefsManager(context) }
    val timerState by timerViewModel.uiState.collectAsState()

    LaunchedEffect(timerState.isRunning) {
        view.keepScreenOn = timerState.isRunning && prefs.keepScreenOn
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = tabs[0]) },
                    label = { Text(tabs[0]) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = tabs[1]) },
                    label = { Text(tabs[1]) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = tabs[2]) },
                    label = { Text(tabs[2]) }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> TimerScreen(timerViewModel, modifier = Modifier.padding(padding))
            1 -> SettingsScreen(modifier = Modifier.padding(padding))
            2 -> ProfileScreen(modifier = Modifier.padding(padding))
        }
    }
}
