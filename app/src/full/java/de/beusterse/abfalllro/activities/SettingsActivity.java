package de.beusterse.abfalllro.activities;


import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.annotation.Target;
import java.util.List;

import de.beusterse.abfalllro.BuildConfig;
import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.controller.SyncController;
import de.beusterse.abfalllro.utils.ArrayUtils;
import de.beusterse.abfalllro.utils.JSONUtils;
import de.beusterse.abfalllro.utils.TimePreference;
import de.beusterse.abfalllro.interfaces.SyncCallback;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity implements SyncCallback {

    public static final String CITY_WITH_STREETS = "Güstrow";

    private SyncController mSyncController;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof TimePreference) {
                TimePreference timePreference = (TimePreference) preference;
                preference.setSummary(timePreference.getSummary());

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || InfoPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationsPreferenceFragment.class.getName().equals(fragmentName)
                || PickupPreferenceFragment.class.getName().equals(fragmentName)
                || SyncPreferenceFragment.class.getName().equals(fragmentName);
    }

    public void startManualSync() {
        mSyncController = new SyncController(this, "manual_check", this);
        mSyncController.run();
    }

    @Override
    public void syncComplete() {
        String toast;
        switch (mSyncController.getStatus()) {
            case 200:
                toast = getString(R.string.pref_sync_manual_toast_success);
                break;
            case 304:
            default:
                toast = getString(R.string.pref_sync_manual_toast_no_change);
                break;
        }
        Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
    }

    /**
     * This fragment shows pickup preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PickupPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_pickup);
            setHasOptionsMenu(true);

            SharedPreferences sp = getPreferenceManager().getSharedPreferences();
            String cityName = sp.getString(getString(R.string.pref_key_pickup_town), "");

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_pickup_street)));
            updateListPreferenceSummary(findPreference(getString(R.string.pref_key_pickup_town)), cityName);
            updateStreetLocationPref(cityName);

            ListPreference city = (ListPreference) findPreference(getString(R.string.pref_key_pickup_town));
            city.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String stringValue = newValue.toString();

                    updateListPreferenceSummary(preference, stringValue);
                    updateStreetLocationPref(stringValue);
                    updateCanSchedules(getPreferenceManager().getSharedPreferences(), newValue.toString());
                    return true;
                }
            });

            updateCanSchedules(sp, cityName);
        }

        private boolean hasLocationSchedule(String preferenceKey, String schedule, String location) {
            if (schedule.equals(getString(R.string.pref_can_schedule_twice_a_week))) {
                if (preferenceKey.equals(getString(R.string.pref_key_pickup_schedule_black)) &&
                        ArrayUtils.indexOf(getResources().getStringArray(R.array.pref_location_twice_per_week_black), location) == -1) {
                    return false;
                }

                if (preferenceKey.equals(getString(R.string.pref_key_pickup_schedule_green)) &&
                        ArrayUtils.indexOf(getResources().getStringArray(R.array.pref_location_twice_per_week_green), location) == -1) {
                    return false;
                }
            }

            if (schedule.equals(getString(R.string.pref_can_schedule_weekly))) {
                if (preferenceKey.equals(getString(R.string.pref_key_pickup_schedule_black)) &&
                        ArrayUtils.indexOf(getResources().getStringArray(R.array.pref_location_weekly_black), location) == -1) {
                    return false;
                }

                if (preferenceKey.equals(getString(R.string.pref_key_pickup_schedule_green)) &&
                        ArrayUtils.indexOf(getResources().getStringArray(R.array.pref_location_weekly_green), location) == -1) {
                    return false;
                }
            }

            return true;
        }

        private void updateCanSchedules(SharedPreferences sp, String location) {
            Preference.OnPreferenceChangeListener onCanScheduleChange = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences sp    = getPreferenceManager().getSharedPreferences();
                    String cityName         = sp.getString(getString(R.string.pref_key_pickup_town), "");
                    String toast            = getString(R.string.pref_can_schedule_not_available, newValue.toString(), cityName);

                    if (!hasLocationSchedule(preference.getKey(), newValue.toString(), cityName)) {
                        Toast.makeText(getActivity(), toast, Toast.LENGTH_LONG).show();
                        return false;
                    }

                    updateListPreferenceSummary(preference, newValue.toString());
                    return true;
                }
            };

            ListPreference black_can    = (ListPreference) findPreference(getString(R.string.pref_key_pickup_schedule_black));
            ListPreference blue_can     = (ListPreference) findPreference(getString(R.string.pref_key_pickup_schedule_blue));
            ListPreference green_can    = (ListPreference) findPreference(getString(R.string.pref_key_pickup_schedule_green));
            ListPreference yellow_can   = (ListPreference) findPreference(getString(R.string.pref_key_pickup_schedule_yellow));

            String schedule_biweekly    = getString(R.string.pref_can_schedule_biweekly);
            String schedule_monthly     = getString(R.string.pref_can_schedule_monthly);

            /*
                Reading values / defaults
             */
            String scheduleBlack    = sp.getString(
                    getString(R.string.pref_key_pickup_schedule_black), schedule_biweekly);

            String scheduleBlue     = sp.getString(
                    getString(R.string.pref_key_pickup_schedule_blue), schedule_monthly);

            String scheduleGreen    = sp.getString(
                    getString(R.string.pref_key_pickup_schedule_green), schedule_biweekly);

            String scheduleYellow   = sp.getString(
                    getString(R.string.pref_key_pickup_schedule_yellow), schedule_biweekly);


            /*
                update schedules due to availability
             */
            if (!hasLocationSchedule(black_can.getKey(), scheduleBlack, location)) {
                black_can.setValue(schedule_biweekly);
                scheduleBlack = schedule_biweekly;
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(getString(R.string.pref_key_pickup_schedule_black), schedule_biweekly);
                editor.apply();
            }

            if (!hasLocationSchedule(green_can.getKey(), scheduleGreen, location)) {
                green_can.setValue(schedule_biweekly);
                scheduleGreen = schedule_biweekly;
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(getString(R.string.pref_key_pickup_schedule_green), schedule_biweekly);
                editor.apply();
            }

            /*
                Update can schedule summaries
             */
            updateListPreferenceSummary(black_can, scheduleBlack);
            updateListPreferenceSummary(blue_can, scheduleBlue);
            updateListPreferenceSummary(green_can, scheduleGreen);
            updateListPreferenceSummary(yellow_can, scheduleYellow);

            /*
                ChangeListeners for can schedules
             */
            black_can.setOnPreferenceChangeListener(onCanScheduleChange);
            blue_can.setOnPreferenceChangeListener(onCanScheduleChange);
            green_can.setOnPreferenceChangeListener(onCanScheduleChange);
            yellow_can.setOnPreferenceChangeListener(onCanScheduleChange);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void updateListPreferenceSummary(Preference preference, String stringValue) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);
        }

        public void updateStreetLocationPref(String cityName) {
            ListPreference street = (ListPreference) findPreference(getString(R.string.pref_key_pickup_street));

            boolean needsStreet = cityName.equals(CITY_WITH_STREETS);
            street.setEnabled(needsStreet);
        }
    }

    /**
     * This fragment shows sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SyncPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_sync);
            setHasOptionsMenu(true);
            updateSyncSummary();

            /*
              Button press to manually synchronize the pickup data.
             */
            Preference syncManualButton = findPreference(getString(R.string.pref_key_sync_manual));
            syncManualButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ((SettingsActivity) getActivity()).startManualSync();
                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        public void updateSyncSummary() {
            SharedPreferences sp    = getPreferenceManager().getSharedPreferences();
            String last_check       = sp.getString(getString(R.string.pref_key_sync_last_check), "-");
            String last_update      = sp.getString(getString(R.string.pref_key_sync_last_update), last_check);

            Preference sync_summary = findPreference(getString(R.string.pref_key_sync_last_check));
            sync_summary.setSummary(getString( R.string.pref_sync_description_last_sync, last_check, last_update ));
        }
    }

    /**
     * This fragment shows information preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class InfoPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_info);
            setHasOptionsMenu(true);

            Preference pref = findPreference(getString(R.string.pref_key_info_rate));
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    final String package_name = preference.getContext().getPackageName();

                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + package_name)));

                    } catch (android.content.ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + package_name)));
                    }

                    return true;
                }
            });

            /*
              Show privacy policy
             */
            Preference privacyButton = findPreference(getString(R.string.pref_key_info_privacy));
            privacyButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    WebView webView = new WebView(getActivity());
                    webView.loadDataWithBaseURL(null, getString(R.string.pref_privacy_policy_text), "text/html", "UTF-8", null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setView(            webView);
                    builder.setTitle(           getString(R.string.pref_privacy_policy_title) );
                    builder.setNeutralButton(   getString(R.string.dialog_button_positive),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                    }
                                                });

                    final AlertDialog dialog = builder.create();

                    webView.setWebViewClient(new WebViewClient(){
                        public void onPageFinished(WebView view, String url) {
                            dialog.show();
                        }
                    });

                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notifications);
            setHasOptionsMenu(true);

            SwitchPreference active = (SwitchPreference) findPreference(getString(R.string.pref_key_notifications_active));
            TimePreference time     = (TimePreference) findPreference(getString(R.string.pref_key_notifications_time));

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
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void updatePreferencesEnabled(boolean enabled) {
            Preference preference = findPreference(getString(R.string.pref_key_notifications_time));
            preference.setEnabled(enabled);

            preference = findPreference(getString(R.string.pref_key_notifications_sound));
            preference.setEnabled(enabled);

            preference = findPreference(getString(R.string.pref_key_notifications_vibrate));
            preference.setEnabled(enabled);
        }

        private void updateTimePreferenceSummary(Preference preference) {
            TimePreference timePreference = (TimePreference) preference;
            preference.setSummary(timePreference.getSummary());
        }
    }
}
