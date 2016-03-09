package br.usp.ime.bandex.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Wagner on 26/02/2016.
 */
public abstract class Meal {
    private String main = "";
    private String meat = "";
    private String second = "";
    private String salad = "";
    private String optional = "";
    private String desert = "";
    private int calories = 0;
    private String raw = "";
    private boolean available;

    public Meal(JSONObject jsonMeal) {
        if (jsonMeal == null) {
            setAvailable(false);
            return;
        }
        try {
            calories = jsonMeal.getInt("calories");
            main = jsonMeal.getString("main");
            meat = jsonMeal.getString("meat");
            second = jsonMeal.getString("second");
            salad = jsonMeal.getString("salad");
            optional = jsonMeal.getString("optional");
            desert = jsonMeal.getString("desert");
            raw = jsonMeal.getString("raw");
            available = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getMain() {
        return main;
    }


    public String getMeat() {
        return meat;
    }


    public String getSecond() {
        return second;
    }


    public String getSalad() {
        return salad;
    }


    public String getOptional() {
        return optional;
    }


    public String getDesert() {
        return desert;
    }


    public String getCalories() {
        return "" + calories + " kcal";
    }


    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public abstract String getName();

}
