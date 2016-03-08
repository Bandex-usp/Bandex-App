package br.usp.ime.bandex.model;

import br.usp.ime.bandex.Util;

/**
 * Created by Wagner on 07/03/2016.
 */
public class BandexFactory {

    public static Bandex getRestaurant(int id) {
        Bandex restaurant = null;
        switch (id) {
            case Util.Bandejao.CENTRAL:
                restaurant = Central.getInstance();
                break;
            case Util.Bandejao.FISICA:
                restaurant = Fisica.getInstance();
                break;
            case Util.Bandejao.QUIMICA:
                restaurant = Quimica.getInstance();
                break;
            case Util.Bandejao.PCO:
                restaurant = PCO.getInstance();
                break;
        }
        return restaurant;
    }
}
