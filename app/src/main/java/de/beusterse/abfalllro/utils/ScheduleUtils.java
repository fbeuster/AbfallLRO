package de.beusterse.abfalllro.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.capsules.Can;

/**
 * Utility functions related to cans
 *
 * Created by Felix Beuster on 1/7/2018.
 */

public class ScheduleUtils {

    public static int getSavedScheduleForCan(int can, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String[] schedule_names = context.getResources().getStringArray(R.array.pref_general_schedule_list);
        String savedSchedule;
        String scheduleBiweekly = context.getString(R.string.pref_can_schedule_biweekly);
        String scheduleMonthly = context.getString(R.string.pref_can_schedule_monthly);

        switch (can) {
            case Can.BLACK :
                savedSchedule = sharedPreferences.getString(context.getString(R.string.pref_key_pickup_schedule_black), scheduleBiweekly);
                return ArrayUtils.indexOf(schedule_names, savedSchedule);
            case Can.BLUE :
                savedSchedule = sharedPreferences.getString(context.getString(R.string.pref_key_pickup_schedule_blue), scheduleMonthly);
                return ArrayUtils.indexOf(schedule_names, savedSchedule);
            case Can.GREEN :
                savedSchedule = sharedPreferences.getString(context.getString(R.string.pref_key_pickup_schedule_green), scheduleBiweekly);
                return ArrayUtils.indexOf(schedule_names, savedSchedule);
            case Can.YELLOW :
                savedSchedule = sharedPreferences.getString(context.getString(R.string.pref_key_pickup_schedule_yellow), scheduleMonthly);
                return ArrayUtils.indexOf(schedule_names, savedSchedule);
            default:
                break;
        }

        return -1;
    }

    public static boolean hasNoCanSchedulesConfigured(Context context) {
        return getSavedScheduleForCan(Can.BLACK, context) == Can.SCHEDULE_NEVER &&
                getSavedScheduleForCan(Can.BLUE, context) == Can.SCHEDULE_NEVER &&
                getSavedScheduleForCan(Can.GREEN, context) == Can.SCHEDULE_NEVER &&
                getSavedScheduleForCan(Can.YELLOW, context) == Can.SCHEDULE_NEVER;
    }

    public static String scheduleIntToString(int schedule) {
        switch (schedule) {
            case Can.SCHEDULE_NEVER:        return "never";
            case Can.SCHEDULE_MONTHLY:      return "monthly";
            case Can.SCHEDULE_BIWEEKLY:     return "biweekly";
            case Can.SCHEDULE_WEEKLY:       return "weekly";
            case Can.SCHEDULE_TWICA_A_WEEK: return "twice a week";
            default:                        return "error";
        }
    }
}