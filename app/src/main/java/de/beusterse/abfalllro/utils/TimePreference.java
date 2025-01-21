package de.beusterse.abfalllro.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.format.DateFormat;
import android.util.AttributeSet;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import de.beusterse.abfalllro.R;

/**
 * Custom preference for a time picker dialog
 * Created by Felix Beuster
 *
 * Kudos for this goes to commonsguy as well.
 * https://github.com/commonsguy/cw-lunchlist/blob/master/19-Alarm/LunchList/src/apt/tutorial/TimePreference.java
 */
public class TimePreference extends DialogPreference {
    private int dialogLayoutResId = R.layout.pref_alert_time_dioalog;
    private String time;

    public TimePreference(Context context) {
        this(context, null);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setDialogLayoutResource(R.layout.pref_alert_time_dioalog);
        setDialogIcon(null);
    }

    public static int getHour(String time) {
        String[] pieces = time.split(":");

        return(Integer.parseInt(pieces[0]));
    }

    public static int getMinute(String time) {
        String[] pieces = time.split(":");

        return (Integer.parseInt(pieces[1]));
    }

    public String getTime() {
        return time;
    }

    public void persistTime(String time) {
        this.time = time;
        persistString(time);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return(a.getString(index));
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        if (defaultValue == null) {
            time = getPersistedString("00:00");

        } else {
            time = getPersistedString(defaultValue.toString());
        }
    }

    @Override
    public int getDialogLayoutResource() {
        return dialogLayoutResId;
    }

    public String getSummary() {
        String format;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, getHour(time));
        cal.set(Calendar.MINUTE, getMinute(time));

        if (DateFormat.is24HourFormat(getContext())) {
            format = "HH:mm";
        } else {
            format = "h:mm a";
        }

        SimpleDateFormat df = new SimpleDateFormat(format);
        String time = df.format(cal.getTime());

        return time;
    }
}
