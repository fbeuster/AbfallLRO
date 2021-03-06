package de.beusterse.abfalllro.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;

import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.capsules.Can;
import de.beusterse.abfalllro.capsules.PickupDay;
import de.beusterse.abfalllro.utils.DataUtils;
import de.beusterse.abfalllro.utils.JSONUtils;


/**
 * Loads raw data and structures them
 *
 * Created by Felix Beuster
 */
public class DataController {

    private Context context;
    private Resources resources;
    private String[] codes = {"", ""};
    private SharedPreferences pref;
    private HashMap<String, PickupDay> schedule;

    private JsonObject mSyncData;

    private int mFirstYear;
    private int mLastYear;

    public DataController(Context context) {
        this.context        = context;
        this.pref           = PreferenceManager.getDefaultSharedPreferences(context);
        this.resources      = context.getResources();

        schedule = new HashMap<>();

        setYears();
        loadSyncData();
        loadFileData();
    }

    private JsonObject fileToObject(String fileName) {
        try {
            InputStream inputStream = context.openFileInput(fileName);
            return JSONUtils.getJsonObjectFromInputStream(inputStream);
        } catch (Exception e) {
            return null;
        }
    }

    public String[] getCodes() { return codes; }

    public int getFirstYear() {
        return mFirstYear;
    }

    public HashMap<String, PickupDay> getSchedule() { return schedule; }

    public int getLastYear() {
        return mLastYear;
    }

    private int getResourceIdentifier(String name) {
        return resources.getIdentifier(name, "raw", context.getPackageName());
    }

    /**
     * Checks, if a line has a schedule for the day.
     *
     * @param line
     * @return
     */
    private boolean hasLineSchedule(String[] line) {
        for (int i = 0; i < line.length; i++) {
            if (i == 0) continue;

            if (!line[i].isEmpty()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Reads code and schedule data from the files.
     */
    private void loadFileData() {
        SimpleDateFormat yf = new SimpleDateFormat("yyyy");
        Calendar now        = Calendar.getInstance();
        String year         = yf.format(now.getTime());
        String yearLater;

        readCode(year, getResourceIdentifier("codes_" + year), 0);
        readSchedule(year);

        if (DataUtils.isCityWithStreets(codes[0])) {
            String weekly_appendix = "";

            if (codes[0].length() == 6) {
                weekly_appendix = codes[0].substring(4, 6);
            }

            readStreetCode(year, getResourceIdentifier("street_codes_" + year), 0);

            codes[0] = codes[0] + weekly_appendix;
        }

        if (needsMultipleYears()) {
            Calendar later      = Calendar.getInstance();
            later.add(Calendar.YEAR, 1);
            yearLater    = yf.format(later.getTime());

            readCode(yearLater, getResourceIdentifier("codes_" + yearLater), 1);
            readSchedule(yearLater);

            if (DataUtils.isCityWithStreets(codes[1])) {
                String weekly_appendix = "";

                if (codes[1].length() == 6) {
                    weekly_appendix = codes[1].substring(4, 6);
                }

                readStreetCode(yearLater, getResourceIdentifier("street_codes_" + yearLater), 1);

                codes[1] = codes[1] + weekly_appendix;
            }
        }
    }

    /**
     * Loads current sync data from preferences.
     */
    private void loadSyncData() {
        String syncDataString   = pref.getString(resources.getString(R.string.pref_key_intern_sync_data), "");

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
     * Checks, if the current date requires checking more than one year.
     *
     * @return true, if we need a second year
     */
    public static boolean needsMultipleYears() {
        SimpleDateFormat mf = new SimpleDateFormat("MM");
        Calendar now        = Calendar.getInstance();

        return mf.format(now.getTime()).equals("11") || mf.format(now.getTime()).equals("12");
    }

    /**
     * Reads a schedule string line into the schedule hash map.
     *
     * @param line current line in the schedule
     * @param year current year
     */
    private void parseScheduleLine(String[] line, String year) {
        Can can;

        String date         = year + "-" + line[0] + "-" + line[1];
        int[] colorMap      = {
                Can.INVALID, Can.INVALID,
                Can.BLACK, Can.GREEN,
                Can.BLACK, Can.GREEN,
                Can.YELLOW, Can.BLUE,
                Can.BLACK, Can.BLACK,
                Can.GREEN, Can.GREEN
        };
        int[] monthlyMap    = {
                Can.SCHEDULE_BIWEEKLY, Can.SCHEDULE_BIWEEKLY,
                Can.SCHEDULE_MONTHLY, Can.SCHEDULE_MONTHLY,
                Can.SCHEDULE_BIWEEKLY, Can.SCHEDULE_BIWEEKLY,
                Can.SCHEDULE_BIWEEKLY, Can.SCHEDULE_MONTHLY,
                Can.SCHEDULE_WEEKLY, Can.SCHEDULE_TWICE_A_WEEK,
                Can.SCHEDULE_WEEKLY, Can.SCHEDULE_TWICE_A_WEEK
        };

        if (schedule.get(date) == null) {
            schedule.put(date, new PickupDay());
        }

        // iterating schedule day fields
        for (int i = 2; i < line.length; i++) {
            if (!line[i].isEmpty()) {

                // iterating field letters
                for (int j = 0; j < line[i].length(); j++) {
                    if (i < colorMap.length && i < monthlyMap.length) {
                        can = new Can(monthlyMap[i], colorMap[i], line[i].charAt(j));
                        schedule.get(date).addCan(can);
                    }
                }
            }
        }
    }

    /**
     * Reads the pickup code for the saved town
     * from stored sync data into the codes array.
     *
     * @param year current year
     * @param resourceId resource id for the code file of the year
     * @param index year index
     */
    private void readCode(String year, int resourceId, int index) {
        if (mSyncData.has(year)) {
            JsonObject yearObject = mSyncData.getAsJsonObject(year);

            if (yearObject.has("codes")) {

                JsonObject object   = fileToObject("codes_" + year + ".json");
                String town         = pref.getString(resources.getString(R.string.pref_key_pickup_town), "");

                if (object != null && object.has(town)) {
                    codes[index] = object.get(town).getAsString();

                } else {
                    codes[index] = "";
                }

            } else {
                readCodeFromResource(resourceId, resources.getString(R.string.pref_key_pickup_town), index);
            }

        } else {
            readCodeFromResource(resourceId, resources.getString(R.string.pref_key_pickup_town), index);
        }
    }

    /**
     * Reads the pickup code for the saved town
     * from resources into the codes array.
     *
     * @param resourceId resource id for the code file of the year
     * @param prefKey preference key to get codes
     * @param index year index
     */
    private void readCodeFromResource(int resourceId, String prefKey, int index) {
        if (resourceId > 0) {
            try {
                InputStream stream = resources.openRawResource(resourceId);

                int size = stream.available();
                byte[] buffer = new byte[size];

                stream.read(buffer);
                stream.close();

                JSONObject codes = new JSONObject(new String(buffer, "UTF-8"));
                this.codes[index] = codes.getString(pref.getString(prefKey, ""));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads the schedule file from storage or resource.
     *
     * @param year current year
     */
    private void readSchedule(String year) {
        if (mSyncData.has(year)) {
            JsonObject yearObject = mSyncData.getAsJsonObject(year);

            if (yearObject.has("schedule")) {
                readScheduleFromStorage(year);

            } else {
                readScheduleFromResource(year);
            }

        } else {
            readScheduleFromResource(year);
        }
    }

    /**
     * Reads the schedule file from resources.
     *
     * @param year current year
     */
    private void readScheduleFromResource(String year) {
        InputStreamReader inputStreamReader;

        try {
            int scheduleId = resources.getIdentifier("raw/schedule_" + year, "raw", context.getPackageName());

            if (scheduleId > 0) {
                inputStreamReader = new InputStreamReader(resources.openRawResource(scheduleId));
                Scanner inputStream = new Scanner(inputStreamReader);

                inputStream.nextLine();

                while (inputStream.hasNext()) {
                    String data = inputStream.nextLine();
                    String[] line = data.split(",");

                    if (hasLineSchedule(line)) {
                        parseScheduleLine(line, year);
                    }
                }
                inputStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the schedule file from the storad sync data.
     *
     * @param year current year
     */
    private void readScheduleFromStorage(String year) {
        FileInputStream inputStream;

        try {
            inputStream = context.openFileInput("schedule_" + year + ".csv");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader       = new BufferedReader(inputStreamReader);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String[] lineElements = line.split(",");

                if (hasLineSchedule(lineElements)) {
                    parseScheduleLine(lineElements, year);
                }
            }

            inputStreamReader.close();

        } catch (Exception ignored) {
        }
    }

    /**
     * Reads the pickup code for the saved street
     * from the stored sync data into the codes array.
     *
     * @param year current year
     * @param resourceId resource id for street code file of the year
     * @param index year index
     */
    private void readStreetCode(String year, int resourceId, int index) {
        if (mSyncData.has(year)) {
            JsonObject yearObject = mSyncData.getAsJsonObject(year);

            if (yearObject.has("street_codes")) {

                JsonObject object   = fileToObject("street_codes_" + year + ".json");
                String street       = pref.getString(resources.getString(R.string.pref_key_pickup_street), "");

                if (object != null && object.has(street)) {
                    codes[index] = object.get(street).getAsString();

                } else {
                    codes[index] = "";
                }

            } else {
                readCodeFromResource(resourceId, resources.getString(R.string.pref_key_pickup_street), index);
            }

        } else {
            readCodeFromResource(resourceId, resources.getString(R.string.pref_key_pickup_street), index);
        }
    }

    /**
     * Sets the first and last year.
     */
    private void setYears() {
        Calendar now    = Calendar.getInstance();
        mFirstYear      = now.get(Calendar.YEAR);
        mLastYear       = mFirstYear;

        if (needsMultipleYears()) {
            mLastYear++;
        }
    }
}
