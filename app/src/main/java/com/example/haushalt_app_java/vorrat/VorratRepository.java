package com.example.haushalt_app_java.vorrat;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.haushalt_app_java.produkt.Produkt;
import com.example.haushalt_app_java.einkaufsliste.EinkaufslisteEintrag;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VorratRepository {

    private final DatabaseReference databaseReference;
    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";

    public VorratRepository() {
        databaseReference = FirebaseDatabase.getInstance(DB_URL).getReference();
    }

    public void getVorrat(String haushaltId, final OnVorratDataChangedListener listener) {
        DatabaseReference vorratRef = databaseReference.child("Haushalte").child(haushaltId).child("vorrat");
        Log.d("VorratRepository", "VorratRef: " + vorratRef);

        vorratRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("VorratRepository", "Snapshot: " + snapshot);

                List<EinkaufslisteEintrag> vorratliste = new ArrayList<>();
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    listener.onVorratDataChanged(vorratliste);
                    return;
                }

                long totalItems = snapshot.getChildrenCount();
                final long[] loadedItems = {0};

                Log.d("VorratRepository", "Snapshot: " + snapshot);
                Log.d("VorratRepository", "Snapshot Children: " + snapshot.getChildren());

                for (DataSnapshot vorratEintragSnapshot : snapshot.getChildren()) {
                    Log.d("VorratRepository", "VorratEintragSnapshot: " + vorratEintragSnapshot);
                    String produktId = vorratEintragSnapshot.getKey();
                    Integer mengeInteger = vorratEintragSnapshot.child("menge").getValue(Integer.class);
                    int menge = (mengeInteger != null) ? mengeInteger : 0;
                    Log.d("VorratRepository", "ProduktId: " + produktId + ", Menge: " + menge);

                    if (produktId != null) {
                        DatabaseReference produktRef = databaseReference.child("Haushalte").child(haushaltId).child("produkte").child(produktId);
                        Log.d("VorratRepository", "ProduktRef: " + produktRef);

                        produktRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot produktSnapshot) {
                                Produkt produkt = produktSnapshot.getValue(Produkt.class);
                                Log.d("VorratRepository", "Produkt: " + produkt);
                                if (produkt != null) {
                                    vorratliste.add(new EinkaufslisteEintrag(produktId, produkt.getName(), produkt.getKategorie(), produkt.getEinheit(), menge));
                                }
                                loadedItems[0]++;
                                if (loadedItems[0] == totalItems) {
                                    listener.onVorratDataChanged(vorratliste);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                loadedItems[0]++;
                                if (loadedItems[0] == totalItems) {
                                    listener.onVorratDataChanged(vorratliste);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error);
            }
        });
    }

    public void addVorratItem(String haushaltId, Produkt produkt, int quantity, final OnVorratItemAddedListener listener) {
        if (produkt.getProdukt_id() == null) {
            listener.onFailure(new IllegalArgumentException("Produkt ID cannot be null"));
            return;
        }

        DatabaseReference vorratItemRef = databaseReference.child("Haushalte").child(haushaltId).child("vorrat").child(produkt.getProdukt_id());

        vorratItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int currentMenge = 0;
                if (snapshot.exists() && snapshot.child("menge").exists()) {
                    Integer existingMenge = snapshot.child("menge").getValue(Integer.class);
                    if (existingMenge != null) {
                        currentMenge = existingMenge;
                    }
                }

                int newMenge = currentMenge + quantity;

                Map<String, Object> vorratData = new HashMap<>();
                vorratData.put("menge", newMenge);
                vorratData.put("timestamp", ServerValue.TIMESTAMP);

                vorratItemRef.setValue(vorratData)
                        .addOnSuccessListener(aVoid -> listener.onSuccess())
                        .addOnFailureListener(listener::onFailure);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.toException());
            }
        });
    }

    public void updateMenge(String haushaltId, String produktId, int neueMenge) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("menge", neueMenge);
        updates.put("timestamp", ServerValue.TIMESTAMP);
        databaseReference.child("Haushalte").child(haushaltId).child("vorrat").child(produktId).updateChildren(updates);
    }

    public interface OnVorratDataChangedListener {
        void onVorratDataChanged(List<EinkaufslisteEintrag> vorratliste);
        void onError(DatabaseError error);
    }

    public interface OnVorratItemAddedListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}
