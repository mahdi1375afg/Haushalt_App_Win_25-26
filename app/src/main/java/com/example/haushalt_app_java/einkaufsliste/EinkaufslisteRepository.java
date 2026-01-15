package com.example.haushalt_app_java.einkaufsliste;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.haushalt_app_java.produkt.Produkt;
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

public class EinkaufslisteRepository {

    private final DatabaseReference databaseReference;
    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";

    public EinkaufslisteRepository() {
        databaseReference = FirebaseDatabase.getInstance(DB_URL).getReference();
    }

    public void getEinkaufsliste(String haushaltId, final OnEinkaufslisteDataChangedListener listener) {
        DatabaseReference einkaufslisteRef = databaseReference.child("Haushalte").child(haushaltId).child("einkaufsliste");
        Log.d("EinkaufslisteRepository", "EinkaufslisteRef: " + einkaufslisteRef);

        einkaufslisteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("EinkaufslisteRepository", "Snapshot: " + snapshot);

                List<ListenEintrag> einkaufsliste = new ArrayList<>();
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    listener.onEinkaufslisteDataChanged(einkaufsliste);
                    return;
                }

                long totalItems = snapshot.getChildrenCount();
                final long[] loadedItems = {0};

                Log.d("EinkaufslisteRepository", "Snapshot: " + snapshot);
                Log.d("EinkaufslisteRepository", "Snapshot Children: " + snapshot.getChildren());

                for (DataSnapshot einkaufslisteEintragSnapshot : snapshot.getChildren()) {
                    Log.d("EinkaufslisteRepository", "EinkaufslisteEintragSnapshot: " + einkaufslisteEintragSnapshot);
                    String produktId = einkaufslisteEintragSnapshot.getKey(); // Corrected from .child("produkt_id").getValue(String.class);
                    Integer mengeInteger = einkaufslisteEintragSnapshot.child("menge").getValue(Integer.class);
                    int menge = (mengeInteger != null) ? mengeInteger : 0;
                    Log.d("EinkaufslisteRepository", "ProduktId: " + produktId + ", Menge: " + menge);

                    if (produktId != null) {
                        DatabaseReference produktRef = databaseReference.child("Haushalte").child(haushaltId).child("produkte").child(produktId);
                        Log.d("EinkaufslisteRepository", "ProduktRef: " + produktRef);

                        produktRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot produktSnapshot) {
                                Produkt produkt = produktSnapshot.getValue(Produkt.class);
                                Log.d("EinkaufslisteRepository", "Produkt: " + produkt);
                                if (produkt != null) {
                                    DatabaseReference vorratRef = databaseReference.child("Haushalte").child(haushaltId).child("vorrat").child(produktId).child("menge");
                                    vorratRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot vorratSnapshot) {
                                            Integer mengeImVorratInt = vorratSnapshot.getValue(Integer.class);
                                            int mengeImVorrat = (mengeImVorratInt != null) ? mengeImVorratInt : 0;

                                            ListenEintrag listenEintrag = new ListenEintrag(produktId, produkt.getName(), produkt.getKategorie(), produkt.getEinheit(), menge);
                                            listenEintrag.setMengeImVorrat(mengeImVorrat);
                                            listenEintrag.setMindestmenge(produkt.getMindBestand());
                                            einkaufsliste.add(listenEintrag);

                                            loadedItems[0]++;
                                            if (loadedItems[0] == totalItems) {
                                                listener.onEinkaufslisteDataChanged(einkaufsliste);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            loadedItems[0]++;
                                            if (loadedItems[0] == totalItems) {
                                                listener.onEinkaufslisteDataChanged(einkaufsliste);
                                            }
                                        }
                                    });
                                } else {
                                    loadedItems[0]++;
                                    if (loadedItems[0] == totalItems) {
                                        listener.onEinkaufslisteDataChanged(einkaufsliste);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                loadedItems[0]++;
                                if (loadedItems[0] == totalItems) {
                                    listener.onEinkaufslisteDataChanged(einkaufsliste);
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

    public void addShoppingListItem(String haushaltId, String productId, int quantity, final OnShoppingListItemListener listener) {
        if (productId == null) {
            listener.onFailure(new IllegalArgumentException("Produkt ID cannot be null"));
            return;
        }

        DatabaseReference shoppingListItemRef = databaseReference.child("Haushalte").child(haushaltId).child("einkaufsliste").child(productId);

        Map<String, Object> shoppingListData = new HashMap<>();
        shoppingListData.put("menge", quantity);
        shoppingListData.put("timestamp", ServerValue.TIMESTAMP);

        shoppingListItemRef.setValue(shoppingListData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    public void setQuantityOnShoppingList(String haushaltId, String productId, int quantity, final OnShoppingListItemListener listener) {
        if (productId == null || productId.isEmpty()) {
            if (listener != null) {
                listener.onFailure(new IllegalArgumentException("Product ID cannot be null or empty."));
            }
            return;
        }

        DatabaseReference itemRef = databaseReference.child("Haushalte").child(haushaltId).child("einkaufsliste").child(productId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("menge", quantity);
        updates.put("timestamp", ServerValue.TIMESTAMP);

        itemRef.setValue(updates)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    public void updateMenge(String haushaltId, String produktId, int neueMenge) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("menge", neueMenge);
        updates.put("timestamp", ServerValue.TIMESTAMP);
        databaseReference.child("Haushalte").child(haushaltId).child("einkaufsliste").child(produktId).updateChildren(updates);
    }

    public void removeShoppingListItem(String haushaltId, String produktId, final OnShoppingListItemRemovedListener listener) {
        databaseReference.child("Haushalte").child(haushaltId).child("einkaufsliste").child(produktId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e));
    }

    public void updateBookmarkedStatus(String currentHaushaltId, String produktId, boolean isBookmarked) {
        // TODO Jonas
    }

    // TODO Jonas unify & simplify Listeners
    public interface OnEinkaufslisteDataChangedListener {
        void onEinkaufslisteDataChanged(List<ListenEintrag> einkaufsliste);
        void onError(DatabaseError error);
    }

    public interface OnShoppingListItemListener {
        void onSuccess();
        void onFailure(Exception e);
    }


    public interface OnShoppingListItemRemovedListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}
