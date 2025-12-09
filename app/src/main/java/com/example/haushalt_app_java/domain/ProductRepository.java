package com.example.haushalt_app_java.domain;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProductRepository {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private DatabaseReference databaseReference;

    public ProductRepository(String haushaltId) {
        this.databaseReference = FirebaseDatabase.getInstance(DB_URL).getReference("Haushalte").child(haushaltId).child("produkte");
    }

    public void addProduct(Produkt product, final OnProductAddedListener listener) {
        String id = databaseReference.push().getKey();
        if (id != null) {
            databaseReference.child(id).setValue(product)
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    listener.onFailure(e);
                });
        } else {
            listener.onFailure(new Exception("Couldn't get push key for products"));
        }
    }

    public interface OnProductAddedListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}
