package com.example.haushalt_app_java.domain;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ShoppingListRepository {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private DatabaseReference databaseReference;

    public ShoppingListRepository(String haushaltId) {
        this.databaseReference = FirebaseDatabase.getInstance(DB_URL).getReference("Haushalte").child(haushaltId).child("einkaufsliste");
    }

    public void addShoppingListItem(EinkaufslisteEintrag item, final OnShoppingListItemAddedListener listener) {
        String id = databaseReference.push().getKey();
        if (id != null) {
            databaseReference.child(id).setValue(item)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e));
        } else {
            listener.onFailure(new Exception("Couldn't get push key for shopping list items"));
        }
    }

    public interface OnShoppingListItemAddedListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}
