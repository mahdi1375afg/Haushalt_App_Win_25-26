package com.example.haushalt_app_java.domain;

public enum Einheit {
    Kilogramm("Kilogramm"),
    Gramm("Gramm"),
    Liter("Liter"),
    Flasche("Flasche"),
    Stueck("Stück"),
    Packung("Packung"),
    Meter("Meter"),
    SONSTIGES("Sonstiges");

    private final String displayName;

    Einheit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}