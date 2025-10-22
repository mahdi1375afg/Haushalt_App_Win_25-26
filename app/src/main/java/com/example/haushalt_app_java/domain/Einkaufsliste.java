package com.example.haushalt_app_java.domain;

import com.google.firebase.database.ServerValue;
import java.util.ArrayList;
import java.util.List;

public class Einkaufsliste {

    private String einkaufslist_id;
    private String haus_id;
    private String name;
    private List<Produkt> produkte;
    private Object datum;
    public Einkaufsliste() {
    }

    public Einkaufsliste(String einkaufslist_id, String haus_id, String name) {
        this.einkaufslist_id = einkaufslist_id;
        this.haus_id = haus_id;
        this.produkte = new ArrayList<>();
        this.datum = ServerValue.TIMESTAMP;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEinkaufslist_id() {
        return einkaufslist_id;
    }

    public void setEinkaufslist_id(String einkaufslist_id) {
        this.einkaufslist_id = einkaufslist_id;
    }

    public String getHaus_id() {
        return haus_id;
    }

    public void setHaus_id(String haus_id) {
        this.haus_id = haus_id;
    }

    public List<Produkt> getProdukte() {
        return produkte;
    }

    public void setProdukte(List<Produkt> produkte) {
        this.produkte = produkte;
    }

    public Object getDatum() {
        return datum;
    }

    public void setDatum(Object datum) {
        this.datum = datum;
    }

    public void addProdukt(Produkt p) {
        if (produkte == null) {
            produkte = new ArrayList<>();
        }
        produkte.add(p);
    }

    public void removeProdukt(Produkt p) {
        if (produkte != null) {
            produkte.remove(p);
        }
    }
}
