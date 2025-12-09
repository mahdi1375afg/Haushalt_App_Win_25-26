package com.example.haushalt_app_java.domain;

public enum Einheit {
    KG("Kilogramm"),
    G("Gramm"),
    L("Liter"),
    ML("Milliliter"),
    ST("Stück");


    private final String displayName;

    Einheit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
