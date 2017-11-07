package de.beusterse.abfalllro.interfaces;

/**
 * Callback interface for the SyncClient
 *
 * Created by Felix Beuster
 */

public interface SyncCallback extends DownloadCallback {
    void syncComplete();
}
