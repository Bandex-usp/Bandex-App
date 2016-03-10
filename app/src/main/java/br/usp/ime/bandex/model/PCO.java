package br.usp.ime.bandex.model;

import org.json.JSONObject;

/**
 * Created by Wagner on 22/02/2016.
 */
public class PCO extends Bandex {

    private static PCO me = null;

    public static PCO getInstance() {
        return me;
    }

    public static void initialize(JSONObject jsonBandex) {
        me = new PCO(jsonBandex);
    }

    private PCO(JSONObject jsonBandex) {
        super(jsonBandex);
    }

    public int getId() {
        return 3;
    }

    @Override
    public String getName() {
        return "Prefeitura";
    }


}
