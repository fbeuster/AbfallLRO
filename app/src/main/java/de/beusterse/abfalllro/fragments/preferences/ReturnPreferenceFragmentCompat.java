package de.beusterse.abfalllro.fragments.preferences;

import android.content.Intent;
import android.view.MenuItem;

import androidx.preference.PreferenceFragmentCompat;
import de.beusterse.abfalllro.activities.SettingsActivity;

public abstract class ReturnPreferenceFragmentCompat extends PreferenceFragmentCompat {
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
