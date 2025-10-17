package com.example.haushalt_app_java.domain;

public class Nutzer {

    private String nutzerId;
    private String name;
    public Nutzer() {
        // Default-Konstruktor erforderlich für Firebase
    }
    public Nutzer(String nutzerId, String name) {
        this.nutzerId = nutzerId;
        this.name = name;
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
