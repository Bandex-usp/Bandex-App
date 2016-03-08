package br.usp.ime.bandex.model;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Wagner on 22/02/2016.
 */
public class Fisica extends Bandex {

    private static Fisica me = null;

    public static void initialize(JSONObject jsonBandex) {
        me = new Fisica(jsonBandex);
    }

    public static Fisica getInstance() {
        return me;
    }

    private Fisica(JSONObject jsonBandex) {
        super(jsonBandex);
    }

    public int getId() {
        return 2;
    }

    @Override
    public String getName() {
        return "Física";
    }
}
