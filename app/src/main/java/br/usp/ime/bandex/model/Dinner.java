package br.usp.ime.bandex.model;

import org.json.JSONObject;

/**
 * Created by Wagner on 26/02/2016.
 */
public class Dinner extends Meal {

    public Dinner(JSONObject jsonMeal) {
        super(jsonMeal);
    }

    @Override
    public String getName() {
        return "Jantar";
    }
}
