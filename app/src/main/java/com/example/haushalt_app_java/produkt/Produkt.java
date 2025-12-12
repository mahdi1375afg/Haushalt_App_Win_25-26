package com.example.haushalt_app_java.produkt;

import static java.util.UUID.randomUUID;

import com.google.firebase.database.ServerValue;

public class Produkt {

    private String produkt_id;
    private String haus_id;
    private String name;
    private String einheit;
    private String kategorie;
    private int mindBestand;
    private int zielbestand;
    private Object timestamp;


    public Produkt() {
        // Default-Konstruktor erforderlich für Firebase
    }

    public Produkt(String haus_id, String name, String kategorie, int mindBestand, int zielbestand, String einheit) {
        this.produkt_id = randomUUID().toString();
        this.haus_id = haus_id;
        this.name = name;
        this.einheit = einheit;
        this.kategorie = kategorie;
        this.mindBestand = mindBestand;
        this.zielbestand = zielbestand;
        this.timestamp = ServerValue.TIMESTAMP;
    }

    public String getHaus_id() {
        return haus_id;
    }
    public void setHaus_id(String haus_id) {
        this.haus_id = haus_id;
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
    public int getZielbestand() {
        return zielbestand;
    }

    public void setZielbestand(int zielbestand) {
        this.zielbestand = zielbestand;
    }
    public Object getTimestamp() {
        return timestamp;}
    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }


    public void setName_lower(String lowerCase) {
        this.name=lowerCase;
    }

    public int getMenge() {
        int menge = 0;
        return menge;
    }


}
