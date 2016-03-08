package br.usp.ime.bandex.model;

import org.json.JSONObject;

/**
 * Created by Wagner on 26/02/2016.
 */
public class Lunch extends Meal {

    public Lunch(JSONObject jsonMeal) {
        super(jsonMeal);
    }

    @Override
    public String getName() {
        return "Almoço";
    }
}
