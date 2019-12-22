package de.beusterse.abfalllro.fragments.preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import androidx.preference.Preference;
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
    }
}
