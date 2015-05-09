package br.usp.ime.linux.wagner.tcc.br.usp.ime.linux.wagner.tcc.model;

import java.util.List;

/**
 * Created by Wagner on 07/05/2015.
 */
public class Bandex {
    int bandex_id;
    List<Day> days;

    public Bandex(int bandex_id, List<Day> days) {
        this.bandex_id = bandex_id;
        this.days = days;
    }

    public int getBandex_id() {
        return bandex_id;
    }

    public void setBandex_id(int bandex_id) {
        this.bandex_id = bandex_id;
    }

    public List<Day> getDays() {
        return days;
    }

    public void setDays(List<Day> days) {
        this.days = days;
    }
}
