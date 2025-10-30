package com.example.haushalt_app_java.domain;

import com.google.firebase.database.ServerValue;

public class Nachricht {

    private String nachricht_id;
    private String haus_id;
    private String text;
    private Object datum;

    public Nachricht() {
    }

    public Nachricht(String nachricht_id, String haus_id, String text) {
        this.nachricht_id = nachricht_id;
        this.haus_id = haus_id;
        this.text = text;
        this.datum = ServerValue.TIMESTAMP;
    }

    public String getNachricht_id() {
        return nachricht_id;
    }

    public void setNachricht_id(String nachricht_id) {
        this.nachricht_id = nachricht_id;
    }

    public String getHaus_id() {
        return haus_id;
    }

    public void setHaus_id(String haus_id) {
        this.haus_id = haus_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Object getDatum() {
        return datum;
    }

    public void setDatum(Object datum) {
        this.datum = datum;
    }
}
