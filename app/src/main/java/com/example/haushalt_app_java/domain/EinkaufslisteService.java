package com.example.haushalt_app_java.domain;

import android.util.Log;
import com.google.firebase.database.*;
public class EinkaufslisteService {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private final FirebaseDatabase db;

    public EinkaufslisteService() {
        this.db = FirebaseDatabase.getInstance(DB_URL);
    }
    public void addEinkaufsliste(Einkaufsliste liste, Runnable onSuccess, Runnable onError) {
        DatabaseReference ref = db.getReference()
                .child("Hauser")
                .child(liste.getHaus_id())
                .child("einkaufslisten")
                .child(liste.getEinkaufslist_id());

        ref.setValue(liste)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EinkaufslisteService", "Einkaufsliste hinzugefügt");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("EinkaufslisteService", "Fehler beim Hinzufügen: " + e.getMessage());
                    if (onError != null) onError.run();
                });
    }

    public void addProduktZuListe(String hausId, String listeId, Produkt produkt,
                                  Runnable onSuccess, Runnable onError) {

        String produktId = produkt.getProdukt_id();

        DatabaseReference ref = db.getReference()
                .child("Hauser")
                .child(hausId)
                .child("einkaufslisten")
                .child(listeId)
                .child("produkte")
                .child(produktId);

        ref.setValue(produkt)
                .addOnSuccessListener(aVoid -> {
                    if (onSuccess != null) onSuccess.run();
                    new AutomatischeEinkaufslisteService().aktualisiereAutomatischeListe(hausId);
                })
                .addOnFailureListener(e -> {
                    if (onError != null) onError.run();
                });
    }

/*    public void getEinkaufslisten(String hausId, ValueEventListener listener) {
        db.getReference()
                .child("Hauser")
                .child(hausId)
                .child("einkaufslisten")
                .addValueEventListener(listener);
    } */

    public void getProdukteEinerListe(String hausId, String listeId, ValueEventListener listener) {
        db.getReference()
                .child("Hauser")
                .child(hausId)
                .child("einkaufslisten")
                .child(listeId)
                .child("produkte")
                .addListenerForSingleValueEvent(listener);
    }

    public void updateEinkaufslisteName(String hausId, String listeId, String newName,
                                        Runnable onSuccess, Runnable onError) {
        db.getReference()
                .child("Hauser")
                .child(hausId)
                .child("einkaufslisten")
                .child(listeId)
                .child("name")
                .setValue(newName)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EinkaufslisteService", "Listenname aktualisiert");
                    if (onSuccess != null) onSuccess.run();
                    new AutomatischeEinkaufslisteService()
                            .aktualisiereAutomatischeListe(hausId);

                })
                .addOnFailureListener(e -> {
                    Log.e("EinkaufslisteService", "Fehler beim Aktualisieren des Listennamens: " + e.getMessage());
                    if (onError != null) onError.run();
                });
    }

    public void updateProduktInListe(String hausId, String listeId, String produktId, Produkt neuesProdukt,
                                     Runnable onSuccess, Runnable onError) {
        db.getReference()
                .child("Hauser")
                .child(hausId)
                .child("einkaufslisten")
                .child(listeId)
                .child("produkte")
                .child(produktId)
                .setValue(neuesProdukt)
                .addOnSuccessListener(aVoid -> {
                    new AutomatischeEinkaufslisteService().aktualisiereAutomatischeListe(hausId);
                    Log.d("EinkaufslisteService", "Produkt aktualisiert");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("EinkaufslisteService", "Fehler beim Aktualisieren des Produkts: " + e.getMessage());
                    if (onError != null) onError.run();
                });
    }
    public void deleteEinkaufsliste(String hausId, String listeId, Runnable onSuccess, Runnable onError) {
        db.getReference()
                .child("Hauser")
                .child(hausId)
                .child("einkaufslisten")
                .child(listeId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    new AutomatischeEinkaufslisteService()
                            .aktualisiereAutomatischeListe(hausId);
                    Log.d("EinkaufslisteService", "Einkaufsliste gelöscht");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("EinkaufslisteService", "Fehler beim Löschen: " + e.getMessage());
                    if (onError != null) onError.run();
                });
    }

    public void deleteProduktInListe(String hausId, String listeId, String produktId,
                                     Runnable onSuccess, Runnable onError) {
        db.getReference()
                .child("Hauser")
                .child(hausId)
                .child("einkaufslisten")
                .child(listeId)
                .child("produkte")
                .child(produktId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    new AutomatischeEinkaufslisteService()
                            .aktualisiereAutomatischeListe(hausId);
                    Log.d("EinkaufslisteService", "Produkt gelöscht");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("EinkaufslisteService", "Fehler beim Löschen des Produkts: " + e.getMessage());
                    if (onError != null) onError.run();
                });
    }
}
