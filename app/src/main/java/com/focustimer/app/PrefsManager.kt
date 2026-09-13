package com.focustimer.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SessionRecord(
    val id: Long,
    val phase: String,
    val startTimeMillis: Long,
    val durationSeconds: Int,
    val interrupted: Boolean,
    val comment: String
)

class PrefsManager(context: Context) {
    private val prefs = context.getSharedPreferences("focus_timer_prefs", Context.MODE_PRIVATE)

    var userName: String
        get() = prefs.getString(KEY_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_NAME, value).apply()

    var lastName: String
        get() = prefs.getString(KEY_LAST_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_NAME, value).apply()

    var dataConsentGiven: Boolean
        get() = prefs.getBoolean(KEY_DATA_CONSENT, false)
        set(value) = prefs.edit().putBoolean(KEY_DATA_CONSENT, value).apply()

    var stepsEnabled: Boolean
        get() = prefs.getBoolean(KEY_STEPS_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_STEPS_ENABLED, value).apply()

    var photoUri: String?
        get() = prefs.getString(KEY_PHOTO, null)
        set(value) = prefs.edit().putString(KEY_PHOTO, value).apply()

    var workMinutes: Int
        get() = prefs.getInt(KEY_WORK_MIN, 25)
        set(value) = prefs.edit().putInt(KEY_WORK_MIN, value).apply()

    var restMinutes: Int
        get() = prefs.getInt(KEY_REST_MIN, 5)
        set(value) = prefs.edit().putInt(KEY_REST_MIN, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION, value).apply()

    var keepScreenOn: Boolean
        get() = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true)
        set(value) = prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, value).apply()

    var telegramEnabled: Boolean
        get() = prefs.getBoolean(KEY_TG_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_TG_ENABLED, value).apply()

    var telegramBotToken: String
        get() = prefs.getString(KEY_TG_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TG_TOKEN, value).apply()

    var telegramChatId: String
        get() = prefs.getString(KEY_TG_CHAT, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TG_CHAT, value).apply()

    var autoCallEnabled: Boolean
        get() = prefs.getBoolean(KEY_CALL_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_CALL_ENABLED, value).apply()

    var autoCallNumber: String
        get() = prefs.getString(KEY_CALL_NUMBER, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CALL_NUMBER, value).apply()

    var email: String
        get() = prefs.getString(KEY_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()

    var weightKg: String
        get() = prefs.getString(KEY_WEIGHT, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEIGHT, value).apply()

    var heightCm: String
        get() = prefs.getString(KEY_HEIGHT, "") ?: ""
        set(value) = prefs.edit().putString(KEY_HEIGHT, value).apply()

    var age: String
        get() = prefs.getString(KEY_AGE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_AGE, value).apply()

    var maritalStatus: String
        get() = prefs.getString(KEY_MARITAL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_MARITAL, value).apply()

    var wakeTime: String
        get() = prefs.getString(KEY_WAKE_TIME, "07:00") ?: "07:00"
        set(value) = prefs.edit().putString(KEY_WAKE_TIME, value).apply()

    var bedTime: String
        get() = prefs.getString(KEY_BED_TIME, "23:00") ?: "23:00"
        set(value) = prefs.edit().putString(KEY_BED_TIME, value).apply()

    var isWorking: Boolean
        get() = prefs.getBoolean(KEY_IS_WORKING, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_WORKING, value).apply()

    fun getHistory(): List<SessionRecord> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                SessionRecord(
                    id = obj.getLong("id"),
                    phase = obj.getString("phase"),
                    startTimeMillis = obj.getLong("startTimeMillis"),
                    durationSeconds = obj.getInt("durationSeconds"),
                    interrupted = obj.getBoolean("interrupted"),
                    comment = obj.optString("comment", "")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addHistoryEntry(entry: SessionRecord) {
        val updated = listOf(entry) + getHistory()
        saveHistory(updated.take(MAX_HISTORY_ENTRIES))
    }

    fun updateHistoryComment(id: Long, comment: String) {
        val updated = getHistory().map { if (it.id == id) it.copy(comment = comment) else it }
        saveHistory(updated)
    }

    private fun saveHistory(entries: List<SessionRecord>) {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject().apply {
                    put("id", entry.id)
                    put("phase", entry.phase)
                    put("startTimeMillis", entry.startTimeMillis)
                    put("durationSeconds", entry.durationSeconds)
                    put("interrupted", entry.interrupted)
                    put("comment", entry.comment)
                }
            )
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    companion object {
        private const val MAX_HISTORY_ENTRIES = 300
        private const val KEY_NAME = "user_name"
        private const val KEY_PHOTO = "photo_uri"
        private const val KEY_WORK_MIN = "work_minutes"
        private const val KEY_REST_MIN = "rest_minutes"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_TG_ENABLED = "telegram_enabled"
        private const val KEY_TG_TOKEN = "telegram_bot_token"
        private const val KEY_TG_CHAT = "telegram_chat_id"
        private const val KEY_CALL_ENABLED = "auto_call_enabled"
        private const val KEY_CALL_NUMBER = "auto_call_number"
        private const val KEY_EMAIL = "email"
        private const val KEY_WEIGHT = "weight_kg"
        private const val KEY_HEIGHT = "height_cm"
        private const val KEY_AGE = "age"
        private const val KEY_MARITAL = "marital_status"
        private const val KEY_WAKE_TIME = "wake_time"
        private const val KEY_BED_TIME = "bed_time"
        private const val KEY_IS_WORKING = "is_working"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_DATA_CONSENT = "data_consent_given"
        private const val KEY_STEPS_ENABLED = "steps_enabled"
        private const val KEY_HISTORY = "session_history"
    }
}
