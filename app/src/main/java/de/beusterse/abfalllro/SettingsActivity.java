package de.beusterse.abfalllro;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

import java.util.List;

import de.beusterse.abfalllro.interfaces.SyncCallback;
import de.beusterse.abfalllro.service.SyncClient;

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

    private SyncClient mSyncClient;

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
            finish();
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
                || PickupPreferenceFragment.class.getName().equals(fragmentName)
                || InfoPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationsPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void finishDownloading() {
        mSyncClient.finishDownloading();
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        mSyncClient.onProgressUpdate(progressCode, percentComplete);
    }

    public void startManualSync() {
        mSyncClient = new SyncClient(this, "manual_check");
        mSyncClient.run();
    }

    @Override
    public void syncComplete() {
        String toast;
        switch (mSyncClient.getStatus()) {
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

    @Override
    public void updateFromDownload(Object result) {
        mSyncClient.updateFromDownload(result);
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
            updateStreetSummary(findPreference(getString(R.string.pref_key_pickup_town)), cityName);
            updateStreetLocationPref(cityName);
            updateSyncSummary();

            ListPreference city = (ListPreference) findPreference(getString(R.string.pref_key_pickup_town));
            city.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String stringValue = newValue.toString();

                    updateStreetSummary(preference, stringValue);
                    updateStreetLocationPref(stringValue);
                    return true;
                }
            });

            /**
             * Button press to manually synchronize the pickup data.
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

        private void updateStreetSummary(Preference preference, String stringValue) {
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
