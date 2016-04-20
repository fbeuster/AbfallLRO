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

    private static final String DATE_FORMAT_NO_DAY = "yyyy-MM-";
    private static final String CITY_WITH_STREETS = "0000";

    private Resources resources;
    private String code;
    private String packageName;
    private SharedPreferences pref;
    private HashMap<String, PickupDay> schedule;


    public DataLoader(SharedPreferences pref, Resources resources, String packageName) {
        this.packageName    = packageName;
        this.pref           = pref;
        this.resources      = resources;

        code = "";
        schedule = new HashMap<>();

        readSchedule();

        readCodeFromFile(R.raw.codes, SettingsActivity.KEY_PREF_LOCATION);

        if (code.equals(CITY_WITH_STREETS)) {
            readCodeFromFile(R.raw.street_codes, SettingsActivity.KEY_PREF_LOCATION_STREET);
        }
    }

    public String getCode() { return code; }

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

        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_NO_DAY);

        String date         = df.format(time.getTime()) + line[0];

        int[] colorMap      = { -1,
                Can.BLACK, Can.GREEN,
                Can.BLACK, Can.GREEN,
                Can.YELLOW, Can.BLUE };

        boolean[] monthlyMap = {    false, true, true,
                                    false, false, false,
                                    true };

        if (schedule.get(date) == null) {
            schedule.put(date, new PickupDay());
        }

        for (int i = 1; i < line.length; i++) {
            if (!line[i].isEmpty()) {
                can = new Can(monthlyMap[i], colorMap[i], line[i].charAt(0));
                schedule.get(date).addCan(can);
            }
        }
    }

    private void readCodeFromFile(int resourceId, String prefKey) {
        try {
            InputStream stream = resources.openRawResource(resourceId);

            int size = stream.available();
            byte[] buffer = new byte[size];

            stream.read(buffer);
            stream.close();

            JSONObject codes = new JSONObject( new String(buffer, "UTF-8") );
            code = codes.getString( pref.getString(prefKey, "") );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readSchedule() {
        InputStreamReader inputStreamReader;

        try {
            SimpleDateFormat df = new SimpleDateFormat("MM");
            Calendar now        = Calendar.getInstance();

            ArrayList<Calendar> dates = new ArrayList<>();
            dates.add(now);

            if (!df.format(now.getTime()).equals("12")) {
                Calendar next = Calendar.getInstance();
                next.add(Calendar.MONTH, 1);
                dates.add(next);

                if (!df.format(now.getTime()).equals("11")) {
                    Calendar nextnext = Calendar.getInstance();
                    nextnext.add(Calendar.MONTH, 2);
                    dates.add(nextnext);
                }
            }

            for(Calendar current : dates) {
                int scheduleId = resources.getIdentifier("raw/schedule" + df.format(current.getTime()), "raw", packageName);

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
