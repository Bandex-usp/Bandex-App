package br.usp.ime.bandex.model;

import br.usp.ime.bandex.Util.Bandejao;

/**
 * Created by Wagner on 07/03/2016.
 */
public class BandexFactory {

    public static Bandex getRestaurant(Bandejao bandejao) {
        return bandejao.getInstance();
    }
}
