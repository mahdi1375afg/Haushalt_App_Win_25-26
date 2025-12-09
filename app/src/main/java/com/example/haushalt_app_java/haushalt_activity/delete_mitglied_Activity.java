package com.example.haushalt_app_java.haushalt_activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.haushalt_app_java.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class delete_mitglied_Activity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_mitglied);

        database = FirebaseDatabase.getInstance(DB_URL);

        String mitgliedName = getIntent().getStringExtra("mitgliedName");
        String hausId = getIntent().getStringExtra("hausId");

        Button deleteMitgliedButton = findViewById(R.id.delete_mitglied_button);
        Button cancelButton = findViewById(R.id.cancel_button);

        deleteMitgliedButton.setOnClickListener(v -> {
            // ✅ Prüfe, ob User sich selbst löschen will
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            database.getReference().child("Benutzer")
                .orderByChild("name").equalTo(mitgliedName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String targetUserId = userSnapshot.getKey();

                            // ✅ Selbst-Löschung blockieren
                            if (targetUserId.equals(currentUserId)) {
                                Toast.makeText(delete_mitglied_Activity.this,
                                    "Sie können sich nicht selbst entfernen!", Toast.LENGTH_LONG).show();
                                return;
                            }

                            // Normal löschen
                            database.getReference()
                                .child("Haushalte")
                                .child(hausId)
                                .child("mitgliederIds")
                                .child(targetUserId)
                                .removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(delete_mitglied_Activity.this,
                                        mitgliedName + " wurde entfernt", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(delete_mitglied_Activity.this,
                                        "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            break;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(delete_mitglied_Activity.this,
                            "Datenbankfehler", Toast.LENGTH_SHORT).show();
                    }
                });
        });

        cancelButton.setOnClickListener(v -> finish());
    }
}