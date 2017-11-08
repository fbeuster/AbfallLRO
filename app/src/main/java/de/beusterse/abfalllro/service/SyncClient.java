package de.beusterse.abfalllro.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.beusterse.abfalllro.DataLoader;
import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.interfaces.DownloadCallback;
import de.beusterse.abfalllro.interfaces.SyncCallback;
import de.beusterse.abfalllro.utils.HashUtils;
import de.beusterse.abfalllro.utils.JSONUtils;

/**
 * SyncClient for checking for new data on the server and
 * saving it to local storage.
 *
 * Created by Felix Beuster
 */

public class SyncClient {

    private NetworkFragment mNetworkFragment;
    private boolean mDownloading = false;
    private Context mContext;
    private Resources mResources;

    public SyncClient(Context context) {
        mContext    = context;
        mResources  = context.getResources();
    }

    public void run() {
        checkPickupCodes();
    }

    private String getSyncRequestUrl() {
        String url  = "https://abfallkalenderlandkreisrostock.beusterse.de/api.php";

        Calendar now            = Calendar.getInstance();
        SimpleDateFormat yf     = new SimpleDateFormat("yyyy");
        ArrayList<String> years = new ArrayList<>();
        years.add(yf.format(now.getTime()));

        if (DataLoader.needsMultipleYears()) {
            Calendar later = Calendar.getInstance();
            later.add(Calendar.YEAR, 1);
            years.add(yf.format(later.getTime()));
        }

        String codes_url        = "&codes=";
        String schedule_url     = "&schedule=";
        String street_codes_url = "&street_codes=";
        String year_url         = "?year=";

        // TODO this should come from prefs
        boolean noHashStored    = true;

        for (String year : years) {
            if (noHashStored) {
                codes_url           += getFileHash("raw/codes_" + year);
                schedule_url        += getFileHash("raw/schedule_" + year);
                street_codes_url    += getFileHash("raw/street_codes_" + year);

            } else {
                // TODO use stored hashes
            }

            year_url += year;

            if (!year.equals(years.get(years.size() - 1))) {
                codes_url           += ";";
                schedule_url        += ";";
                street_codes_url    += ";";
                year_url            += ";";
            }
        }

        return url + year_url + codes_url + schedule_url + street_codes_url;
    }


    private void checkPickupCodes() {
        SharedPreferences pref  = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean sync_enabled    = pref.getBoolean(  mResources.getString(R.string.pref_key_sync_auto),
                                                    mResources.getBoolean(R.bool.sync_auto) );

        if (sync_enabled) {
            mNetworkFragment = NetworkFragment.getInstance(((AppCompatActivity) mContext).getFragmentManager(), getSyncRequestUrl());
        }
    }

    private String getFileHash(String name) {
        try {
            return HashUtils.inputStreamToSha256(mResources.openRawResource(getResourceIdentifier(name)));

        } catch (Exception e) {
            // if no resource is found, append empty hash
            return "";
        }
    }

    private int getResourceIdentifier(String name) {
        return mResources.getIdentifier(name, "raw", mContext.getPackageName());
    }

    public void updateFromDownload(Object result) {
        // TODO update pickup code files
        // TODO continue to intro activity

        String resultString = result.toString();

        if (JSONUtils.isValidJSON(resultString)) {
            JsonParser parser           = new JsonParser();
            JsonObject responseObject   = (parser.parse(resultString)).getAsJsonObject();

            evaluateResponseObject( responseObject );
        }

        ((SyncCallback) mContext).syncComplete();
    }

    private void evaluateResponseObject(JsonObject responseObject) {
        Log.d("fetch", responseObject.toString());

        // check overall status
        switch (responseObject.get("status").getAsInt()) {
            case 200:
                // check year status
                // TODO add loop over years
                JsonObject yearObject = responseObject.get("2017").getAsJsonObject();
                evaluateYearObject( yearObject );
                break;
            case 500:
                Log.d("sync data", responseObject.get("message").getAsString());
                break;
            default:
                break;
        }
    }

    private void evaluateYearObject(JsonObject yearObject) {
        switch (yearObject.get("status").getAsInt()) {
            case 200:
                // check each file
                // TODO add loop over files
                switch (yearObject.get("status").getAsInt()) {
                    case 200:
                        // new data available
                        // TODO save to file
                        Log.d("sync data", yearObject.get("codes").getAsJsonObject().get("status").getAsString());
                        Log.d("sync data", yearObject.get("schedule").getAsJsonObject().get("status").getAsString());
                        Log.d("sync data", yearObject.get("street_codes").getAsJsonObject().get("status").getAsString());
                        break;
                    case 304:
                        // no changes in data
                        // TODO check if resources ex in internal storage or copy
                        break;
                    default:
                        break;
                }
            case 404: // no data found for year
            case 500: // missing hashes for year
            default:
                break;
        }
    }

    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    public void onProgressUpdate(int progressCode, int percentComplete) {
        switch(progressCode) {
            case DownloadCallback.Progress.ERROR:
                // TODO error occurred, continue to intro activity
                break;
            case DownloadCallback.Progress.CONNECT_SUCCESS:
                break;
            case DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS:
                break;
            case DownloadCallback.Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                break;
            case DownloadCallback.Progress.PROCESS_INPUT_STREAM_SUCCESS:
                break;
        }
    }

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
    public void ready() {
        if (!mDownloading && mNetworkFragment != null) {
            mNetworkFragment.startDownload();
            mDownloading = true;
        }
    }
}
