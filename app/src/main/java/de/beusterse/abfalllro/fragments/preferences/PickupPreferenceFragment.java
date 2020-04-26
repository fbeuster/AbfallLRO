package de.beusterse.abfalllro.fragments.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.activities.SettingsActivity;
import de.beusterse.abfalllro.capsules.Schedule;
import de.beusterse.abfalllro.utils.ArrayUtils;

public class PickupPreferenceFragment extends ReturnPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_pickup);
        setHasOptionsMenu(true);

        SharedPreferences sp = getPreferenceManager().getSharedPreferences();
        String cityName = sp.getString(getString(R.string.pref_key_pickup_town), "");

        // Bind the summaries of list preferences to their values
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_pickup_street)));
        updateListPreferenceSummary(findPreference(getString(R.string.pref_key_pickup_town)), cityName);
        updateStreetLocationPref(cityName);

        ListPreference city = findPreference(getString(R.string.pref_key_pickup_town));
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

    private boolean hasNoLocationSchedule(String preferenceKey, String schedule, String location) {
        if (schedule.equals(getString(R.string.pref_can_schedule_twice_a_week))) {
            if (preferenceKey.equals(getString(R.string.pref_key_pickup_schedule_black)) &&
                    isNotWeeklyOrBiweekly(R.array.pref_location_twice_per_week_black, location)) {
                return true;
            }

            if (preferenceKey.equals(getString(R.string.pref_key_pickup_schedule_green)) &&
                    isNotWeeklyOrBiweekly(R.array.pref_location_twice_per_week_green, location)) {
                return true;
            }
        }

        if (schedule.equals(getString(R.string.pref_can_schedule_weekly))) {
            if (preferenceKey.equals(getString(R.string.pref_key_pickup_schedule_black)) &&
                    isNotWeeklyOrBiweekly(R.array.pref_location_weekly_black, location)) {
                return true;
            }

            if (preferenceKey.equals(getString(R.string.pref_key_pickup_schedule_green)) &&
                    isNotWeeklyOrBiweekly(R.array.pref_location_weekly_green, location)) {
                return true;
            }
        }

        return false;
    }

    private boolean isNotWeeklyOrBiweekly(int locationArray, String location) {
        return ArrayUtils.indexOf(getResources().getStringArray(locationArray), location) == -1;
    }

    private void updateCanSchedules(SharedPreferences sp, String location) {
        Preference.OnPreferenceChangeListener onCanScheduleChange = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences sp    = getPreferenceManager().getSharedPreferences();
                String cityName         = sp.getString(getString(R.string.pref_key_pickup_town), "");
                String toast            = getString(R.string.pref_can_schedule_not_available, newValue.toString(), cityName);

                if (hasNoLocationSchedule(preference.getKey(), newValue.toString(), cityName)) {
                    Toast.makeText(getActivity(), toast, Toast.LENGTH_LONG).show();
                    return false;
                }

                updateListPreferenceSummary(preference, newValue.toString());
                return true;
            }
        };

        ListPreference black_can    = findPreference(getString(R.string.pref_key_pickup_schedule_black));
        ListPreference blue_can     = findPreference(getString(R.string.pref_key_pickup_schedule_blue));
        ListPreference green_can    = findPreference(getString(R.string.pref_key_pickup_schedule_green));
        ListPreference yellow_can   = findPreference(getString(R.string.pref_key_pickup_schedule_yellow));

        String schedule_biweekly    = Schedule.BIWEEKLY.name();
        String schedule_monthly     = Schedule.MONTHLY.name();

        // Reading values / defaults
        String scheduleBlack    = sp.getString(
                getString(R.string.pref_key_pickup_schedule_black), schedule_biweekly);

        String scheduleBlue     = sp.getString(
                getString(R.string.pref_key_pickup_schedule_blue), schedule_monthly);

        String scheduleGreen    = sp.getString(
                getString(R.string.pref_key_pickup_schedule_green), schedule_biweekly);

        String scheduleYellow   = sp.getString(
                getString(R.string.pref_key_pickup_schedule_yellow), schedule_biweekly);


        // update schedules due to availability
        if (hasNoLocationSchedule(black_can.getKey(), scheduleBlack, location)) {
            black_can.setValue(schedule_biweekly);
            scheduleBlack = schedule_biweekly;
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(getString(R.string.pref_key_pickup_schedule_black), schedule_biweekly);
            editor.apply();
        }

        if (hasNoLocationSchedule(green_can.getKey(), scheduleGreen, location)) {
            green_can.setValue(schedule_biweekly);
            scheduleGreen = schedule_biweekly;
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(getString(R.string.pref_key_pickup_schedule_green), schedule_biweekly);
            editor.apply();
        }

        // Update can schedule summaries
        updateListPreferenceSummary(black_can, scheduleBlack);
        updateListPreferenceSummary(blue_can, scheduleBlue);
        updateListPreferenceSummary(green_can, scheduleGreen);
        updateListPreferenceSummary(yellow_can, scheduleYellow);

        // ChangeListeners for can schedules
        black_can.setOnPreferenceChangeListener(onCanScheduleChange);
        blue_can.setOnPreferenceChangeListener(onCanScheduleChange);
        green_can.setOnPreferenceChangeListener(onCanScheduleChange);
        yellow_can.setOnPreferenceChangeListener(onCanScheduleChange);
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
        ListPreference street = findPreference(getString(R.string.pref_key_pickup_street));

        boolean needsStreet = cityName.equals(SettingsActivity.CITY_WITH_STREETS);
        street.setEnabled(needsStreet);
    }
}
