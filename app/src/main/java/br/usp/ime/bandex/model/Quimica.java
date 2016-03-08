package br.usp.ime.bandex.model;

import org.json.JSONObject;

/**
 * Created by Wagner on 22/02/2016.
 */
public class Quimica extends Bandex {

    private static Quimica me = null;

    public static Quimica getInstance() {
        return me;
    }

    private Quimica(JSONObject jsonBandex) {
        super(jsonBandex);
    }

    public static void initialize(JSONObject jsonBandex) {
        me = new Quimica(jsonBandex);
    }

    public int getId() {
        return 1;
    }

    @Override
    public String getName() {
        return "Química";
    }
}
