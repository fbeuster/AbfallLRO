package de.beusterse.abfalllro.utils;

import android.widget.Spinner;

/**
 * Utility functions for spinner elements
 *
 * Created by Felix Beuster on 1/11/2018.
 */

public class SpinnerUtils {
    /**
     * @param spinner Spinner to be searched
     * @param value item to be found
     * @return index of value in spinner, -1 on not found
     */
    public static int indexOf(Spinner spinner, String value) {
        int index = -1;

        for (int i = 0; i < spinner.getCount(); i++){
            if (spinner.getItemAtPosition(i).toString().equals(value)){
                index = i;
                break;
            }
        }

        return index;
    }
}
