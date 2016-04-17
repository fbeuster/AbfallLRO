package de.beusterse.abfalllro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;

import de.beusterse.abfalllro.service.ScheduleClient;

/**
 * Main info activity, presents processed data as trash cans
 *
 * Created by Felix Beuster
 */
public class TrashCheckActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private DataLoader loader;
    private ScheduleClient scheduleClient;
    private TrashController controller;
    private UIUpdater updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);

        loader = new DataLoader(this, pref);
        controller = new TrashController(pref, loader.getCode(), loader.getSchedule());
        updater = new UIUpdater(this, pref);

        setTheme( controller.getTheme() );

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trash_check_acitivity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        scheduleClient = new ScheduleClient(this);
        scheduleClient.doBindService();

        updater.prepare(controller.getCans(), controller.getError(), controller.getPreview());
        updater.update();

        setAlarms();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

//        updater.updateTrashMainDisplay();
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
        recreate();
    }

    @Override
    protected void onStop() {
        if (scheduleClient != null) {
            scheduleClient.doUnbindService();
        }
        super.onStop();
    }

    private void setAlarms() {
        if (scheduleClient.isBound()) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 5);
            scheduleClient.setAlarmForNotification(cal, RawNotification.BLACK_CAN);
            scheduleClient.setAlarmForNotification(cal, RawNotification.BLUE_CAN);
            scheduleClient.setAlarmForNotification(cal, RawNotification.GREEN_CAN);
            scheduleClient.setAlarmForNotification(cal, RawNotification.YELLOW_CAN);

        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setAlarms();
                }
            }, 50);
        }
    }
}
