package de.beusterse.abfalllro.utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Utility methods for hashing data.
 *
 * Created by Felix Beuster on 11/6/2017.
 */

public class HashUtils {
    /**
     * Generates a SHA256 hash string from an input stream.
     *
     * @param inputStream
     * @return Hash string
     */
    public static String inputStreamToSha256(InputStream inputStream) {
        try {
            byte[] bytes = new byte[inputStream.available()];

            BufferedInputStream buffer = new BufferedInputStream(inputStream);
            buffer.read(bytes, 0, bytes.length);
            buffer.close();

            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(bytes);

            return bin2hex(hash);

        } catch (Exception e) {
            Log.e("File Utils", e.getMessage(), e);
            return "";
        }
    }

    /**
     * Converts a byte array to a hexadecimal string.
     *
     * @param data
     * @return
     */
    public static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }
}
