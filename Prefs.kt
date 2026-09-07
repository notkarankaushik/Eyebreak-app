package com.karan.eyebreak

import android.content.Context

object Prefs {
    private const val NAME = "eyebreak_prefs"
    private const val KEY_INTERVAL_MIN = "interval_minutes"
    private const val KEY_IDLE_MIN = "idle_minutes"

    fun getIntervalMinutes(context: Context): Int {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getInt(KEY_INTERVAL_MIN, 20)
    }

    fun setIntervalMinutes(context: Context, minutes: Int) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_INTERVAL_MIN, minutes).apply()
    }

    fun getIdleMinutes(context: Context): Int {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getInt(KEY_IDLE_MIN, 3)
    }

    fun setIdleMinutes(context: Context, minutes: Int) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_IDLE_MIN, minutes).apply()
    }
}

/**
 * In-memory tracker of the last time the user actually touched/interacted
 * with the device, updated by EyeBreakAccessibilityService.
 * Plain in-process object — both services live in the same app process,
 * so this is safe without IPC.
 */
object InteractionTracker {
    @Volatile
    var lastInteractionMillis: Long = System.currentTimeMillis()
}
