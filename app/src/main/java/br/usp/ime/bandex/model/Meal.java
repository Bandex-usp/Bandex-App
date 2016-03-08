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
            setMeat("Restaurante Fechado");
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

    public void setMain(String main) {
        this.main = main;
    }

    public String getMeat() {
        return meat;
    }

    public void setMeat(String meat) {
        this.meat = meat;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public String getSalad() {
        return salad;
    }

    public void setSalad(String salad) {
        this.salad = salad;
    }

    public String getOptional() {
        return optional;
    }

    public void setOptional(String optional) {
        this.optional = optional;
    }

    public String getDesert() {
        return desert;
    }

    public void setDesert(String desert) {
        this.desert = desert;
    }

    public String getCalories() {
        return "" + calories + " kcal";
    }

    public void setCalories(int calories) {
        this.calories = calories;
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
