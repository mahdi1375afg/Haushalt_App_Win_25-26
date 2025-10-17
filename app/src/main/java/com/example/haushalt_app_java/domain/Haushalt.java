package com.example.haushalt_app_java.domain;

import java.util.List;

public class Haushalt {
    private String haushalt_id;
    private String name;
    private String lowercaseName;
    List<Nutzer> mitglieder;

    public Haushalt() {
        // Default-Konstruktor erforderlich für Firebase
    }

    public Haushalt(String haushalt_id, String name, List<Nutzer> mitglieder) {
        this.haushalt_id = haushalt_id;
        this.name = name;
        this.lowercaseName = name.toLowerCase();
        this.mitglieder = mitglieder;
    }
    public String getLowercaseName() {
        return lowercaseName;
    }
    public String getHaushalt_id() {
        return haushalt_id;
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
    public List<Nutzer> getMitglieder() {
        return mitglieder;
    }

}
