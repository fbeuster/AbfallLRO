package de.beusterse.abfalllro.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import de.beusterse.abfalllro.R;

public class AppUtils {
    private static final int DEFAULT_THEME = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

    public static void setDayNightModeFromPreference(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            int dayNightMode = Integer.parseInt(sharedPreferences.getString(context.getString(R.string.pref_key_info_theme), DEFAULT_THEME + ""));
            setDayNightMode(dayNightMode);

        } catch (Exception e) {
            setDayNightMode(DEFAULT_THEME);
        }
    }

    public static void setDayNightMode(int dayNightMode) {
        AppCompatDelegate.setDefaultNightMode(dayNightMode);
    }
}
