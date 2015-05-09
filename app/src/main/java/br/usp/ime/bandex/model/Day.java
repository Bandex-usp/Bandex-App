package br.usp.ime.bandex.model;

import android.text.format.DateFormat;
import android.text.style.TtsSpan;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Wagner on 08/05/2015.
 */
public class Day {
    String entry_date;
    int dayOfWeek;
    Cardapio lunch;
    Cardapio dinner;
    Cardapio[] day;

    public Cardapio[] getDay() {
        return day;
    }

    public Day(String entry_date, Cardapio lunch, Cardapio dinner) {
        Date date;
        day = new Cardapio[2];
        day[0] = lunch;
        day[1] = dinner;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(entry_date);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        } catch (ParseException e) {
            Log.e("parseData", "erro ao parsear");
            e.printStackTrace();
        }

        this.entry_date = entry_date;
        this.lunch = lunch;
        this.dinner = dinner;
    }

    public Cardapio getLunch() {
        return lunch;
    }

    public void setLunch(Cardapio lunch) {
        this.lunch = lunch;
    }

    public Cardapio getDinner() {
        return dinner;
    }

    public void setDinner(Cardapio dinner) {
        this.dinner = dinner;
    }

    public String getEntry_date() {
        return entry_date;
    }

    public void setEntry_date(String entry_date) {
        this.entry_date = entry_date;
    }
}
