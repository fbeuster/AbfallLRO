package de.beusterse.abfalllro.controller;

import android.content.SharedPreferences;
import android.content.res.Resources;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.capsules.Can;
import de.beusterse.abfalllro.capsules.PickupDay;

/**
 * Processes structured data and returns results for UI
 *
 * Created by Felix Beuster
 */
public class TrashController {
    private static final String DATE_FORMAT = "yyyy-MM-d";
    private static final String CITY_WITH_STREETS = "0000";

    private ArrayList<int[]> cCans;
    private int cError;
    private int[] cPreview;

    private String[] locationCans;
    private Resources resources;
    private HashMap<String, PickupDay> schedule;

    Calendar now;
    SimpleDateFormat dateFormat;

    boolean monthly;
    boolean monthly_black;
    boolean monthly_green;

    private DataController loader;

    public TrashController(SharedPreferences pref, DataController loader, Resources resources) {
        this.loader         = loader;
        this.locationCans   = loader.getCodes();
        this.resources      = resources;
        this.schedule       = loader.getSchedule();

        cCans = new ArrayList<>();
        cError = -1;

        now     = Calendar.getInstance();

        dateFormat = new SimpleDateFormat(DATE_FORMAT);

        monthly         = true;
        monthly_black   = pref.getBoolean(  resources.getString(R.string.pref_key_pickup_schedule_black),
                                            resources.getBoolean(R.bool.pickup_monthly_black));
        monthly_green   = pref.getBoolean(  resources.getString(R.string.pref_key_pickup_schedule_green),
                                            resources.getBoolean(R.bool.pickup_monthly_green));

        calcCurrentCans();
        cPreview = calcPreview(0);
    }

    public ArrayList<int[]> getCans() { return cCans; }

    public int getError() { return cError; }

    public int[] getNextPreview(int dayOffset) {
        return calcPreview(dayOffset);
    }

    public int[] getPreview() { return cPreview; }

    public int getTheme() {
        if (cError != -1 || cCans.size() > 1) {
            return R.style.AppTheme_NoActionBar;

        } else {
            return cCans.get(0)[2];
        }
    }

    private void calcCurrentCans() {
        String current_codes = locationCans[now.get(Calendar.YEAR) - loader.getFirstYear()];

        if (current_codes.length() == 0) {
            cError = R.string.check_invalid_city;

        } else if (locationCans.equals(CITY_WITH_STREETS)) {
            cError = R.string.check_invalid_street;

        } else if (current_codes.length() != 4) {
            cError = R.string.check_invalid_code;

        } else {
            String today    = dateFormat.format(now.getTime());
            PickupDay plan  = schedule.get(today);

            if (plan == null) {
                cError = R.string.check_can_none;

            } else {
                if (plan.hasCan(monthly_black, Can.BLACK, current_codes.charAt(0))) {
                    cCans.add(new int[]{
                            R.string.check_can_black,
                            R.drawable.can_black_scale,
                            R.style.CanBlackTheme_NoActionBar
                    });
                }

                if (plan.hasCan(monthly_green, Can.GREEN, current_codes.charAt(1))) {
                    cCans.add(new int[]{
                            R.string.check_can_green,
                            R.drawable.can_green_scale,
                            R.style.CanGreenTheme_NoActionBar
                    });
                }

                if (plan.hasCan(!monthly, Can.YELLOW, current_codes.charAt(2))) {
                    cCans.add(new int[]{
                            R.string.check_can_yellow,
                            R.drawable.can_yellow_scale,
                            R.style.CanYellowTheme_NoActionBar
                    });
                }

                if (plan.hasCan(monthly, Can.BLUE, current_codes.charAt(3))) {
                    cCans.add(new int[]{
                            R.string.check_can_blue,
                            R.drawable.can_blue_scale,
                            R.style.CanBlueTheme_NoActionBar
                    });
                }

                if (cCans.size() == 0) {
                    cError = R.string.check_can_none;
                }
            }
        }
    }

    private int[] calcPreview(int dayOffset) {
        int found = 0;
        int dayCount = 0;
        int[] preview = {-1, -1, -1, -1};

        if (locationCans[0].length() > 0) {

            Calendar pNow = (Calendar) now.clone();
            Calendar pMaxTime = (Calendar) now.clone();

            pNow.add(Calendar.DATE, dayOffset);
            pMaxTime.add(Calendar.DATE, dayOffset);
            pMaxTime.add(Calendar.MONTH, 2);

            if (pMaxTime.get(Calendar.YEAR) > loader.getLastYear()) {
                pMaxTime.set(Calendar.YEAR, loader.getLastYear());
                pMaxTime.set(Calendar.MONTH, 12);
                pMaxTime.set(Calendar.DATE, 31);
            }

            while (found < 4 && pNow.getTime().before(pMaxTime.getTime())) {
                String today            = dateFormat.format(pNow.getTime());
                PickupDay plan          = schedule.get(today);
                String current_codes    = locationCans[pNow.get(Calendar.YEAR) - loader.getFirstYear()];

                if (plan != null && current_codes.length() > 0) {
                    if (preview[Can.BLACK] == -1 && plan.hasCan(monthly_black, Can.BLACK, current_codes.charAt(0))) {
                        preview[Can.BLACK] = dayCount;
                        found++;
                    }

                    if (preview[Can.GREEN] == -1 && plan.hasCan(monthly_green, Can.GREEN, current_codes.charAt(1))) {
                        preview[Can.GREEN] = dayCount;
                        found++;
                    }

                    if (preview[Can.YELLOW] == -1 && plan.hasCan(!monthly, Can.YELLOW, current_codes.charAt(2))) {
                        preview[Can.YELLOW] = dayCount;
                        found++;
                    }

                    if (preview[Can.BLUE] == -1 && plan.hasCan(monthly, Can.BLUE, current_codes.charAt(3))) {
                        preview[Can.BLUE] = dayCount;
                        found++;
                    }
                }

                pNow.add(Calendar.DATE, 1);
                dayCount++;
            }
        }
        return preview;
    }
}
