package br.usp.ime.bandex;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Wagner on 26/07/2015.
 */
public class Util {
    static Calendar cal = Calendar.getInstance();
    static int hours = cal.get(Calendar.HOUR);
    static int minutes = cal.get(Calendar.MINUTE);
    static int period = 0; // 0 = lunch, 1 = dinner
    static int day_of_week = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Calendar.Monday == 2. In this code, Monday = 0.
    static Date entry_date = null;

    static {
        // Choose whether to show the lunch or the dinner
        if (hours >= 14 && minutes >= 30)
            period = 1;
        else period = 0;
    }

    public static void setEntry_date(Date entry_date) {
        Util.entry_date = entry_date;

    }
}
