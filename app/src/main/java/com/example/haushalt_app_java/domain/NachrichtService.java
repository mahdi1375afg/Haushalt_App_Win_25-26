package com.example.haushalt_app_java.domain;

import android.util.Log;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class NachrichtService {

    private static final String DB_URL =
            "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";

    private final FirebaseDatabase db;

    public NachrichtService() {
        this.db = FirebaseDatabase.getInstance(DB_URL);
    }
    public void addNachricht(String hausId, String text) {
        DatabaseReference ref = db.getReference()
                .child("Hauser")
                .child(hausId)
                .child("nachrichten")
                .push();

        String id = ref.getKey();
        Nachricht n = new Nachricht(id, hausId, text);

        ref.setValue(n).addOnFailureListener(e ->
                Log.e("NachrichtService", "Fehler beim Speichern der Nachricht: " + e.getMessage()));
    }
    public void getNachrichten(String hausId, ValueEventListener listener) {
        db.getReference()
                .child("Hauser")
                .child(hausId)
                .child("nachrichten")
                .addListenerForSingleValueEvent(listener);
    }
    public void clearNachrichten(String hausId) {
        db.getReference()
                .child("Hauser")
                .child(hausId)
                .child("nachrichten")
                .removeValue();
    }
}
