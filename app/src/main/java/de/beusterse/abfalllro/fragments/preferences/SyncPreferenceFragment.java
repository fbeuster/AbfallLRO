package de.beusterse.abfalllro.fragments.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.Preference;
import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.activities.SettingsActivity;

public class SyncPreferenceFragment extends ReturnPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_sync, rootKey);
        setHasOptionsMenu(true);
        updateSyncSummary();

        Preference syncManualButton = findPreference(getString(R.string.pref_key_sync_manual));
        syncManualButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((SettingsActivity) getActivity()).startManualSync();
                return true;
            }
        });
    }

    public void updateSyncSummary() {
        SharedPreferences sp    = getPreferenceManager().getSharedPreferences();
        String last_check       = sp.getString(getString(R.string.pref_key_sync_last_check), "-");
        String last_update      = sp.getString(getString(R.string.pref_key_sync_last_update), last_check);

        Preference sync_summary = findPreference(getString(R.string.pref_key_sync_last_check));
        sync_summary.setSummary(getString( R.string.pref_sync_description_last_sync, last_check, last_update ));
    }
}
