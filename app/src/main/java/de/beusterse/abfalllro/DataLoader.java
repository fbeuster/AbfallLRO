package de.beusterse.abfalllro;

import android.content.SharedPreferences;
import android.content.res.Resources;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;

import de.beusterse.abfalllro.capsules.Can;
import de.beusterse.abfalllro.capsules.PickupDay;


/**
 * Loads raw data and structures them
 *
 * Created by Felix Beuster
 */
public class DataLoader {

    private static final String CITY_WITH_STREETS = "0000";

    private Resources resources;
    private String[] codes = {"", ""};
    private String packageName;
    private SharedPreferences pref;
    private HashMap<String, PickupDay> schedule;


    public DataLoader(SharedPreferences pref, Resources resources, String packageName) {
        this.packageName    = packageName;
        this.pref           = pref;
        this.resources      = resources;

        schedule = new HashMap<>();

        readSchedule();

        readCodeFromFile(R.raw.codes_2016, resources.getString(R.string.pref_key_pickup_town), 0);

        if (codes.equals(CITY_WITH_STREETS)) {
            readCodeFromFile(R.raw.street_codes_2016, resources.getString(R.string.pref_key_pickup_street), 0);
        }

        readCodeFromFile(R.raw.codes_2017, resources.getString(R.string.pref_key_pickup_town), 1);

        if (codes.equals(CITY_WITH_STREETS)) {
            readCodeFromFile(R.raw.street_codes_2017, resources.getString(R.string.pref_key_pickup_street), 1);
        }
    }

    public String[] getCodes() { return codes; }

    public HashMap<String, PickupDay> getSchedule() { return schedule; }

    private boolean hasLineSchedule(String[] line) {
        for (int i = 0; i < line.length; i++) {
            if (i == 0) continue;

            if (!line[i].isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private void parseScheduleLine(String[] line, Calendar time) {
        Can can;

        SimpleDateFormat df = new SimpleDateFormat("yyyy");

        String date         = df.format(time.getTime()) + "-" + line[0] + "-" + line[1];

        int[] colorMap      = { -1, -1,
                Can.BLACK, Can.GREEN,
                Can.BLACK, Can.GREEN,
                Can.YELLOW, Can.BLUE };

        boolean[] monthlyMap = {    false, false,
                                    true, true,
                                    false, false,
                                    false, true };

        if (schedule.get(date) == null) {
            schedule.put(date, new PickupDay());
        }

        for (int i = 2; i < line.length; i++) {
            if (!line[i].isEmpty()) {
                can = new Can(monthlyMap[i], colorMap[i], line[i].charAt(0));
                schedule.get(date).addCan(can);
            }
        }
    }

    private void readCodeFromFile(int resourceId, String prefKey, int index) {
        try {
            InputStream stream = resources.openRawResource(resourceId);

            int size = stream.available();
            byte[] buffer = new byte[size];

            stream.read(buffer);
            stream.close();

            JSONObject codes = new JSONObject( new String(buffer, "UTF-8") );
            this.codes[index] = codes.getString( pref.getString(prefKey, "") );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readSchedule() {
        InputStreamReader inputStreamReader;

        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy");
            SimpleDateFormat mf = new SimpleDateFormat("MM");
            Calendar now        = Calendar.getInstance();

            ArrayList<Calendar> dates = new ArrayList<>();
            dates.add(now);

            if (mf.format(now.getTime()).equals("11") || mf.format(now.getTime()).equals("12")) {
                Calendar next = Calendar.getInstance();
                next.add(Calendar.YEAR, 1);
                dates.add(next);
            }

            for(Calendar current : dates) {
                int scheduleId = resources.getIdentifier("raw/schedule_" + df.format(current.getTime()), "raw", packageName);

                inputStreamReader = new InputStreamReader(resources.openRawResource(scheduleId));
                Scanner inputStream = new Scanner(inputStreamReader);

                inputStream.nextLine();

                while (inputStream.hasNext()) {
                    String data = inputStream.nextLine();
                    String[] line = data.split(",");

                    if (hasLineSchedule(line)) {
                        parseScheduleLine(line, current);
                    }
                }
                inputStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
