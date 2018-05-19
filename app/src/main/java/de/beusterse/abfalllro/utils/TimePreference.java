package de.beusterse.abfalllro.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.beusterse.abfalllro.R;

/**
 * Custom prefrence for a time picker dialog
 * Created by Felix Beuster
 *
 * Kudos for this goes to commonsguy as well.
 * https://github.com/commonsguy/cw-lunchlist/blob/master/19-Alarm/LunchList/src/apt/tutorial/TimePreference.java
 */
public class TimePreference extends DialogPreference {
    private int lastHour        = 0;
    private int lastMinute      = 0;
    private TimePicker picker   = null;

    public static int getHour(String time) {
        String[] pieces = time.split(":");

        return(Integer.parseInt(pieces[0]));
    }

    public static int getMinute(String time) {
        String[] pieces = time.split(":");

        return(Integer.parseInt(pieces[1]));
    }

    public TimePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        setDialogLayoutResource(R.layout.pref_alert_time_dioalog);

        setDialogIcon(null);
    }

    public String getSummary() {
        String format;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, lastHour);
        cal.set(Calendar.MINUTE, lastMinute);

        if (DateFormat.is24HourFormat(getContext())) {
            format = "HH:mm";
        } else {
            format = "h:mm a";
        }

        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(cal.getTime());
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());

        boolean is24HourFormat = DateFormat.is24HourFormat(getContext());
        picker.setIs24HourView(is24HourFormat);

        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        if (Build.VERSION.SDK_INT >= 23) {
            picker.setHour(lastHour);
            picker.setMinute(lastMinute);

        } else {
            picker.setCurrentHour(lastHour);
            picker.setCurrentMinute(lastMinute);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            if (Build.VERSION.SDK_INT >= 23) {
                lastHour    = picker.getHour();
                lastMinute  = picker.getMinute();

            } else {
                lastHour    = picker.getCurrentHour();
                lastMinute  = picker.getCurrentMinute();
            }

            String time = String.valueOf(lastHour) + ":" + String.valueOf(lastMinute);

            if (callChangeListener(time)) {
                persistString(time);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return(a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time = null;

        if (restoreValue) {
            if (defaultValue == null) {
                time = getPersistedString("00:00");

            } else {
                time = getPersistedString(defaultValue.toString());
            }

        } else {
            time = defaultValue.toString();
        }

        lastHour    = getHour(time);
        lastMinute  = getMinute(time);
    }
}
