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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.focustimer.app.ui.AuthScreen
import com.focustimer.app.ui.DayPlanScreen
import com.focustimer.app.ui.HistoryScreen
import com.focustimer.app.ui.IntroScreen
import com.focustimer.app.ui.LifestyleQuestionsScreen
import com.focustimer.app.ui.OnboardingScreen
import com.focustimer.app.ui.ProfileScreen
import com.focustimer.app.ui.RoutineScreen
import com.focustimer.app.ui.SettingsScreen
import com.focustimer.app.ui.StatsScreen
import com.focustimer.app.ui.TimerScreen
import com.focustimer.app.ui.theme.FocusTimerTheme
import com.focustimer.app.ui.theme.RestColor
import com.focustimer.app.ui.theme.WorkColor

class MainActivity : ComponentActivity() {
    private val timerViewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusTimerTheme {
                RootNavigator(timerViewModel)
            }
        }
    }
}

private enum class RootScreen { AUTH, INTRO, ONBOARDING, LIFESTYLE, PLAN_SETUP, MAIN }

private enum class OverlayScreen { DAY_PLAN, ROUTINE }

@Composable
fun RootNavigator(timerViewModel: TimerViewModel) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }

    var screen by remember {
        mutableStateOf(
            when {
                !prefs.isRegistered || !prefs.isLoggedIn -> RootScreen.AUTH
                !prefs.isOnboarded -> RootScreen.INTRO
                else -> RootScreen.MAIN
            }
        )
    }

    when (screen) {
        RootScreen.AUTH -> AuthScreen(
            startInLoginMode = prefs.isRegistered,
            onAuthenticated = {
                screen = if (prefs.isOnboarded) RootScreen.MAIN else RootScreen.INTRO
            }
        )
        RootScreen.INTRO -> IntroScreen(onContinue = { screen = RootScreen.ONBOARDING })
        RootScreen.ONBOARDING -> OnboardingScreen(onComplete = { screen = RootScreen.LIFESTYLE })
        RootScreen.LIFESTYLE -> LifestyleQuestionsScreen(onComplete = { screen = RootScreen.PLAN_SETUP })
        RootScreen.PLAN_SETUP -> DayPlanScreen(
            onBack = {},
            isSetupFlow = true,
            onSetupComplete = { screen = RootScreen.MAIN }
        )
        RootScreen.MAIN -> AppRoot(
            timerViewModel = timerViewModel,
            onLogout = {
                prefs.isLoggedIn = false
                screen = RootScreen.AUTH
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(timerViewModel: TimerViewModel, onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Таймер", "История", "Статистика", "Настройки", "Профиль")

    var overlayScreen by remember { mutableStateOf<OverlayScreen?>(null) }
    var showAddMenu by remember { mutableStateOf(false) }

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
        topBar = {
            TopAppBar(
                title = { Text("EPV") },
                actions = {
                    IconButton(onClick = { showAddMenu = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить")
                    }
                    DropdownMenu(expanded = showAddMenu, onDismissRequest = { showAddMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Создать план на день") },
                            onClick = {
                                showAddMenu = false
                                overlayScreen = OverlayScreen.DAY_PLAN
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Утренняя рутина") },
                            onClick = {
                                showAddMenu = false
                                overlayScreen = OverlayScreen.ROUTINE
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; overlayScreen = null },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = tabs[0]) },
                    label = { Text(tabs[0]) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; overlayScreen = null },
                    icon = { Icon(Icons.Default.History, contentDescription = tabs[1]) },
                    label = { Text(tabs[1]) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2; overlayScreen = null },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = tabs[2]) },
                    label = { Text(tabs[2]) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3; overlayScreen = null },
                    icon = { Icon(Icons.Default.Settings, contentDescription = tabs[3]) },
                    label = { Text(tabs[3]) }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4; overlayScreen = null },
                    icon = { Icon(Icons.Default.Person, contentDescription = tabs[4]) },
                    label = { Text(tabs[4]) }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val phaseFresh = timerState.secondsLeft == (if (timerState.phase == TimerPhase.WORK) timerState.workMinutes else timerState.restMinutes) * 60
            val showMiniTimer = timerState.isRunning || !phaseFresh
            if (showMiniTimer) {
                MiniTimerBar(
                    timerState = timerState,
                    onClick = { selectedTab = 0; overlayScreen = null }
                )
            }
            when (overlayScreen) {
                OverlayScreen.DAY_PLAN -> DayPlanScreen(onBack = { overlayScreen = null })
                OverlayScreen.ROUTINE -> RoutineScreen(onBack = { overlayScreen = null })
                null -> when (selectedTab) {
                    0 -> TimerScreen(timerViewModel)
                    1 -> HistoryScreen()
                    2 -> StatsScreen()
                    3 -> SettingsScreen(onLogout = onLogout)
                    4 -> ProfileScreen()
                }
            }
        }
    }
}

@Composable
private fun MiniTimerBar(timerState: TimerUiState, onClick: () -> Unit) {
    val phaseColor = if (timerState.phase == TimerPhase.WORK) WorkColor else RestColor
    val phaseLabel = if (timerState.phase == TimerPhase.WORK) "Работа" else "Отдых"
    val minutes = timerState.secondsLeft / 60
    val seconds = timerState.secondsLeft % 60
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(phaseColor.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (timerState.isRunning) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = null,
                tint = phaseColor
            )
            Text(
                phaseLabel,
                color = phaseColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Text(
            "%02d:%02d".format(minutes, seconds),
            color = phaseColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
