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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.beusterse.abfalllro.interfaces.DownloadCallback;
import de.beusterse.abfalllro.service.NetworkFragment;
import de.beusterse.abfalllro.utils.HashUtils;
import de.beusterse.abfalllro.utils.JSONUtils;

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
            String url  = "https://abfallkalenderlandkreisrostock.beusterse.de/api.php?";
            url         += "year=2017";

            // TODO this should come from prefs
            boolean noHashStored = true;

            if (noHashStored) {
                url += "&codes=" + HashUtils.inputStreamToSha256(getResources().openRawResource(R.raw.codes_2017));
                url += "&schedule=" + HashUtils.inputStreamToSha256(getResources().openRawResource(R.raw.schedule_2017));
                url += "&street_codes=" + HashUtils.inputStreamToSha256(getResources().openRawResource(R.raw.street_codes_2017));
            }

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

        String resultString = result.toString();

        if (JSONUtils.isValidJSON(resultString)) {
            JsonParser parser = new JsonParser();
            JsonObject object = (parser.parse(resultString)).getAsJsonObject();
            Log.d("fetch", object.toString());

            switch (object.get("status").getAsInt()) {
                case 200:
                    // TODO foreach file check status
                    // TODO     200 save to file
                    // TODO     304 no change
                    Log.d("sync data", object.get("codes").getAsJsonObject().get("status").getAsString());
                    Log.d("sync data", object.get("schedule").getAsJsonObject().get("status").getAsString());
                    Log.d("sync data", object.get("street_codes").getAsJsonObject().get("status").getAsString());
                    break;
                case 404:
                    Log.d("sync data", object.get("message").getAsString());
                    break;
                case 500:
                    Log.d("sync data", object.get("message").getAsString());
                    break;
                default:
                    break;
            }
        }
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
