package com.example.haushalt_app_java.produkt;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ProductRepository {

    private final DatabaseReference databaseReference;

    public ProductRepository(String haushaltId) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Haushalte").child(haushaltId).child("produkte");
    }

    public void addProduct(Produkt product, final OnProductAddedListener listener) {
        String produktId = databaseReference.push().getKey();
        if (produktId != null) {
            product.setProdukt_id(produktId);
            databaseReference.child(produktId).setValue(product)
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onFailure(e));
        } else {
            listener.onFailure(new Exception("Could not get push key"));
        }
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

    public interface OnProductUpdatedListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnProductDeletedListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}
