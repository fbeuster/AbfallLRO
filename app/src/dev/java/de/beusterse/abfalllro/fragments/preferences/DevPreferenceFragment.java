package de.beusterse.abfalllro.fragments.preferences;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.activities.IntroActivity;
import de.beusterse.abfalllro.utils.JSONUtils;

public class DevPreferenceFragment extends ReturnPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_dev, rootKey);
        setHasOptionsMenu(true);

        Preference clearDataButton = findPreference(getString(R.string.pref_key_dev_clear_data));
        clearDataButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                JsonObject syncData;
                SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
                SharedPreferences.Editor editor     = sharedPreferences.edit();


                String syncDataString = sharedPreferences.getString(getString(R.string.pref_key_intern_sync_data), "");

                try {
                    if (syncDataString.equals("") || !JSONUtils.isValidJSON(syncDataString)) {
                        syncData = new JsonObject();

                    } else {
                        JsonParser parser = new JsonParser();
                        syncData = parser.parse(syncDataString).getAsJsonObject();
                    }

                } catch (Exception e) {
                    return true;
                }

                for (String year : syncData.keySet()) {
                    if (year.matches("\\d{4}") && syncData.get(year).isJsonObject()) {
                        for (String file : syncData.getAsJsonObject(year).keySet()) {
                            String fileName = file + "_" + year + ".";

                            if (file.equals("schedule")) {
                                fileName += "csv";
                            } else {
                                fileName += "json";
                            }

                            preference.getContext().deleteFile(fileName);
                        }
                    }
                }

                editor.remove(getString(R.string.pref_key_sync_last_check));
                editor.remove(getString(R.string.pref_key_sync_last_update));
                editor.remove(getString(R.string.pref_key_intern_sync_data));
                editor.apply();

                Toast.makeText(
                        preference.getContext(),
                        getString(R.string.pref_dev_clear_data_toast),
                        Toast.LENGTH_LONG
                ).show();

                return true;
            }
        });

        Preference clearSettingsButton = findPreference(getString(R.string.pref_key_dev_clear_settings));
        clearSettingsButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(preference.getContext(), IntroActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);

                return false;
            }
        });

        Preference showSettingsButton = findPreference(getString(R.string.pref_key_dev_show_settings));
        showSettingsButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(getPreferenceDump().toString(4));
                    builder.setTitle(getString(R.string.pref_dev_show_settings_title));
                    builder.setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } catch (JSONException e) {
                }

                return false;
            }
        });
    }

    private JSONObject getPreferenceDump() {
        JSONObject preferencesJson = new JSONObject();

        HashSet<String> maxSDK25Values = new HashSet<String>() {{
            add(getString(R.string.pref_key_notifications_sound));
            add(getString(R.string.pref_key_notifications_vibrate));
        }};

        HashSet<String> stringValues = new HashSet<String>() {{
            add(getString(R.string.pref_key_intern_last_alarm_black));
            add(getString(R.string.pref_key_intern_last_alarm_blue));
            add(getString(R.string.pref_key_intern_last_alarm_green));
            add(getString(R.string.pref_key_intern_last_alarm_yellow));
            add(getString(R.string.pref_key_intern_last_daily_check));
            add(getString(R.string.pref_key_notifications_sound));
            add(getString(R.string.pref_key_notifications_vibrate));
            add(getString(R.string.pref_key_notifications_time));
            add(getString(R.string.pref_key_pickup_town));
            add(getString(R.string.pref_key_pickup_street));
            add(getString(R.string.pref_key_pickup_schedule_black));
            add(getString(R.string.pref_key_pickup_schedule_blue));
            add(getString(R.string.pref_key_pickup_schedule_green));
            add(getString(R.string.pref_key_pickup_schedule_yellow));
            add(getString(R.string.pref_key_sync_last_check));
            add(getString(R.string.pref_key_sync_last_update));
            add(getString(R.string.pref_key_intern_sync_data));
            add(getString(R.string.pref_key_info_theme));
        }};

        HashSet<String> booleanValues = new HashSet<String>() {{
            add(getString(R.string.pref_key_intern_setup_done));
            add(getString(R.string.pref_key_notifications_active));
            add(getString(R.string.pref_key_sync_auto));
        }};

        HashSet<String> intValues = new HashSet<String>() {{
            add(getString(R.string.pref_key_intern_migrated_version));
        }};

        ArrayList<String> keys = new ArrayList<>();
        keys.addAll(stringValues);
        keys.addAll(booleanValues);
        keys.addAll(intValues);

        Collections.sort(keys);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        for (String key : keys) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && maxSDK25Values.contains(key)) {
                continue;
            }

            try {
                if (booleanValues.contains(key)) {
                    preferencesJson.put(key, preferences.getBoolean(key, false));
                } else if (intValues.contains(key)) {
                    preferencesJson.put(key, preferences.getInt(key, -2));
                } else {
                    preferencesJson.put(key, preferences.getString(key, ""));
                }
            } catch (JSONException e) {
            }
        }

        return preferencesJson;
    }
}
