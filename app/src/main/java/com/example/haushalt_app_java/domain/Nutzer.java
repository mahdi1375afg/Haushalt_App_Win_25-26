package com.example.haushalt_app_java.domain;

import java.util.HashMap;
import java.util.Map;

public class Nutzer {

    private String nutzerId;
    private String name;
    private Map<String, Boolean> haushalte;
    public Nutzer() {
        // Default-Konstruktor erforderlich für Firebase
        this.haushalte = new HashMap<>();
    }
    public Nutzer(String nutzerId, String name, Map<String, Boolean> haushalte) {
        this.nutzerId = nutzerId;
        this.name = name;
        this.haushalte = haushalte;

    }

    public Map<String, Boolean> getHausId() {
        return haushalte;
    }
    public void setHausId(Map<String, Boolean> hausId) {
        this.haushalte= hausId;
    }
    public String getnutzerId() {
        return nutzerId;
    }
    public void setnutzerId(String nutzerId) {
        this.nutzerId = nutzerId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


}
