package com.example.haushalt_app_java.utils;

public class HausIdManager {
    private static HausIdManager instance;
    private String hausId;

    private HausIdManager() {}

    public static HausIdManager getInstance() {
        if (instance == null) {
            instance = new HausIdManager();
        }
        return instance;
    }

    public String getHausId() {
        return hausId;
    }

    public void setHausId(String hausId) {
        this.hausId = hausId;
    }
}