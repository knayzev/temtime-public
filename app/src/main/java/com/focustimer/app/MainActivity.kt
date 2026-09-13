package com.focustimer.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.focustimer.app.ui.HistoryScreen
import com.focustimer.app.ui.ProfileScreen
import com.focustimer.app.ui.SettingsScreen
import com.focustimer.app.ui.StatsScreen
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
    val tabs = listOf("Таймер", "История", "Статистика", "Настройки", "Профиль")

    val context = LocalContext.current
    val view = LocalView.current
    val prefs = remember { PrefsManager(context) }
    val timerState by timerViewModel.uiState.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Coming back to the app (from launcher, Recents, or a notification tap) counts as
    // acknowledging any pending phase-change/escalation, since the user is now looking at it.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                timerViewModel.acknowledge()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                    icon = { Icon(Icons.Default.History, contentDescription = tabs[1]) },
                    label = { Text(tabs[1]) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = tabs[2]) },
                    label = { Text(tabs[2]) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = tabs[3]) },
                    label = { Text(tabs[3]) }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Person, contentDescription = tabs[4]) },
                    label = { Text(tabs[4]) }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> TimerScreen(timerViewModel, modifier = Modifier.padding(padding))
            1 -> HistoryScreen(modifier = Modifier.padding(padding))
            2 -> StatsScreen(modifier = Modifier.padding(padding))
            3 -> SettingsScreen(modifier = Modifier.padding(padding))
            4 -> ProfileScreen(modifier = Modifier.padding(padding))
        }
    }
}
