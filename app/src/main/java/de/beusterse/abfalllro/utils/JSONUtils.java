package de.beusterse.abfalllro.utils;

import com.google.gson.Gson;

/**
 * Created by felix on 11/5/2017.
 */

public class JSONUtils {
    private static final Gson gson = new Gson();

    private JSONUtils(){}

    public static boolean isValidJSON(String jsonInString) {
        try {
            gson.fromJson(jsonInString, Object.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }
}
