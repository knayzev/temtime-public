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
import android.speech.tts.TextToSpeech
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
import java.util.Locale

enum class TimerPhase { WORK, REST }

data class TimerUiState(
    val phase: TimerPhase = TimerPhase.WORK,
    val isRunning: Boolean = false,
    val secondsLeft: Int = 0,
    val workMinutes: Int = 25,
    val restMinutes: Int = 5,
    val escalationActive: Boolean = false,
    val currentComment: String = "",
    val currentCategory: String = "",
    val motivationQuote: String? = null
)

val MOTIVATIONAL_QUOTES = listOf(
    "Успех — это способность идти от одной неудачи к другой, не теряя энтузиазма. — Уинстон Черчилль",
    "Единственный способ сделать великую работу — любить то, что ты делаешь. — Стив Джобс",
    "Не бойтесь совершенства — вам его не достичь. — Сальвадор Дали",
    "Дисциплина — это мост между целями и результатом. — Джим Рон",
    "Я не терпел неудачу. Я просто нашёл 10 000 способов, которые не работают. — Томас Эдисон",
    "Секрет продвижения вперёд — начать. — Марк Твен",
    "Тяжело в учении — легко в бою. — Александр Суворов",
    "Будущее принадлежит тем, кто верит в красоту своей мечты. — Элеонора Рузвельт",
    "Маленькие ежедневные улучшения со временем дают потрясающие результаты. — Робин Шарма",
    "Ты никогда не будешь готов на 100%. Начни с тем, что есть. — Наполеон Хилл",
    "Не считай дни, делай дни значимыми. — Мухаммед Али",
    "Лучшее время посадить дерево было 20 лет назад. Второе лучшее — сейчас. — китайская пословица",
    "Делай то, что можешь, с тем, что имеешь, там, где ты есть. — Теодор Рузвельт",
    "Мотивация — то, что заставляет тебя начать. Привычка — то, что заставляет продолжать. — Джим Рон",
    "Единственный, кто может остановить тебя, — это ты сам. — неизвестный автор"
)

val WORK_DONE_PHRASES_RU = listOf(
    "Пора отдыхать! Выпейте чашку кофе или чая.",
    "Работа завершена. Самое время немного отдохнуть.",
    "Отличная работа! Теперь можно расслабиться и передохнуть.",
    "Время отдыха началось. Встаньте, разомнитесь, подышите свежим воздухом.",
    "Вы молодец! Сделайте паузу — заварите чай и отдохните.",
    "Рабочий блок завершён. Дайте глазам и телу отдохнуть.",
    "Пора сделать перерыв. Прогуляйтесь или выпейте воды.",
    "Работа окончена — насладитесь заслуженным отдыхом.",
    "Отлично поработали! Теперь немного расслабьтесь.",
    "Время выдохнуть. Отдых начался — используйте его с пользой."
)

val REST_DONE_PHRASES_RU = listOf(
    "Пора работать! Желаю удачи — всё получится.",
    "Отдых завершён. Приступим к делу с новыми силами.",
    "Время снова сосредоточиться. У вас точно получится!",
    "Перерыв окончен. Вперёд, к новым результатам!",
    "Пора возвращаться к работе. Вы справитесь!",
    "Отдохнули — теперь за дело! Удачи вам.",
    "Рабочее время началось. Сфокусируйтесь и действуйте.",
    "Время продуктивности! Начинаем работать.",
    "Перерыв закончен — покажите, на что способны!",
    "Снова в бой! Желаю продуктивной работы."
)

val WORK_DONE_PHRASES_EN = listOf(
    "Time to rest! Grab a cup of coffee or tea.",
    "Work session complete. Time for a well-deserved break.",
    "Great job! Now relax and recharge for a bit.",
    "Rest time has started. Stand up, stretch, get some fresh air.",
    "Well done! Take a break — brew some tea and unwind.",
    "Work block finished. Give your eyes and body a rest.",
    "Time for a break. Take a walk or drink some water.",
    "Work is done — enjoy your well-earned rest.",
    "Great work! Time to relax a little.",
    "Time to breathe out. Rest has begun — make the most of it."
)

val REST_DONE_PHRASES_EN = listOf(
    "Time to work! Good luck — you've got this.",
    "Break's over. Let's get back to it with fresh energy.",
    "Time to focus again. You can definitely do this!",
    "Break is over. Onward to new results!",
    "Time to get back to work. You'll manage just fine!",
    "Rested up — now let's get to it! Good luck.",
    "Work time has started. Focus and take action.",
    "Time to be productive! Let's start working.",
    "Break's over — show what you're capable of!",
    "Back into it! Wishing you a productive work session."
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
    private var sessionStartMillis: Long? = null
    private var announcedThisPhase = false
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var nextVoiceFemale = true

    override fun onCreate() {
        super.onCreate()
        prefs = PrefsManager(applicationContext)
        _uiState.update {
            it.copy(
                workMinutes = prefs.workMinutes,
                restMinutes = prefs.restMinutes,
                secondsLeft = prefs.workMinutes * 60,
                currentCategory = prefs.categories.firstOrNull() ?: ""
            )
        }
        createNotificationChannels()
        tts = TextToSpeech(applicationContext) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
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

    fun setComment(text: String) {
        _uiState.update { it.copy(currentComment = text) }
    }

    fun setCategory(category: String) {
        _uiState.update { it.copy(currentCategory = category) }
    }

    fun dismissQuote() {
        _uiState.update { it.copy(motivationQuote = null) }
    }

    fun start() {
        if (_uiState.value.isRunning) return
        if (sessionStartMillis == null) {
            sessionStartMillis = System.currentTimeMillis()
        }
        startForegroundCompat(buildOngoingNotification())
        _uiState.update { it.copy(isRunning = true) }
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_uiState.value.secondsLeft > 0) {
                delay(1000)
                _uiState.update { it.copy(secondsLeft = it.secondsLeft - 1) }
                updateOngoingNotification()
                maybeAnnounceUpcomingPhase()
            }
            onPhaseFinished()
        }
    }

    private fun maybeAnnounceUpcomingPhase() {
        if (announcedThisPhase || !prefs.voiceAnnounceEnabled) return
        val lead = prefs.voiceAnnounceLeadSeconds()
        if (lead <= 0) return
        if (_uiState.value.secondsLeft <= lead) {
            announcedThisPhase = true
            val current = _uiState.value.phase
            val english = prefs.voiceLanguage == "English"
            val text = if (english) {
                if (current == TimerPhase.WORK) "Rest time is approaching" else "Work time is approaching"
            } else {
                if (current == TimerPhase.WORK) "Приближается время отдыха" else "Приближается время работы"
            }
            speak(text)
        }
    }

    private fun currentVoiceLocale(): Locale =
        if (prefs.voiceLanguage == "English") Locale.US else Locale("ru")

    private fun speak(text: String) {
        val engine = tts ?: return
        if (!ttsReady) return

        val locale = currentVoiceLocale()
        engine.setLanguage(locale)

        val female = nextVoiceFemale
        nextVoiceFemale = !nextVoiceFemale

        val voice = pickVoice(engine, locale, female)
        if (voice != null) {
            engine.setVoice(voice)
        }
        // Even when we can't reliably tell voices apart by gender, a pitch shift keeps the
        // alternation audible and pushes the flat default voice a bit further from "robotic".
        engine.setPitch(if (female) 1.12f else 0.86f)
        engine.setSpeechRate(1.0f)

        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "focus_timer_announce")
    }

    /**
     * Best-effort pick of a natural, gendered voice. Voice availability/naming/quality varies by
     * device and TTS engine, so this quietly falls back to the engine default when no match is
     * found — it never fails the TTS setup. Prefers the highest-quality (typically network/
     * WaveNet-style) voices over the flatter on-device ones.
     */
    private fun pickVoice(engine: TextToSpeech, locale: Locale, female: Boolean): android.speech.tts.Voice? {
        val voices = engine.voices ?: return null
        val candidates = voices
            .filter { it.locale.language == locale.language }
            .sortedByDescending { it.quality }
        if (candidates.isEmpty()) return null

        fun matches(voice: android.speech.tts.Voice): Boolean {
            val name = voice.name.lowercase()
            return if (female) {
                name.contains("female") || Regex("x-...-f(-|$)").containsMatchIn(name)
            } else {
                (name.contains("male") && !name.contains("female")) || Regex("x-...-[dm](-|$)").containsMatchIn(name)
            }
        }

        return candidates.firstOrNull { matches(it) } ?: candidates.firstOrNull()
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
        flushHistoryEntry(interrupted = true)
        val minutes = _uiState.value.workMinutes
        announcedThisPhase = false
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
        val finishedPhase = _uiState.value.phase
        val quote = if (finishedPhase == TimerPhase.WORK) MOTIVATIONAL_QUOTES.random() else null
        flushHistoryEntry(interrupted = false, quote = quote ?: "")
        val nextPhase = if (finishedPhase == TimerPhase.WORK) TimerPhase.REST else TimerPhase.WORK
        val minutes = if (nextPhase == TimerPhase.WORK) _uiState.value.workMinutes else _uiState.value.restMinutes
        announcedThisPhase = false
        _uiState.update {
            it.copy(
                phase = nextPhase,
                secondsLeft = minutes * 60,
                isRunning = false,
                motivationQuote = quote ?: it.motivationQuote
            )
        }
        notifyPhaseChanged(nextPhase)
        armGraceTimer()
        start()
        repeatAlert(times = if (finishedPhase == TimerPhase.WORK) 3 else 1)
        if (prefs.voiceAnnounceEnabled) {
            val english = prefs.voiceLanguage == "English"
            val phrase = if (finishedPhase == TimerPhase.WORK) {
                (if (english) WORK_DONE_PHRASES_EN else WORK_DONE_PHRASES_RU).random()
            } else {
                (if (english) REST_DONE_PHRASES_EN else REST_DONE_PHRASES_RU).random()
            }
            speak(phrase)
        }
    }

    private fun repeatAlert(times: Int) {
        scope.launch {
            repeat(times) { index ->
                alertUser()
                if (index < times - 1) delay(1200)
            }
        }
    }

    private fun flushHistoryEntry(interrupted: Boolean, quote: String = "") {
        val start = sessionStartMillis ?: return
        val state = _uiState.value
        val plannedSeconds = (if (state.phase == TimerPhase.WORK) state.workMinutes else state.restMinutes) * 60
        val elapsedSeconds = if (interrupted) (plannedSeconds - state.secondsLeft).coerceAtLeast(0) else plannedSeconds
        prefs.addHistoryEntry(
            SessionRecord(
                id = start,
                phase = state.phase.name,
                startTimeMillis = start,
                durationSeconds = elapsedSeconds,
                interrupted = interrupted,
                comment = state.currentComment,
                category = state.currentCategory,
                quote = quote
            )
        )
        sessionStartMillis = null
        _uiState.update { it.copy(currentComment = "") }
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
        tts?.stop()
        tts?.shutdown()
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
