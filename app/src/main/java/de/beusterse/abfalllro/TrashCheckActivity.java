package de.beusterse.abfalllro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import de.beusterse.abfalllro.service.ServiceManager;

/**
 * Main info activity, presents processed data as trash cans
 *
 * Created by Felix Beuster
 */
public class TrashCheckActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private DataLoader loader;
    private ServiceManager serviceManager;
    private TrashController controller;
    private UIUpdater updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);

        loader          = new DataLoader(this);
        controller      = new TrashController(pref, loader.getCodes(), loader.getSchedule(), getResources());
        serviceManager  = new ServiceManager(this);
        updater         = new UIUpdater(this, pref);

        setTheme( controller.getTheme() );

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trash_check_acitivity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        serviceManager.bind();

        updater.prepare(controller.getCans(), controller.getError(), controller.getPreview());
        updater.update();

        serviceManager.run();
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
        if( !(  key.equals(getString(R.string.pref_key_intern_last_alarm_black)) ||
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
}
