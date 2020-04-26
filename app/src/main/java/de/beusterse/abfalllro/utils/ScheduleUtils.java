package de.beusterse.abfalllro.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.capsules.Can;
import de.beusterse.abfalllro.capsules.Schedule;

/**
 * Utility functions related to cans
 *
 * Created by Felix Beuster on 1/7/2018.
 */

public class ScheduleUtils {

    public static int getSavedScheduleForCan(int can, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String savedSchedule;
        String scheduleBiweekly = Schedule.BIWEEKLY.name();
        String scheduleMonthly = Schedule.MONTHLY.name();

        switch (can) {
            case Can.BLACK :
                savedSchedule = sharedPreferences.getString(context.getString(R.string.pref_key_pickup_schedule_black), scheduleBiweekly);
                return mapScheduleToInt(savedSchedule);
            case Can.BLUE :
                savedSchedule = sharedPreferences.getString(context.getString(R.string.pref_key_pickup_schedule_blue), scheduleMonthly);
                return mapScheduleToInt(savedSchedule);
            case Can.GREEN :
                savedSchedule = sharedPreferences.getString(context.getString(R.string.pref_key_pickup_schedule_green), scheduleBiweekly);
                return mapScheduleToInt(savedSchedule);
            case Can.YELLOW :
                savedSchedule = sharedPreferences.getString(context.getString(R.string.pref_key_pickup_schedule_yellow), scheduleBiweekly);
                return mapScheduleToInt(savedSchedule);
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

    public static Schedule mapIntToSchedule(int schedule) {
        switch (schedule) {
            case Can.SCHEDULE_NEVER:        return Schedule.NEVER;
            case Can.SCHEDULE_MONTHLY:      return Schedule.MONTHLY;
            case Can.SCHEDULE_BIWEEKLY:     return Schedule.BIWEEKLY;
            case Can.SCHEDULE_WEEKLY:       return Schedule.WEEKLY;
            case Can.SCHEDULE_TWICE_A_WEEK: return Schedule.TWICE_A_WEEK;
            default:                        return Schedule.NEVER;
        }
    }

    private static int mapScheduleToInt(String schedule)
    {
        switch (Schedule.valueOf(schedule)) {
            case NEVER:         return Can.SCHEDULE_NEVER;
            case MONTHLY:       return Can.SCHEDULE_MONTHLY;
            case BIWEEKLY:      return Can.SCHEDULE_BIWEEKLY;
            case WEEKLY:        return Can.SCHEDULE_WEEKLY;
            case TWICE_A_WEEK:  return Can.SCHEDULE_TWICE_A_WEEK;
            default:            return 0;
        }
    }
}
