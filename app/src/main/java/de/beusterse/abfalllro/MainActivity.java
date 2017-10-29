package de.beusterse.abfalllro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;

import de.beusterse.abfalllro.interfaces.DownloadCallback;
import de.beusterse.abfalllro.service.NetworkFragment;

/**
 * Opening activity of the app
 *
 * Created by Felix Beuster
 */
public class MainActivity extends AppCompatActivity implements DownloadCallback {

    private static int SPLASH_TIME = 2000;

    private NetworkFragment mNetworkFragment;
    private boolean mDownloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPickupCodes();

        // TODO continue to intro activity on error, success or after timeout
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, IntroActivity.class));
                finish();
            }
        }, SPLASH_TIME);
    }

    private void checkPickupCodes() {
        SharedPreferences pref  = PreferenceManager.getDefaultSharedPreferences(this);
        boolean sync_enabled    = pref.getBoolean(  getResources().getString(R.string.pref_key_sync_auto),
                                                    getResources().getBoolean(R.bool.sync_auto) );

        if (sync_enabled) {
            String url = "https://abfallkalenderlandkreisrostock.beusterse.de/data/test.json";
            mNetworkFragment = NetworkFragment.getInstance(getFragmentManager(), url);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void updateFromDownload(Object result) {
        // TODO update pickup code files
        // TODO continue to intro activity
        Log.d(this.getLocalClassName(), result.toString());
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        switch(progressCode) {
            case Progress.ERROR:
                // TODO error occurred, continue to intro activity
                break;
            case Progress.CONNECT_SUCCESS:
                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                break;
        }
    }

    @Override
    public void finishDownloading() {
        mDownloading = false;
        if (mNetworkFragment != null) {
            mNetworkFragment.cancelDownload();
        }
    }

    /**
     * Wait for the NetworkFragment to be initialized,
     * then start download.
     */
    @Override
    public void ready() {
        if (!mDownloading && mNetworkFragment != null) {
            mNetworkFragment.startDownload();
            mDownloading = true;
        }
    }
}
