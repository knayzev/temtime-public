package com.focustimer.app

import android.app.Application
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TimerPhase { WORK, REST }

data class TimerUiState(
    val phase: TimerPhase = TimerPhase.WORK,
    val isRunning: Boolean = false,
    val secondsLeft: Int = 0,
    val workMinutes: Int = 25,
    val restMinutes: Int = 5
)

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PrefsManager(application)

    private val _uiState = MutableStateFlow(
        TimerUiState(
            workMinutes = prefs.workMinutes,
            restMinutes = prefs.restMinutes,
            secondsLeft = prefs.workMinutes * 60
        )
    )
    val uiState: StateFlow<TimerUiState> = _uiState

    private var timerJob: Job? = null

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
        _uiState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.secondsLeft > 0) {
                delay(1000)
                _uiState.update { it.copy(secondsLeft = it.secondsLeft - 1) }
            }
            onPhaseFinished()
        }
    }

    fun pause() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun stop() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(isRunning = false, phase = TimerPhase.WORK, secondsLeft = it.workMinutes * 60)
        }
    }

    private fun onPhaseFinished() {
        alertUser()
        _uiState.update {
            val nextPhase = if (it.phase == TimerPhase.WORK) TimerPhase.REST else TimerPhase.WORK
            val minutes = if (nextPhase == TimerPhase.WORK) it.workMinutes else it.restMinutes
            it.copy(phase = nextPhase, secondsLeft = minutes * 60, isRunning = false)
        }
        start()
    }

    private fun alertUser() {
        val context = getApplication<Application>()
        if (prefs.vibrationEnabled) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        }
        if (prefs.soundEnabled) {
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                RingtoneManager.getRingtone(context, uri)?.play()
            } catch (_: Exception) {
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
