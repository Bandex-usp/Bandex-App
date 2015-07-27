package br.usp.ime.bandex.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.text.style.TtsSpan;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import br.usp.ime.bandex.MainActivity;
import br.usp.ime.bandex.R;
import br.usp.ime.bandex.Util;

/**
 * Created by Wagner on 08/05/2015.
 */
public class Day {
    Date entry_date;
    int dayOfWeek;
    Cardapio lunch;
    Cardapio dinner;
    Cardapio[] day;

    public Cardapio[] getDay() {
        return day;
    }

    public Day(String data, Cardapio lunch, Cardapio dinner, MainActivity caller) {
        day = new Cardapio[2];
        day[0] = lunch;
        day[1] = dinner;
        try {
            entry_date = new SimpleDateFormat("yyyy-MM-dd").parse(data);
        } catch (ParseException e) {
            Log.e("parseData", "erro ao parsear");
            e.printStackTrace();
        }
        this.lunch = lunch;
        this.dinner = dinner;
        Util.setEntry_date(entry_date);
        SharedPreferences sharedPref = caller.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(caller.getString(R.string.preferences_entry_date_cache), data);
        editor.commit();
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

    public Date getEntry_date() {
        return (Date)entry_date.clone();
    }

    public void setEntry_date(Date entry_date) {
        this.entry_date = (Date)entry_date.clone();
    }
}
