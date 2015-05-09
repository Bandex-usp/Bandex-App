package br.usp.ime.bandex.model;

/**
 * Created by Wagner on 08/05/2015.
 */
public class Cardapio {
    String main;
    String meat;
    String second;
    String salad;
    String optional;
    String desert;

    public Cardapio(String main, String meat, String second, String salad, String optional,
                    String desert) {
        this.desert = desert;
        this.main = main;
        this.meat = meat;
        this.optional = optional;
        this.salad = salad;
        this.second = second;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getMeat() {
        return meat;
    }

    public void setMeat(String meat) {
        this.meat = meat;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public String getSalad() {
        return salad;
    }

    public void setSalad(String salad) {
        this.salad = salad;
    }

    public String getOptional() {
        return optional;
    }

    public void setOptional(String optional) {
        this.optional = optional;
    }

    public String getDesert() {
        return desert;
    }

    public void setDesert(String desert) {
        this.desert = desert;
    }
}
