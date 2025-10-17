package com.example.haushalt_app_java.domain;

public enum kategorie {
    LEBENSMITTEL("Lebensmittel"),
    GETRAENKE("Getränke"),
    HYGIENE("Hygiene"),
    HAUSHALT("Haushalt"),
    SONSTIGES("Sonstiges");

    private final String displayName;

    kategorie(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}