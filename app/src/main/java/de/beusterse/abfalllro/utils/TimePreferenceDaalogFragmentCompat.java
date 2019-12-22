package de.beusterse.abfalllro.utils;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import de.beusterse.abfalllro.R;

public class TimePreferenceDaalogFragmentCompat extends PreferenceDialogFragmentCompat {

    private TimePicker timePicker;

    public static TimePreferenceDaalogFragmentCompat newInstance(String key) {
        final Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, key);

        final TimePreferenceDaalogFragmentCompat fragment = new TimePreferenceDaalogFragmentCompat();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        timePicker = view.findViewById(R.id.timePicker);

        if (timePicker != null) {
            DialogPreference preference = getPreference();

            if (preference instanceof TimePreference) {
                TimePreference timePreference = (TimePreference) preference;
                String time = timePreference.getTime();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    timePicker.setHour(timePreference.getHour(time));
                    timePicker.setMinute(timePreference.getMinute(time));
                } else {
                    timePicker.setCurrentHour(timePreference.getHour(time));
                    timePicker.setCurrentMinute(timePreference.getMinute(time));
                }

                boolean is24HourFormat = android.text.format.DateFormat.is24HourFormat(getContext());
                timePicker.setIs24HourView(is24HourFormat);
            }
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int hour;
            int minute;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hour = timePicker.getHour();
                minute = timePicker.getMinute();
            } else {
                hour = timePicker.getCurrentHour();
                minute = timePicker.getCurrentMinute();
            }

            String time = hour + ":" + minute;

            DialogPreference preference = getPreference();

            if (preference instanceof TimePreference) {
                TimePreference timePreference = (TimePreference) preference;

                timePreference.persistTime(time);
                timePreference.callChangeListener(time);
            }
        }
    }
}
