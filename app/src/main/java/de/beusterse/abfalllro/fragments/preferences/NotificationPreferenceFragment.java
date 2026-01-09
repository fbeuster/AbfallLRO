package de.beusterse.abfalllro.fragments.preferences;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.utils.NotificationUtils;
import de.beusterse.abfalllro.utils.TimePreference;
import de.beusterse.abfalllro.utils.TimePreferenceDaalogFragmentCompat;

public class NotificationPreferenceFragment extends ReturnPreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_notifications, rootKey);
        setHasOptionsMenu(true);

        SwitchPreferenceCompat active = findPreference(getString(R.string.pref_key_notifications_active));
        TimePreference time = findPreference(getString(R.string.pref_key_notifications_time));

        updatePreferencesEnabled(active.isChecked());
        updateTimePreferenceSummary(time);

        active.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object enabled) {
                updatePreferencesEnabled((boolean) enabled);
                return true;
            }
        });
        time.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateTimePreferenceSummary(preference);
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Preference linkAllSettings = findPreference(getString(R.string.pref_key_notifications_all_settings));
            linkAllSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, preference.getContext().getPackageName());
                    startActivity(intent);
                    return true;
                }
            });

            Preference linkChannelSettings = findPreference(getString(R.string.pref_key_notifications_channel_settings));
            linkChannelSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, preference.getContext().getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, getString(R.string.notification_channel_id));
                    startActivity(intent);
                    return true;
                }
            });
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialog = null;

        if (preference instanceof TimePreference) {
            dialog = TimePreferenceDaalogFragmentCompat.newInstance(preference.getKey());
        }

        if (dialog != null) {
            dialog.setTargetFragment(this, 0);
            dialog.show(this.getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    private void updatePreferencesEnabled(boolean enabled) {
        Preference preference = findPreference(getString(R.string.pref_key_notifications_time));
        preference.setEnabled(enabled);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            preference = findPreference(getString(R.string.pref_key_notifications_channel_settings));
            preference.setEnabled(enabled);

            if (enabled) {
                NotificationUtils.createNotificationChannel(getContext());
            }

        } else {
            preference = findPreference(getString(R.string.pref_key_notifications_sound));
            preference.setEnabled(enabled);

            preference = findPreference(getString(R.string.pref_key_notifications_vibrate));
            preference.setEnabled(enabled);
        }
    }

    private void updateTimePreferenceSummary(Preference preference) {
        TimePreference timePreference = (TimePreference) preference;
        preference.setSummary(timePreference.getSummary());
    }
}
