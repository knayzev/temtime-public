package com.focustimer.app

import android.content.Context

class PrefsManager(context: Context) {
    private val prefs = context.getSharedPreferences("focus_timer_prefs", Context.MODE_PRIVATE)

    var userName: String
        get() = prefs.getString(KEY_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_NAME, value).apply()

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

    companion object {
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
    }
}
