package com.example.haushalt_app_java.einkaufsliste;

import com.example.haushalt_app_java.produkt.Produkt;

public class ListenEintrag {
    private String produktId;
    private String name;
    private String kategorie;
    private String einheit;
    private int menge;

    private boolean isBookmarked;


    public ListenEintrag() {
    }
    public ListenEintrag(Produkt produkt, int menge) {
        this.produktId = produkt.getProdukt_id();
        this.name = produkt.getName();
        this.kategorie = produkt.getKategorie();
        this.einheit = produkt.getEinheit();
        this.menge = menge;
    }

    public ListenEintrag(String produktId, String name, String kategorie, String einheit, int menge) {
        this.produktId = produktId;
        this.name = name;
        this.kategorie = kategorie;
        this.einheit = einheit;
        this.menge = menge;
    }

    public String getProduktId() {
        return produktId;
    }

    public void setProduktId(String produktId) {
        this.produktId = produktId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKategorie() {
        return kategorie;
    }

    public void setKategorie(String kategorie) {
        this.kategorie = kategorie;
    }

    public String getEinheit() {
        return einheit;
    }

    public void setEinheit(String einheit) {
        this.einheit = einheit;
    }

    public int getMenge() {
        return menge;
    }

    public void setMenge(int menge) {
        this.menge = menge;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean isBookmarked) {
        this.isBookmarked = isBookmarked;
    }
}
