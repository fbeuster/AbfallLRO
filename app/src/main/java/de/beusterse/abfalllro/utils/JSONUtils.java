package de.beusterse.abfalllro.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility methods for handling JSON data.
 *
 * Created by Felix Beuster on 11/5/2017.
 */

public class JSONUtils {
    private static final Gson gson = new Gson();

    private JSONUtils(){}

    /**
     * Checks if a string is a valid JSON string.
     *
     * @param jsonInString String that is meant to be JSON
     * @return true if string is valid JSON String
     */
    public static boolean isValidJSON(String jsonInString) {
        if (jsonInString == null || jsonInString.equals("")) {
            return false;
        }

        try {
            gson.fromJson(jsonInString, Object.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    /**
     * Reads a file into a JSON object.
     *
     * @param inputStream input stream of the file
     * @return JsonObject from file
     */
    public static JsonObject getJsonObjectFromInputStream(InputStream inputStream) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader       = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder         = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            inputStreamReader.close();

            if (JSONUtils.isValidJSON(stringBuilder.toString())) {
                JsonParser parser = new JsonParser();
                return (parser.parse(stringBuilder.toString())).getAsJsonObject();

            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }
}
