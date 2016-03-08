package br.usp.ime.bandex.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Wagner on 07/05/2015.
 */
public abstract class Bandex {
    private Day[] days;
    private int lineStatus;
    private Date lastSubmit;
    private int id;

    public String getFormattedLastSubmit() {
        return new SimpleDateFormat("dd/MM/yyyy").format(lastSubmit);
    }
    public int getId() {
        return id;
    }

    public Bandex(JSONObject jsonBandex) {
        days = new Day[7];
        JSONArray jsonArrayBandexDays = null;
        try {
            jsonArrayBandexDays = jsonBandex.getJSONArray("days");
            for (int i = 0; i < jsonArrayBandexDays.length(); i++) { // percorre o array de dias do json
                JSONObject jsonDay = jsonArrayBandexDays.getJSONObject(i);
                String date = jsonDay.getString("entry_date");
                Lunch lunch = new Lunch(jsonDay.optJSONObject("lunch"));
                Dinner dinner = new Dinner(jsonDay.optJSONObject("dinner"));
                days[i] = new Day(new Meal[]{lunch, dinner});
                days[i].setDateName(date);
            } // Array de dias do json
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Day getDay(int dayOfWeek) {
        return days[dayOfWeek];
    }

    public void setDays(Day[] days) {
        this.days = days;
    }

    public abstract String getName();

    public int getLineStatus() {
        return lineStatus;
    }

    public Date getLastSubmit() {
        return lastSubmit;
    }

    public void setLastSubmit(Date lastSubmit) {
        this.lastSubmit = lastSubmit;
    }

    public void setLineStatus(int lineStatus) {
        this.lineStatus = lineStatus;
    }

}
