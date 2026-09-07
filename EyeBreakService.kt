package com.karan.eyebreak

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat

class EyeBreakService : Service() {

    private var countDownTimer: CountDownTimer? = null
    private val idleCheckHandler = Handler(Looper.getMainLooper())
    private var idleCheckRunnable: Runnable? = null

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    InteractionTracker.lastInteractionMillis = System.currentTimeMillis()
                    startCountdown()
                    startIdleWatcher()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    stopCountdown()
                    stopIdleWatcher()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID_PERSISTENT, buildPersistentNotification())
        // Screen is presumably on right now if the user just tapped "start"
        startCountdown()
        startIdleWatcher()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCountdown()
        stopIdleWatcher()
        unregisterReceiver(screenStateReceiver)
    }

    override fun onBind(intent: Intent?) = null

    private fun startCountdown() {
        stopCountdown()
        val intervalMillis = Prefs.getIntervalMinutes(this) * 60_000L
        countDownTimer = object : CountDownTimer(intervalMillis, intervalMillis) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                triggerBreakAlert()
                startCountdown() // loop again while screen stays on
            }
        }.start()
    }

    private fun stopCountdown() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun startIdleWatcher() {
        stopIdleWatcher()
        val idleThresholdMillis = Prefs.getIdleMinutes(this) * 60_000L
        idleCheckRunnable = object : Runnable {
            override fun run() {
                val sinceLastInteraction = System.currentTimeMillis() - InteractionTracker.lastInteractionMillis
                if (sinceLastInteraction >= idleThresholdMillis) {
                    // User has gone idle with screen on (e.g. reading/video) — reset the break clock
                    startCountdown()
                }
                idleCheckHandler.postDelayed(this, 30_000L) // check every 30s
            }
        }
        idleCheckHandler.postDelayed(idleCheckRunnable!!, 30_000L)
    }

    private fun stopIdleWatcher() {
        idleCheckRunnable?.let { idleCheckHandler.removeCallbacks(it) }
        idleCheckRunnable = null
    }

    private fun triggerBreakAlert() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_ALERT)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("Eye break time")
            .setContentText("Look at something 20 feet away for 20 seconds.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .build()
        nm.notify(NOTIF_ID_ALERT, notification)
    }

    private fun buildPersistentNotification() =
        NotificationCompat.Builder(this, CHANNEL_ID_PERSISTENT)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("Eye Break running")
            .setContentText("Tracking screen time for 20-20-20 reminders")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_PERSISTENT, "Eye Break Status",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_ALERT, "Eye Break Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }

    companion object {
        const val CHANNEL_ID_PERSISTENT = "eyebreak_persistent"
        const val CHANNEL_ID_ALERT = "eyebreak_alert"
        const val NOTIF_ID_PERSISTENT = 1
        const val NOTIF_ID_ALERT = 2
    }
}
