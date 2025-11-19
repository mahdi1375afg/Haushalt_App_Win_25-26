package com.example.haushalt_app_java.domain;

import java.util.ArrayList;
import java.util.List;

public class Haushalt {
    private String haushalt_id;
    private String name;
    private String lowercaseName;
    List<String> mitgliederIds;

    public Haushalt() {
        // Default-Konstruktor erforderlich für Firebase
        this.mitgliederIds = new ArrayList<>();
    }

    public Haushalt(String haushalt_id, String name, List<String> mitglieder) {
        this.haushalt_id = haushalt_id;
        this.name = name;
        this.lowercaseName = name.toLowerCase();
        this.mitgliederIds = new ArrayList<>();
    }
    public String getLowercaseName() {
        return lowercaseName;
    }
    public String getHaus_id() {
        return this.haushalt_id;
    }
    public void setHaushalt_id(String haushalt_id) {
        this.haushalt_id = haushalt_id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setMitgliederIds(List<String> mitgliederIds) {
        this.mitgliederIds = mitgliederIds;
    }

    public void addMitglied(String nutzerId) {
        if (mitgliederIds == null) {
            mitgliederIds = new ArrayList<>();
        }
        if (!mitgliederIds.contains(nutzerId)) {
            mitgliederIds.add(nutzerId);
        }
    }

}
