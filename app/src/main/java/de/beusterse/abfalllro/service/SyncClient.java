package de.beusterse.abfalllro.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    private ArrayList<String> mFiles;
    private ArrayList<String> mYears;
    private boolean mDownloading = false;
    private Context mContext;
    private int mStatus = 304;
    private JsonObject mSyncData;
    private NetworkClient mNetworkClient;
    private Resources mResources;
    private String mView;

    public SyncClient(Context context, String view) {
        mContext    = context;
        mResources  = context.getResources();
        mFiles      = new ArrayList<>();
        mView       = view;
        mYears      = new ArrayList<>();

        loadSyncData();

        mFiles.add("codes");
        mFiles.add("schedule");
        mFiles.add("street_codes");
    }

    /**
     * Checks if response object has valid data.
     *
     * @param responseObject
     */
    private void evaluateResponseObject(JsonObject responseObject) {
        // check overall status
        switch (responseObject.get("status").getAsInt()) {
            case 200:
                // set years to check
                for (String key : responseObject.keySet()) {
                    if (key.matches("\\d{4}")) {
                        mYears.add(key);
                    }
                }

                // check year status
                for (String year : mYears) {
                    if (!mSyncData.has(year)) {
                        // adding member if not exists
                        mSyncData.add(year, new JsonObject());
                    }

                    evaluateYearObject( year, responseObject.get( year ).getAsJsonObject() );
                }
                break;
            case 500:
            default:
                break;
        }
    }

    /**
     * Checks if year object has valid data.
     *
     * @param year
     * @param yearObject
     */
    private void evaluateYearObject(String year, JsonObject yearObject) {
        switch (yearObject.get("status").getAsInt()) {
            case 200:   // check each file
                saveToStorage( year, "codes", yearObject.get("codes").getAsJsonObject() );
                saveToStorage( year, "schedule", yearObject.get("schedule").getAsJsonObject() );
                saveToStorage( year, "street_codes", yearObject.get("street_codes").getAsJsonObject() );
                break;
            case 404:   // no data found for year
            default:
                break;
        }
    }

    /**
     * Callback when download finishes.
     */
    public void finishDownloading() {
        mDownloading = false;
        if (mNetworkClient != null) {
            mNetworkClient.cancelDownload();
        }
    }

    /**
     * Gets the hash of a given file.
     *
     * @param name File name
     * @return Hash string
     */
    private String getFileHash(String name) {
        try {
            return HashUtils.inputStreamToSha256(mResources.openRawResource(getResourceIdentifier(name)));

        } catch (Exception e) {
            // if no resource is found, append empty hash
            return "";
        }
    }

    /**
     * Get a resource identifier by the resource's name
     *
     * @param name Resource name
     * @return Resource identifier
     */
    private int getResourceIdentifier(String name) {
        return mResources.getIdentifier(name, "raw", mContext.getPackageName());
    }

    /**
     * Get the status of the sync.
     *
     * @return
     */
    public int getStatus() {
        return mStatus;
    }

    /**
     * Construct the sync request URL, to include all needed files and years.
     * @return URL string
     */
    private String getSyncRequestUrl() {
        Calendar now        = Calendar.getInstance();
        SimpleDateFormat yf = new SimpleDateFormat("yyyy");

        String url              = "https://abfallkalenderlandkreisrostock.beusterse.de/api.php";
        String codes_url        = "&codes=";
        String schedule_url     = "&schedule=";
        String street_codes_url = "&street_codes=";
        String view_url         = "&view=" + mView;
        String year_url         = "?year=";

        // adding years from saved data, with saved or generated hashes
        HashMap<String, String[]> year_hashes = new HashMap<>();

        for (Map.Entry<String, JsonElement> year : mSyncData.entrySet()) {
            int year_value = Integer.parseInt(year.getKey());

            if (year_value >= now.get(Calendar.YEAR)) {
                String[] hashes         = new String[3];
                JsonObject yearObject   = year.getValue().getAsJsonObject();

                if (yearObject.has("codes")) {
                    hashes[0] = yearObject.get("codes").getAsString();
                } else {
                    hashes[0] = getFileHash("codes_" + year.getKey());
                }

                if (yearObject.has("schedule")) {
                    hashes[1] = yearObject.get("schedule").getAsString();
                } else {
                    hashes[1] = getFileHash("schedule_" + year.getKey());
                }

                if (yearObject.has("street_codes")) {
                    hashes[2] = yearObject.get("street_codes").getAsString();
                } else {
                    hashes[2] = getFileHash("street_codes_" + year.getKey());
                }

                year_hashes.put(year.getKey(), hashes);
            }
        }

        if (!year_hashes.containsKey(yf.format(now.getTime()))) {
            String[] hashes = new String[3];
            String year     = yf.format(now.getTime());

            hashes[0] = getFileHash("codes_" + year);
            hashes[1] = getFileHash("schedule_" + year);
            hashes[2] = getFileHash("street_codes_" + year);

            year_hashes.put(year, hashes);
        }

        if (DataLoader.needsMultipleYears()) {
            now.add(Calendar.YEAR, 1);
            if (!year_hashes.containsKey(yf.format(now.getTime()))) {
                String[] hashes = new String[3];
                String year     = yf.format(now.getTime());

                hashes[0] = getFileHash("codes_" + year);
                hashes[1] = getFileHash("schedule_" + year);
                hashes[2] = getFileHash("street_codes_" + year);

                year_hashes.put(year, hashes);
            }
        }

        for (Map.Entry<String, String[]> year : year_hashes.entrySet()) {
            codes_url           += year.getValue()[0];
            schedule_url        += year.getValue()[1];
            street_codes_url    += year.getValue()[2];

            year_url += year.getKey();

            if (!year.getKey().equals(year_hashes.get(year_hashes.size() - 1))) {
                codes_url           += ";";
                schedule_url        += ";";
                street_codes_url    += ";";
                year_url            += ";";
            }
        }

        return url + year_url + codes_url + schedule_url + street_codes_url + view_url;
    }

    /**
     * Loads currently stored sync data.
     */
    private void loadSyncData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String syncDataString   = prefs.getString(mResources.getString(R.string.pref_key_intern_sync_data), "");

        try {
            if (syncDataString.equals("") || !JSONUtils.isValidJSON(syncDataString)) {
                mSyncData = new JsonObject();

            } else {
                JsonParser parser = new JsonParser();
                mSyncData = parser.parse(syncDataString).getAsJsonObject();
            }

        } catch (Exception e) {
            mSyncData = new JsonObject();
        }
    }

    /**
     * Action when a progress update comes in.
     *
     * @param progressCode
     * @param percentComplete
     */
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

    /**
     * Runs the SyncClient
     */
    public void run() {
        SharedPreferences pref  = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean sync_enabled    = pref.getBoolean(  mResources.getString(R.string.pref_key_sync_auto),
                mResources.getBoolean(R.bool.sync_auto) );

        if (sync_enabled) {
            mNetworkClient = new NetworkClient((DownloadCallback) mContext, getSyncRequestUrl());

            if (!mDownloading && mNetworkClient != null) {
                mNetworkClient.startDownload();
                mDownloading = true;
            }
        }
    }

    /**
     * Saves a data string to a file.
     *
     * @param filename Destination for the file
     * @param data Data to be stored
     * @return boolean
     */
    private boolean saveFile(String filename, String data) {
        FileOutputStream outputStream;

        try {
            outputStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Saves the data of a given temporary file and year to a local file.
     *
     * @param year Year of the file
     * @param file Name of the temporary file
     * @param fileObject Object containing the data of the file
     */
    private void saveToStorage(String year, String file, JsonObject fileObject) {
        switch (fileObject.get("status").getAsInt()) {
            case 200:   // new data available
                String filename = file + "_" + year + "." + fileObject.get("type").getAsString();
                mStatus = 200;

                boolean saved;
                if (fileObject.get("type").getAsString().equals("json")) {
                    saved = saveFile(filename, fileObject.get("data").toString());

                } else {
                    saved = saveFile(filename, fileObject.get("data").getAsString());
                }

                if ( saved ) {
                    mSyncData.get(year).getAsJsonObject().addProperty(file, fileObject.get("hash").getAsString());
                }
                break;
            case 304:   // no changes in data
            default:
                break;
        }
    }

    /**
     * Callback for updates from downloads.
     *
     * Evaluates response object and saves data to preferences.
     *
     * @param result
     */
    public void updateFromDownload(Object result) {
        if (result != null) {
            String resultString = result.toString();

            if (JSONUtils.isValidJSON(resultString)) {
                JsonParser parser = new JsonParser();
                JsonObject responseObject = (parser.parse(resultString)).getAsJsonObject();

                evaluateResponseObject(responseObject);

                // updating preferences to store sync data
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = prefs.edit();

                // saving the date of the checks
                Calendar now = Calendar.getInstance();
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());

                editor.putString(mResources.getString(R.string.pref_key_sync_last_check), df.format(now.getTime()));

                if (mStatus == 200 || !prefs.contains(mResources.getString(R.string.pref_key_sync_last_update))) {
                    editor.putString(mResources.getString(R.string.pref_key_sync_last_update), df.format(now.getTime()));
                }

                editor.putString(mResources.getString(R.string.pref_key_intern_sync_data), mSyncData.toString());
                editor.apply();
            }
        }

        ((SyncCallback) mContext).syncComplete();
    }
}
