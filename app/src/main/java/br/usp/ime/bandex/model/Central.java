package br.usp.ime.bandex.model;

import android.app.Activity;

import org.json.JSONObject;

import br.usp.ime.bandex.MainActivity;
import br.usp.ime.bandex.Util;

/**
 * Created by Wagner on 22/02/2016.
 */
public class Central extends Bandex {

    private static Central me = null;

    public static void initialize(JSONObject jsonBandex) {
        me = new Central(jsonBandex);
    }

    public static Central getInstance() {
        return me;
    }

    private Central(JSONObject jsonBandex) {
        super(jsonBandex);
    }

    public int getId() {
        return 0;
    }

    @Override
    public String getName() {
        return "Central";
    }
}
