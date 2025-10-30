package com.example.haushalt_app_java.domain;

import android.util.Log;

import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AutomatischeEinkaufslisteService {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private final FirebaseDatabase db;

    public AutomatischeEinkaufslisteService() {
        this.db = FirebaseDatabase.getInstance(DB_URL);
    }

    public void automatischErstelleEinkaufsliste(String hausId, Runnable onFertig, Runnable onFehler) {
        DatabaseReference produkteRef = db.getReference()
                .child("Hauser")
                .child(hausId)
                .child("produkte");

        produkteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Produkt> einkaufsProdukte = new ArrayList<>();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Produkt p = snap.getValue(Produkt.class);
                    if (p == null) continue;

                    int menge = p.getMenge();
                    int mind = p.getMindBestand();

                    if (menge < mind) {
                        int nachkaufMenge = 2 * mind - menge;

                        Produkt einkaufProdukt = new Produkt(
                                p.getProdukt_id(),
                                p.getHaus_id(),
                                p.getName(),
                                nachkaufMenge,
                                p.getKategorie(),
                                p.getMindBestand(),
                                p.getEinheit()
                        );

                        einkaufsProdukte.add(einkaufProdukt);
                    }
                }

                if (einkaufsProdukte.isEmpty()) {
                    Log.d("EinkaufslisteService", "Keine Produkte unter Mindestbestand.");
                    if (onFertig != null) onFertig.run();
                    return;
                }

                String listeId = db.getReference()
                        .child("Hauser")
                        .child(hausId)
                        .child("einkaufslisten")
                        .push()
                        .getKey();

                String datum = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        .format(new Date());
                String listenName = "Automatische Liste vom " + datum;

                Einkaufsliste liste = new Einkaufsliste(listeId, hausId, listenName);

                for (Produkt prod : einkaufsProdukte) {
                    liste.addProdukt(prod);
                }

                db.getReference()
                        .child("Hauser")
                        .child(hausId)
                        .child("einkaufslisten")
                        .child(listeId)
                        .setValue(liste)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("EinkaufslisteService", "Einkaufsliste erfolgreich erstellt!");
                            if (onFertig != null) onFertig.run();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("EinkaufslisteService", "Fehler: " + e.getMessage());
                            if (onFehler != null) onFehler.run();
                        });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("EinkaufslisteService", "Fehler beim Laden: " + error.getMessage());
                if (onFehler != null) onFehler.run();
            }
        });
    }
}
