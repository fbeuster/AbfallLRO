package de.beusterse.abfalllro.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

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

    public static JsonObject getJsonObjectFromFile(Context context, String filename) {
        FileInputStream inputStream;
        try {
            inputStream = context.openFileInput(filename);
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
