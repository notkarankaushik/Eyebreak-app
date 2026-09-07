package com.karan.eyebreak

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // v1: does not auto-start after reboot. You'll need to reopen
            // the app and tap "Start Eye Break Service" once after a restart.
            // Wire this up later if that friction bothers you.
        }
    }
}
