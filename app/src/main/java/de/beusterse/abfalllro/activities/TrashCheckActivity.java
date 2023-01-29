package de.beusterse.abfalllro.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import de.beusterse.abfalllro.controller.DataController;
import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.controller.SyncController;
import de.beusterse.abfalllro.controller.TrashController;
import de.beusterse.abfalllro.controller.UIController;
import de.beusterse.abfalllro.interfaces.SyncCallback;
import de.beusterse.abfalllro.service.ServiceManager;

/**
 * Main info activity, presents processed data as trash cans
 *
 * Created by Felix Beuster
 */
public class TrashCheckActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, SyncCallback {

    private DataController dataController;
    private ServiceManager serviceManager;
    private SyncController mSyncController;
    private TrashController trashController;
    private UIController uiController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);

        dataController  = new DataController(this);
        trashController = new TrashController(this, dataController);
        serviceManager  = new ServiceManager(this);
        uiController    = new UIController(this, pref);

        setTheme( trashController.getTheme() );

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trash_check_acitivity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        serviceManager.bind();

        uiController.prepare(trashController.getCans(), trashController.getError(), trashController.getPreview());
        uiController.update();

        serviceManager.run();

        if (pref.getBoolean(
                getString(R.string.pref_key_sync_auto),
                getResources().getBoolean(R.bool.sync_auto))) {
            mSyncController = new SyncController(this, "trash_check", this);
            mSyncController.run();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if( key != null && !(  key.equals(getString(R.string.pref_key_intern_last_alarm_black)) ||
                key.equals(getString(R.string.pref_key_intern_last_alarm_blue))  ||
                key.equals(getString(R.string.pref_key_intern_last_alarm_green)) ||
                key.equals(getString(R.string.pref_key_intern_last_alarm_yellow)) ) ) {
            recreate();
        }
    }

    @Override
    protected void onStop() {
        serviceManager.unbind();
        super.onStop();
    }

    @Override
    public void syncComplete() {
        if (mSyncController.getStatus() == 200) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

            // TODO simple reload() methods would be nice
            dataController = new DataController(this);
            trashController = new TrashController(this, dataController);
            uiController = new UIController(this, pref);

            uiController.prepare(trashController.getCans(), trashController.getError(), trashController.getPreview());
            uiController.update();
        }
    }
}
