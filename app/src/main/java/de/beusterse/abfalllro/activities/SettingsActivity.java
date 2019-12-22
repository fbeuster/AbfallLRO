package de.beusterse.abfalllro.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.controller.SyncController;
import de.beusterse.abfalllro.fragments.preferences.PreferenceHeaders;
import de.beusterse.abfalllro.interfaces.SyncCallback;

public class SettingsActivity extends AppCompatActivity implements SyncCallback,
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    public static final String CITY_WITH_STREETS = "Güstrow";
    private static final String TITLE_TAG = "preferencesActivityTitle";

    private SyncController mSyncController;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.preferences, new PreferenceHeaders())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }

        getSupportFragmentManager()
                .addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    setTitle(R.string.title_activity_settings);
                }
            }
        });

        setupActionBar();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager()
                .getFragmentFactory()
                .instantiate(getClassLoader(), pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.preferences, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
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

    public void startManualSync() {
        mSyncController = new SyncController(this, "manual_check", this);
        mSyncController.run();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
