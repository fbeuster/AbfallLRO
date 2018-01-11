package de.beusterse.abfalllro.utils;

/**
 * Utility functions for arrays
 *
 * Created by Felix Beuster on 1/11/2018.
 */

public class ArrayUtils {
    /**
     * @param haystack array to be searched
     * @param needle value to be found
     * @return index of needle in haystack, -1 on not found
     */
    public static int indexOf(String[] haystack, String needle) {
        int i = -1;

        for (String hay : haystack) {
            i++;

            if (hay.equals(needle)) {
                return i;
            }
        }

        return -1;
    }
}
