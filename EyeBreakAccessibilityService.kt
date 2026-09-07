package com.karan.eyebreak

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * We don't care what the user is doing or where — we only care THAT
 * something happened, as a proxy for "device is actively in use."
 * canRetrieveWindowContent is false in the config, so this service
 * cannot read screen content, only event timestamps.
 */
class EyeBreakAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        InteractionTracker.lastInteractionMillis = System.currentTimeMillis()
    }

    override fun onInterrupt() {
        // no-op
    }
}
