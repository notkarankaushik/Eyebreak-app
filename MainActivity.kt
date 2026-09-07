package com.karan.eyebreak

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var intervalInput: EditText
    private lateinit var idleInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        intervalInput = findViewById(R.id.intervalInput)
        idleInput = findViewById(R.id.idleInput)

        intervalInput.setText(Prefs.getIntervalMinutes(this).toString())
        idleInput.setText(Prefs.getIdleMinutes(this).toString())

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            val interval = intervalInput.text.toString().toIntOrNull() ?: 20
            val idle = idleInput.text.toString().toIntOrNull() ?: 3
            Prefs.setIntervalMinutes(this, interval)
            Prefs.setIdleMinutes(this, idle)
            Toast.makeText(this, "Saved. Restart the service to apply.", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.accessibilityButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(
                this,
                "Find 'Eye Break' in the list and enable it.",
                Toast.LENGTH_LONG
            ).show()
        }

        findViewById<Button>(R.id.startButton).setOnClickListener {
            if (!isAccessibilityServiceEnabled()) {
                Toast.makeText(
                    this,
                    "Enable the Accessibility permission first (step 1), or idle-reset won't work.",
                    Toast.LENGTH_LONG
                ).show()
            }
            val serviceIntent = Intent(this, EyeBreakService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
            statusText.text = "Service: running"
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            stopService(Intent(this, EyeBreakService::class.java))
            statusText.text = "Service: stopped"
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponent = "$packageName/${EyeBreakAccessibilityService::class.java.name}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(":").any { it.equals(expectedComponent, ignoreCase = true) }
    }
}
