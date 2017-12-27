package de.beusterse.abfalllro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import de.beusterse.abfalllro.interfaces.SyncCallback;
import de.beusterse.abfalllro.service.ServiceManager;
import de.beusterse.abfalllro.service.SyncClient;

/**
 * Main info activity, presents processed data as trash cans
 *
 * Created by Felix Beuster
 */
public class TrashCheckActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, SyncCallback {

    private DataLoader loader;
    private ServiceManager serviceManager;
    private SyncClient mSyncClient;
    private TrashController controller;
    private UIUpdater updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);

        loader          = new DataLoader(this);
        controller      = new TrashController(pref, loader, getResources());
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

        mSyncClient = new SyncClient(this);
        mSyncClient.run();
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

    @Override
    public void syncComplete() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        // TODO simple reload() methods would be nice
        loader          = new DataLoader(this);
        controller      = new TrashController(pref, loader, getResources());
        updater         = new UIUpdater(this, pref);

        updater.prepare(controller.getCans(), controller.getError(), controller.getPreview());
        updater.update();
    }

    @Override
    public void updateFromDownload(Object result) {
        mSyncClient.updateFromDownload(result);
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

    @Override
    public void finishDownloading() {
        mSyncClient.finishDownloading();
    }
}
