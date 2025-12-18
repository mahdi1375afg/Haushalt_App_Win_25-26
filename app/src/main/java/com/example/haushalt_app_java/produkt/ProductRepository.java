package com.example.haushalt_app_java.produkt;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProductRepository {
    private final String dbUrl = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";

    private final DatabaseReference databaseReference;

    public ProductRepository(String haushaltId) {
        databaseReference = FirebaseDatabase.getInstance(dbUrl).getReference("Haushalte").child(haushaltId).child("produkte");
    }

    public void addProduct(Produkt product, final OnProductAddedListener listener) {
        String produktId = databaseReference.push().getKey();
        Log.d("ProductRepository", "Generated produktId: " + produktId);
        if (produktId != null) {
            product.setProdukt_id(produktId);
            databaseReference.child(produktId).setValue(product)
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onFailure(e));
        } else {
            listener.onFailure(new Exception("Could not get push key"));
        }
    }

    public void getProductById(String produktId, final OnProductLoadedListener listener) {
        databaseReference.child(produktId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Produkt produkt = snapshot.getValue(Produkt.class);
                    if (produkt != null) {
                        produkt.setProdukt_id(snapshot.getKey());
                        listener.onSuccess(produkt);
                    } else {
                        listener.onFailure(new Exception("Failed to parse product data."));
                    }
                } else {
                    listener.onFailure(new Exception("Product not found."));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.toException());
            }
        });
    }

    public void updateProduct(Produkt product, final OnProductUpdatedListener listener) {
        if (product.getProdukt_id() == null) {
            listener.onFailure(new IllegalArgumentException("Produkt ID cannot be null"));
            return;
        }

        Map<String, Object> productValues = new HashMap<>();
        productValues.put("name", product.getName());
        productValues.put("kategorie", product.getKategorie());
        productValues.put("einheit", product.getEinheit());
        productValues.put("mindBestand", product.getMindBestand());
        productValues.put("zielbestand", product.getZielbestand());

        databaseReference.child(product.getProdukt_id()).updateChildren(productValues)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e));
    }

    public void deleteProduct(String produktId, final OnProductDeletedListener listener) {
        databaseReference.child(produktId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e));
    }

    public void updateBookmarkStatus(String produktId, boolean isBookmarked) {
        databaseReference.child(produktId).child("bookmarked").setValue(isBookmarked);
    }

    public interface OnProductAddedListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnProductLoadedListener {
        void onSuccess(Produkt produkt);
        void onFailure(Exception e);
    }

    public interface OnProductUpdatedListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnProductDeletedListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}
