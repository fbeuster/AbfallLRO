package de.beusterse.abfalllro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;

import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.utils.AppUtils;

/**
 * Opening activity of the app
 *
 * Created by Felix Beuster
 */
public class MainActivity extends AppCompatActivity {

    private static int SPLASH_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppUtils.setDayNightModeFromPreference(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, IntroActivity.class));
                finish();
            }
        }, SPLASH_TIME);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
