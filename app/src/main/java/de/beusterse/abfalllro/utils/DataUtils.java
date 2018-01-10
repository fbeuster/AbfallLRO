package de.beusterse.abfalllro.utils;

/**
 * Utility functions related to data.
 *
 * Created by Felix Beuster on 1/10/2018.
 */

public class DataUtils {
    private static final String CITY_WITH_STREETS = "0000";

    public static boolean isCityWithStreets(String codes) {
        return codes.substring(0, Math.min(codes.length(), 4)).equals(CITY_WITH_STREETS);
    }
}
