package de.beusterse.abfalllro.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;

import de.beusterse.abfalllro.BuildConfig;
import de.beusterse.abfalllro.R;

/**
 * Migrates user settings across versions
 *
 * Created by Felix Beuster on 1/6/2018.
 */

public class MigrationController {

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public MigrationController(Context context) {
        mContext = context;
    }

    public void migrate() {
        mSharedPreferences  = PreferenceManager.getDefaultSharedPreferences(mContext);
        mEditor             = mSharedPreferences.edit();

        int migratedVersion = mSharedPreferences.getInt(
                mContext.getString(R.string.pref_key_intern_migrated_version),
                mContext.getResources().getInteger(R.integer.start_migrated_version));

        if (migratedVersion < BuildConfig.VERSION_CODE) {
            while (migratedVersion < BuildConfig.VERSION_CODE) {
                if (migratedVersion == 16) {
                    migrateTo17();
                }

                if (migratedVersion == 17) {
                    migrateTo18();
                }

                if (migratedVersion == 18) {
                    migrateTo19();
                }

                migratedVersion++;
            }
        }

        mEditor.putInt(mContext.getString(R.string.pref_key_intern_migrated_version), migratedVersion);
        mEditor.apply();
    }

    private void migrateTo17() {
        String schedule_biweekly    = mContext.getString(R.string.pref_can_schedule_biweekly);
        String schedule_monthly     = mContext.getString(R.string.pref_can_schedule_monthly);

        String key_schedule_black   = mContext.getString(R.string.pref_key_pickup_schedule_black);
        String key_schedule_green   = mContext.getString(R.string.pref_key_pickup_schedule_green);

        boolean bool_schedule_black = mSharedPreferences.getBoolean(key_schedule_black, false);
        boolean bool_schedule_green = mSharedPreferences.getBoolean(key_schedule_green, false);

        String string_schedule_black = bool_schedule_black ? schedule_monthly : schedule_biweekly;
        String string_schedule_green = bool_schedule_green ? schedule_monthly : schedule_biweekly;

        mEditor.putString(mContext.getString(R.string.pref_key_pickup_schedule_black), string_schedule_black);
        mEditor.putString(mContext.getString(R.string.pref_key_pickup_schedule_green), string_schedule_green);
        mEditor.putString(mContext.getString(R.string.pref_key_pickup_schedule_yellow), schedule_biweekly);
    }

    private void migrateTo18() {
        String schedule_biweekly = mContext.getString(R.string.pref_can_schedule_biweekly);
        mEditor.putString(mContext.getString(R.string.pref_key_pickup_schedule_yellow), schedule_biweekly);
    }

    private void migrateTo19() {
        String schedule_biweekly    = mContext.getString(R.string.pref_can_schedule_biweekly);
        String schedule_monthly     = mContext.getString(R.string.pref_can_schedule_monthly);

        String key_black            = mContext.getString(R.string.pref_key_pickup_schedule_black);
        String key_green            = mContext.getString(R.string.pref_key_pickup_schedule_black);

        Map<String, ?> prefs        = mSharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : prefs.entrySet()) {
            if (entry.getKey().equals(key_black) && entry.getValue().getClass().equals(Boolean.class)) {
                boolean monthly = mSharedPreferences.getBoolean(key_black, false);

                mEditor.putString(key_black, monthly ? schedule_monthly : schedule_biweekly);
            }

            if (entry.getKey().equals(key_green) && entry.getValue().getClass().equals(Boolean.class)) {
                boolean monthly = mSharedPreferences.getBoolean(key_green, false);

                mEditor.putString(key_black, monthly ? schedule_monthly : schedule_biweekly);
            }

            if (entry.getKey().equals(key_black) && entry.getValue().getClass().equals(Integer.class)) {
                mEditor.putString(key_black, schedule_monthly);
            }

            if (entry.getKey().equals(key_green) && entry.getValue().getClass().equals(Integer.class)) {
                mEditor.putString(key_black, schedule_monthly);
            }
        }
    }
}
