package com.example.haushalt_app_java.domain;

import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Produkt {

    private String produkt_id;
    private String hausId;
    private String name;
    private String name_lower;
    private String einheit;
    private int menge;
    private String kategorie;
    private int mindBestand;
    private Object timestamp;


    public Produkt() {
        // Default-Konstruktor erforderlich für Firebase
    }

    public Produkt(String produkt_id,String hausId, String name, int menge, String kategorie, int mindBestand, String einheit) {
        this.produkt_id = produkt_id;
        this.hausId = hausId;
        this.name = name;
        this.menge = menge;
        this.name_lower = name.toLowerCase(); // Speichert den Namen in Kleinbuchstaben für die Suche
        this.einheit = einheit;
        this.kategorie = kategorie;
        this.mindBestand = mindBestand;

        this.timestamp = ServerValue.TIMESTAMP;
    }

    public Produkt(String name, int menge, String kategorie, int mindBestand) {
    }

    public String getHausId() {
        return hausId;
    }
    public void setHausId(String hausId) {
        this.hausId = hausId;
    }

    public String getProdukt_id() {
        return this.produkt_id;
    }
    public void setProdukt_id(String produkt_id) {
        this.produkt_id = produkt_id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEinheit() {
        return einheit;
    }
    public void setEinheit(String einheit) {
        this.einheit = einheit;
    }

    public String getName_lower() {
        return name_lower;
    }
    public void setName_lower(String name_lower) {
        this.name_lower = name_lower;
    }
    public int getMenge() {
        return this.menge;
    }
    public void setMenge(int menge) {
        this.menge = menge;
    }
    public String getKategorie() {
        return this.kategorie;
    }
    public void setKategorie(String kategorie) {
        this.kategorie = kategorie;
    }
    public int getMindBestand() {
        return mindBestand;
    }
    public void setMindBestand(int mindBestand) {
        this.mindBestand = mindBestand;
    }
    public Object getTimestamp() {
        return timestamp;}
    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }





}

