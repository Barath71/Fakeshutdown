package com.example.fakeapp;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_SESSION_COUNT = "session_count";
    private static final int MAX_SESSION_COUNT = 10;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Save user ID and reset session count to 0
    public void saveUserId(int userId) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putInt(KEY_SESSION_COUNT, 0); // Reset count on login
        editor.apply();
    }

    // Get the stored user ID
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    // Check if user is still in valid session
    public boolean isSessionValid() {
        int userId = getUserId();
        int count = prefs.getInt(KEY_SESSION_COUNT, 0);

        if (userId != -1 && count < MAX_SESSION_COUNT) {
            // Increase session count
            editor.putInt(KEY_SESSION_COUNT, count + 1);
            editor.apply();
            return true;
        } else {
            return false; // Don't clear here
        }
    }


    // Clear all saved session data
    public void clear() {
        editor.clear().apply();
    }
}
