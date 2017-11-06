package de.beusterse.abfalllro.capsules;

/**
 * Holding a result of a download task.
 *
 * Created by Felix Beuster
 */

public class DownloadResult {
    public String mResultValue;
    public Exception mException;
    public DownloadResult(String resultValue) {
        mResultValue = resultValue;
    }
    public DownloadResult(Exception exception) {
        mException = exception;
    }
}
