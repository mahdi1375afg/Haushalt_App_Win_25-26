//package com.example.haushalt_app_java.domain;
//
//import android.util.Log;
//import com.google.firebase.database.*;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//
//public class AutomatischeEinkaufslisteService {
//
//    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
//    private final FirebaseDatabase db;
//
//    public AutomatischeEinkaufslisteService() {
//        this.db = FirebaseDatabase.getInstance(DB_URL);
//    }
//
//    public void aktualisiereAutomatischeListe(String hausId) {
//        DatabaseReference produkteRef = db.getReference()
//                .child("Hauser")
//                .child(hausId)
//                .child("produkte");
//
//        DatabaseReference listenRef = db.getReference()
//                .child("Hauser")
//                .child(hausId)
//                .child("einkaufslisten");
//
//        listenRef.orderByChild("name").equalTo("Automatische Einkaufsliste")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot listeSnap) {
//                        if (!listeSnap.exists()) return;
//
//                        DataSnapshot listeNode = listeSnap.getChildren().iterator().next();
//                        String listeId = listeNode.getKey();
//
//                        produkteRef.get().addOnSuccessListener(productSnap -> {
//
//                            DatabaseReference autoProdukteRef =
//                                    listenRef.child(listeId).child("produkte");
//
//                            autoProdukteRef.get().addOnSuccessListener(autoSnap -> {
//
//                                Map<String, Boolean> autoListeVorher = new HashMap<>();
//                                for (DataSnapshot s : autoSnap.getChildren()) {
//                                    autoListeVorher.put(s.getKey(), true);
//                                }
//
//                                for (DataSnapshot s : productSnap.getChildren()) {
//                                    Produkt p = s.getValue(Produkt.class);
//                                    if (p == null) continue;
//
//                                    int menge = p.getMenge();
//                                    int mind = p.getMindBestand();
//
//                                    if (menge <= mind) {
//                                        int nachkaufMenge = 2 * mind - menge;
//
//                                        Produkt einkaufProdukt = new Produkt(
//                                                p.getProdukt_id(),
//                                                p.getHaus_id(),
//                                                p.getName(),
//                                                nachkaufMenge,
//                                                p.getKategorie(),
//                                                p.getMindBestand(),
//                                                p.getEinheit()
//                                        );
//
//                                        autoProdukteRef.child(p.getProdukt_id()).setValue(einkaufProdukt);
//
//                                        autoListeVorher.remove(p.getProdukt_id());
//
//                                    } else {
//                                        autoListeVorher.remove(p.getProdukt_id());
//                                        autoProdukteRef.child(p.getProdukt_id()).removeValue();
//                                    }
//                                }
//
//                                Log.d("AutoListe", "Automatische Liste aktualisiert 🔄");
//                            });
//                        });
//                    }
//
//                    @Override public void onCancelled(DatabaseError error) {}
//                });
//    }
//
//
//
//    public void automatischErstelleEinkaufsliste(String hausId, Runnable onFertig, Runnable onFehler) {
//        DatabaseReference produkteRef = db.getReference()
//                .child("Hauser")
//                .child(hausId)
//                .child("produkte");
//
//        produkteRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                List<Produkt> einkaufsProdukte = new ArrayList<>();
//
//                for (DataSnapshot snap : snapshot.getChildren()) {
//                    Produkt p = snap.getValue(Produkt.class);
//                    if (p == null) continue;
//
//                    int menge = p.getMenge();
//                    int mind = p.getMindBestand();
//
//                    if (menge <= mind) {
//                        int nachkaufMenge = 2 * mind - menge;
//
//                        Produkt einkaufProdukt = new Produkt(
//                                p.getProdukt_id(),
//                                p.getHaus_id(),
//                                p.getName(),
//                                nachkaufMenge,
//                                p.getKategorie(),
//                                p.getMindBestand(),
//                                p.getEinheit()
//                        );
//
//                        einkaufsProdukte.add(einkaufProdukt);
//                    }
//                }
//
//                if (einkaufsProdukte.isEmpty()) {
//                    Log.d("EinkaufslisteService", "Keine Produkte unter Mindestbestand.");
//                    if (onFertig != null) onFertig.run();
//                    return;
//                }
//
//                // ✅ Einkaufsliste erstellen
//                String listeId = db.getReference()
//                        .child("Hauser")
//                        .child(hausId)
//                        .child("einkaufslisten")
//                        .push()
//                        .getKey();
//
//                if (listeId == null) {
//                    Log.e("EinkaufslisteService", "Fehler beim Generieren der Listen-ID");
//                    if (onFehler != null) onFehler.run();
//                    return;
//                }
//
//                String datum = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
//                        .format(new Date());
//                String listenName = "Automatische Liste vom " + datum;
//
//                // ✅ Einkaufsliste ohne Produkte erstellen
//                Map<String, Object> listeData = new HashMap<>();
//                listeData.put("einkaufslist_id", listeId);
//                listeData.put("haus_id", hausId);
//                listeData.put("name", listenName);
//
//                DatabaseReference listeRef = db.getReference()
//                        .child("Hauser")
//                        .child(hausId)
//                        .child("einkaufslisten")
//                        .child(listeId);
//
//                // ✅ Zuerst die Liste erstellen, dann Produkte hinzufügen
//                listeRef.setValue(listeData)
//                        .addOnSuccessListener(aVoid -> {
//                            // ✅ Produkte zur Liste hinzufügen
//                            DatabaseReference produkteRef = listeRef.child("produkte");
//
//                            for (Produkt prod : einkaufsProdukte) {
//                                String produktId = produkteRef.push().getKey();
//                                if (produktId != null) {
//                                    prod.setProdukt_id(produktId);
//                                    produkteRef.child(produktId).setValue(prod);
//                                }
//                            }
//
//                            Log.d("EinkaufslisteService",
//                                "Einkaufsliste mit " + einkaufsProdukte.size() + " Produkten erstellt!");
//                            if (onFertig != null) onFertig.run();
//                        })
//                        .addOnFailureListener(e -> {
//                            Log.e("EinkaufslisteService", "Fehler: " + e.getMessage());
//                            if (onFehler != null) onFehler.run();
//                        });
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                Log.e("EinkaufslisteService", "Fehler beim Laden: " + error.getMessage());
//                if (onFehler != null) onFehler.run();
//            }
//        });
//    }
//}