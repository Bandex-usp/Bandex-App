package br.usp.ime.bandex.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Wagner on 26/02/2016.
 */
public class Day {
    private Date date;

    public String getDateName() {
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    public void setDateName(String dateName) {
        this.dateName = dateName;
        try {
            this.date = new SimpleDateFormat("yyyy-MM-dd").parse(dateName);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String dateName;
    private String dayOfWeek;
    private Meal meals[];

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public Day(Meal meals[]) {
        this.meals = meals;
    }


    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Meal getMeal(int period) {
        return meals[period];
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
