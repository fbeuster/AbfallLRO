package de.beusterse.abfalllro;

import java.util.ArrayList;


/**
 * Holds a single pickup day
 *
 * Created by Felix Beuster
 */
public class PickupDay {

    private ArrayList<Can> cans;

    public PickupDay() {
        this.cans = new ArrayList<>();
    }

    public void addCan(Can can) {
        this.cans.add(can);
    }
    
    public String toString() {
        String result = "";
        for (Can can : cans) {
            result += can + "; ";
        }
        return result;
    }

    public boolean hasCan(boolean monthly, String color, char letter) {
        for (Can can : cans) {
            if (can.isMonthly() == monthly && can.getColor().equals(color) && can.getLetter() == letter) {
                return true;
            }
        }
        return false;
    }
}
