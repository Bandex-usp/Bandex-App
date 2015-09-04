package br.usp.ime.bandex.model;

import java.util.Date;
import java.util.List;

/**
 * Created by Wagner on 07/05/2015.
 */
public class Bandex {
    int bandex_id;
    List<Day> days;
    int lineStatus;
    Date last_submit;

    public int getLineStatus() {
        return lineStatus;
    }

    public Date getLast_submit() {
        return last_submit;
    }

    public void setLast_submit(Date last_submit) {
        this.last_submit = last_submit;
    }

    public void setLineStatus(int lineStatus) {
        this.lineStatus = lineStatus;
    }

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
