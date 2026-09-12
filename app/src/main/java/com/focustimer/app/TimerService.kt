package com.focustimer.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.RingtoneManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

enum class TimerPhase { WORK, REST }

data class TimerUiState(
    val phase: TimerPhase = TimerPhase.WORK,
    val isRunning: Boolean = false,
    val secondsLeft: Int = 0,
    val workMinutes: Int = 25,
    val restMinutes: Int = 5,
    val escalationActive: Boolean = false
)

/**
 * Runs the work/rest countdown as a foreground service so it keeps going (and can escalate a
 * missed phase change) even while the app is backgrounded or the task is swiped away.
 */
class TimerService : Service() {

    inner class LocalBinder : Binder() {
        val service: TimerService get() = this@TimerService
    }

    private val binder = LocalBinder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var prefs: PrefsManager

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> get() = _uiState

    private var timerJob: Job? = null
    private var graceJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        prefs = PrefsManager(applicationContext)
        _uiState.update {
            it.copy(
                workMinutes = prefs.workMinutes,
                restMinutes = prefs.restMinutes,
                secondsLeft = prefs.workMinutes * 60
            )
        }
        createNotificationChannels()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_ACKNOWLEDGE) {
            acknowledge()
        }
        return START_STICKY
    }

    fun setWorkMinutes(minutes: Int) {
        prefs.workMinutes = minutes
        _uiState.update {
            if (!it.isRunning && it.phase == TimerPhase.WORK) {
                it.copy(workMinutes = minutes, secondsLeft = minutes * 60)
            } else {
                it.copy(workMinutes = minutes)
            }
        }
    }

    fun setRestMinutes(minutes: Int) {
        prefs.restMinutes = minutes
        _uiState.update {
            if (!it.isRunning && it.phase == TimerPhase.REST) {
                it.copy(restMinutes = minutes, secondsLeft = minutes * 60)
            } else {
                it.copy(restMinutes = minutes)
            }
        }
    }

    fun start() {
        if (_uiState.value.isRunning) return
        startForegroundCompat(buildOngoingNotification())
        _uiState.update { it.copy(isRunning = true) }
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_uiState.value.secondsLeft > 0) {
                delay(1000)
                _uiState.update { it.copy(secondsLeft = it.secondsLeft - 1) }
                updateOngoingNotification()
            }
            onPhaseFinished()
        }
    }

    fun pause() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
        updateOngoingNotification()
    }

    fun stop() {
        timerJob?.cancel()
        graceJob?.cancel()
        graceJob = null
        val minutes = _uiState.value.workMinutes
        _uiState.update {
            it.copy(isRunning = false, phase = TimerPhase.WORK, secondsLeft = minutes * 60, escalationActive = false)
        }
        clearNotifications()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun acknowledge() {
        graceJob?.cancel()
        graceJob = null
        if (_uiState.value.escalationActive) {
            _uiState.update { it.copy(escalationActive = false) }
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.cancel(NOTIF_ID_PHASE)
        nm.cancel(NOTIF_ID_ESCALATION)
    }

    private fun onPhaseFinished() {
        alertUser()
        val nextPhase = if (_uiState.value.phase == TimerPhase.WORK) TimerPhase.REST else TimerPhase.WORK
        val minutes = if (nextPhase == TimerPhase.WORK) _uiState.value.workMinutes else _uiState.value.restMinutes
        _uiState.update { it.copy(phase = nextPhase, secondsLeft = minutes * 60, isRunning = false) }
        notifyPhaseChanged(nextPhase)
        armGraceTimer()
        start()
    }

    private fun armGraceTimer() {
        graceJob?.cancel()
        graceJob = scope.launch {
            delay(MISSED_WINDOW_GRACE_MS)
            escalate()
        }
    }

    private fun escalate() {
        _uiState.update { it.copy(escalationActive = true) }
        showEscalationNotification()
        if (prefs.vibrationEnabled) vibrateNow()

        if (prefs.telegramEnabled && prefs.telegramBotToken.isNotBlank() && prefs.telegramChatId.isNotBlank()) {
            val phaseLabel = if (_uiState.value.phase == TimerPhase.WORK) "работа" else "отдых"
            sendTelegramMessage(
                "⏰ Focus Timer: окно \"$phaseLabel\" пропущено — приложение не открыли вовремя."
            )
        }

        if (prefs.autoCallEnabled && prefs.autoCallNumber.isNotBlank()) {
            placeAutoCall(prefs.autoCallNumber)
        }
    }

    private fun placeAutoCall(number: String) {
        val hasPermission = ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(number)}")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            startActivity(callIntent)
        } catch (_: Exception) {
        }
    }

    private fun sendTelegramMessage(text: String) {
        val token = prefs.telegramBotToken
        val chatId = prefs.telegramChatId
        scope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://api.telegram.org/bot$token/sendMessage")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                val body = "chat_id=${URLEncoder.encode(chatId, "UTF-8")}&text=${URLEncoder.encode(text, "UTF-8")}"
                OutputStreamWriter(connection.outputStream).use { it.write(body) }
                connection.responseCode
                connection.disconnect()
            } catch (_: Exception) {
            }
        }
    }

    private fun alertUser() {
        if (prefs.vibrationEnabled) vibrateNow()
        if (prefs.soundEnabled) {
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                RingtoneManager.getRingtone(applicationContext, uri)?.play()
            } catch (_: Exception) {
            }
        }
    }

    private fun vibrateNow() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID_ONGOING, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID_ONGOING, notification)
        }
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ONGOING, "Таймер", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Текущий отсчёт времени"
                setShowBadge(false)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_PHASE, "Смена этапа", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Работа/отдых начались"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ESCALATION, "Пропущенное окно", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Окно таймера пропущено"
            }
        )
    }

    private fun openAppPendingIntent(): PendingIntent {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
            ?: Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun acknowledgePendingIntent(requestCode: Int): PendingIntent {
        val intent = Intent(this, TimerService::class.java).apply { action = ACTION_ACKNOWLEDGE }
        return PendingIntent.getService(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildOngoingNotification(): Notification {
        val state = _uiState.value
        val phaseLabel = if (state.phase == TimerPhase.WORK) "Работа" else "Отдых"
        val minutes = state.secondsLeft / 60
        val seconds = state.secondsLeft % 60
        val timeText = "%02d:%02d".format(minutes, seconds)
        return NotificationCompat.Builder(this, CHANNEL_ONGOING)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("$phaseLabel — $timeText")
            .setContentText(if (state.isRunning) "Таймер идёт" else "На паузе")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppPendingIntent())
            .build()
    }

    private fun updateOngoingNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID_ONGOING, buildOngoingNotification())
    }

    private fun notifyPhaseChanged(phase: TimerPhase) {
        val label = if (phase == TimerPhase.WORK) "Работа" else "Отдых"
        val notification = NotificationCompat.Builder(this, CHANNEL_PHASE)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("$label началось")
            .setContentText("Откройте приложение или нажмите \"Я тут\" — иначе через минуту придёт напоминание")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent())
            .addAction(0, "Я тут", acknowledgePendingIntent(1))
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID_PHASE, notification)
    }

    private fun showEscalationNotification() {
        val phaseLabel = if (_uiState.value.phase == TimerPhase.WORK) "Работа" else "Отдых"
        val notification = NotificationCompat.Builder(this, CHANNEL_ESCALATION)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("Окно \"$phaseLabel\" пропущено!")
            .setContentText("Вы не отреагировали вовремя. Нажмите, чтобы открыть таймер.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent())
            .addAction(0, "Я тут", acknowledgePendingIntent(2))
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID_ESCALATION, notification)
    }

    private fun clearNotifications() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.cancel(NOTIF_ID_PHASE)
        nm.cancel(NOTIF_ID_ESCALATION)
    }

    override fun onDestroy() {
        timerJob?.cancel()
        graceJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ONGOING = "timer_ongoing"
        const val CHANNEL_PHASE = "timer_phase_change"
        const val CHANNEL_ESCALATION = "timer_escalation"
        const val NOTIF_ID_ONGOING = 1
        const val NOTIF_ID_PHASE = 2
        const val NOTIF_ID_ESCALATION = 3
        const val ACTION_ACKNOWLEDGE = "com.focustimer.app.ACTION_ACKNOWLEDGE"
        const val MISSED_WINDOW_GRACE_MS = 60_000L
    }
}
