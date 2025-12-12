package com.example.haushalt_app_java.einkaufsliste;

import com.example.haushalt_app_java.produkt.Produkt;

import java.util.HashMap;
import java.util.Map;

public class Einkaufsliste {
    private String einkaufslist_id;
    private String haus_id;
    private String name;
    private Map<String, Produkt> produkte;  // ✅ ÄNDERUNG: List → Map

    public Einkaufsliste() {}

    public Einkaufsliste(String einkaufslist_id, String haus_id, String name) {
        this.einkaufslist_id = einkaufslist_id;
        this.haus_id = haus_id;
        this.name = name;
        this.produkte = new HashMap<>();  // ✅ HashMap statt ArrayList
    }

    // Getter und Setter
    public String getEinkaufslist_id() { return einkaufslist_id; }
    public void setEinkaufslist_id(String einkaufslist_id) { this.einkaufslist_id = einkaufslist_id; }

    public String getHaus_id() { return haus_id; }
    public void setHaus_id(String haus_id) { this.haus_id = haus_id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, Produkt> getProdukte() { return produkte; }
    public void setProdukte(Map<String, Produkt> produkte) { this.produkte = produkte; }
}