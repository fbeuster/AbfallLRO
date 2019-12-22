package de.beusterse.abfalllro.fragments.preferences;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import de.beusterse.abfalllro.R;

public class PreferenceHeaders extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_headers, rootKey);
    }
}
