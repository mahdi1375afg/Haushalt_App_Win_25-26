package com.example.haushalt_app_java.domain;

public enum Kategorie {
    LEBENSMITTEL("Lebensmittel"),
    GETRAENKE("Getränke"),
    HYGIENE("Hygiene"),
    HAUSHALT("Haushalt"),
    SONSTIGES("Sonstiges");

    private final String displayName;

    Kategorie(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}