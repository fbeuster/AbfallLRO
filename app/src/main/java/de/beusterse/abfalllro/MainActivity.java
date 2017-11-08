package de.beusterse.abfalllro;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import de.beusterse.abfalllro.interfaces.SyncCallback;
import de.beusterse.abfalllro.service.SyncClient;

/**
 * Opening activity of the app
 *
 * Created by Felix Beuster
 */
public class MainActivity extends AppCompatActivity implements SyncCallback {

    private static int SPLASH_TIME = 2000;
    private SyncClient mSyncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSyncClient = new SyncClient(this);
        mSyncClient.run();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void syncComplete() {
        // TODO continue to intro activity on error, success or after timeout
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, IntroActivity.class));
                finish();
            }
        }, SPLASH_TIME);
    }

    @Override
    public void updateFromDownload(Object result) {
        mSyncClient.updateFromDownload(result);
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
        mSyncClient.onProgressUpdate(progressCode, percentComplete);
    }

    @Override
    public void finishDownloading() {
        mSyncClient.finishDownloading();
    }

    @Override
    public void ready() {
        mSyncClient.ready();
    }
}
