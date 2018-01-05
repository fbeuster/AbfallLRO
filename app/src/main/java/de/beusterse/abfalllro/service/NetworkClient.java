package de.beusterse.abfalllro.service;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import de.beusterse.abfalllro.capsules.DownloadResult;
import de.beusterse.abfalllro.interfaces.DownloadCallback;

/**
 * Implementation of headless Fragment that runs an AsyncTask to fetch data from the network.
 *
 * Implemented by Felix Beuster
 * Created by Google Samples
 * https://github.com/googlesamples/android-NetworkConnect/blob/master/Application/src/main/java/com/example/android/networkconnect/NetworkFragment.java
 */
public class NetworkClient {
    private DownloadCallback mDownloadCallback;
    private DownloadTask mDownloadTask;
    private String mUrlString;

    public NetworkClient(DownloadCallback downloadCallback, String url) {
        mDownloadCallback = downloadCallback;
        mUrlString = url;
    }

    /**
     * Start non-blocking execution of DownloadTask.
     */
    public void startDownload() {
        cancelDownload();
        mDownloadTask = new DownloadTask();
        mDownloadTask.execute(mUrlString);
    }

    /**
     * Cancel (and interrupt if necessary) any ongoing DownloadTask execution.
     */
    public void cancelDownload() {
        if (mDownloadTask != null) {
            mDownloadTask.cancel(true);
        }
    }

    /**
     * Implementation of AsyncTask designed to fetch data from the network.
     */
    private class DownloadTask extends AsyncTask<String,Integer,DownloadResult> {

        /**
         * Given a URL, sets up a connection and gets the HTTP response body from the server.
         * If the network request is successful, it returns the response body in String form. Otherwise,
         * it will throw an IOException.
         */
        private String downloadUrl(URL url) throws IOException {
            InputStream stream = null;
            HttpsURLConnection connection = null;
            String result = null;

            try {
                connection = (HttpsURLConnection) url.openConnection();
                connection.setReadTimeout(3000);
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-length", "0");
                connection.setDoInput(true);
                connection.connect();

                publishProgress(DownloadCallback.Progress.CONNECT_SUCCESS);
                int responseCode = connection.getResponseCode();

                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }

                // Retrieve the response body as an InputStream.
                publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);
                stream                              = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(stream);
                BufferedReader bufferedReader       = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder         = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                bufferedReader.close();
                result = stringBuilder.toString();

            } finally {
                // Close Stream and disconnect HTTPS connection.
                if (stream != null) {
                    stream.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return result;
        }

        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        @Override
        protected void onPreExecute() {
            if (mDownloadCallback != null) {
                NetworkInfo networkInfo = mDownloadCallback.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected() ||
                        (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                                && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                    // If no connectivity, cancel task and update Callback with null data.
                    mDownloadCallback.updateFromDownload(null);
                    cancel(true);
                }
            }
        }

        /**
         * Defines work to perform on the background thread.
         */
        @Override
        protected DownloadResult doInBackground(String... urls) {
            DownloadResult result = null;
            if (!isCancelled() && urls != null && urls.length > 0) {
                String urlString = urls[0];
                try {
                    URL url = new URL(urlString);
                    String resultString = downloadUrl(url);
                    if (resultString != null) {
                        result = new DownloadResult(resultString);
                    } else {
                        throw new IOException("No response received.");
                    }
                } catch(Exception e) {
                    result = new DownloadResult(e);
                }
            }

            return result;
        }

        /**
         * Updates the DownloadCallback with the result.
         */
        @Override
        protected void onPostExecute(DownloadResult result) {
            if (result != null && mDownloadCallback != null) {
                if (result.mException != null) {
                    mDownloadCallback.updateFromDownload(result.mException.getMessage());
                } else if (result.mResultValue != null) {
                    mDownloadCallback.updateFromDownload(result.mResultValue);
                }
                mDownloadCallback.finishDownloading();
            }
        }

        /**
         * Override to add special behavior for cancelled AsyncTask.
         */
        @Override
        protected void onCancelled(DownloadResult result) {
        }
    }
}