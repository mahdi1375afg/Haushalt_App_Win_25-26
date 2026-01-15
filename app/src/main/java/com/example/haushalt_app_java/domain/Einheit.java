package com.example.haushalt_app_java.domain;

public enum Einheit {
    Kilogramm("KG"),
    Gramm("G"),
    Liter("Lr"),
    Flasche("FL"),
    Stueck("STK"),
    Packung("PKG"),
    Meter("M"),
    SONSTIGES("SONST");

    private final String displayName;

    Einheit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}