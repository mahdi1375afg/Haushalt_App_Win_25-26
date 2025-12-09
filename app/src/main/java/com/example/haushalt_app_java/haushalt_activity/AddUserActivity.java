package com.example.haushalt_app_java.haushalt_activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.haushalt_app_java.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddUserActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private EditText editTextUsername;
    private Button addUserButton;
    private ImageView backButton;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private String hausId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        editTextUsername = findViewById(R.id.name_input);
        addUserButton = findViewById(R.id.add_user);
        backButton = findViewById(R.id.back_button);
        database = FirebaseDatabase.getInstance(DB_URL);
        auth = FirebaseAuth.getInstance();

        hausId = getIntent().getStringExtra("hausId");

        if (hausId == null || auth.getCurrentUser() == null) {
            Toast.makeText(this, "Fehler beim Laden", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backButton.setOnClickListener(v -> finish());
        addUserButton.setOnClickListener(v -> addUserToHaushalt());
    }

    private void addUserToHaushalt() {
        String username = editTextUsername.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Bitte Benutzernamen eingeben", Toast.LENGTH_SHORT).show();
            return;
        }

        addUserButton.setEnabled(false);

        // ✅ Suche alle Benutzer (erfordert ".read": "auth != null" in Rules)
        database.getReference().child("Benutzer")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String targetUserId = null;
                    String targetUserHausId = null;

                    // Durchsuche alle Benutzer nach Name
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userName = userSnapshot.child("name").getValue(String.class);
                        if (username.equals(userName)) {
                            targetUserId = userSnapshot.getKey();
                            targetUserHausId = userSnapshot.child("hausId").getValue(String.class);
                            break;
                        }
                    }

                    if (targetUserId == null) {
                        Toast.makeText(AddUserActivity.this,
                            "Benutzer '" + username + "' nicht gefunden", Toast.LENGTH_SHORT).show();
                        addUserButton.setEnabled(true);
                        return;
                    }

                    if (targetUserHausId != null && !targetUserHausId.isEmpty()) {
                        Toast.makeText(AddUserActivity.this,
                            "Benutzer ist bereits einem Haushalt zugeordnet",
                            Toast.LENGTH_SHORT).show();
                        addUserButton.setEnabled(true);
                        return;
                    }

                    // ✅ Füge Benutzer zum Haushalt hinzu
                    String finalUserId = targetUserId;
                    DatabaseReference hausRef = database.getReference("Haushalte")
                        .child(hausId).child("mitgliederIds").child(finalUserId);

                    hausRef.setValue(true)
                        .addOnSuccessListener(aVoid -> {
                            // ✅ Setze hausId beim Benutzer
                            database.getReference("Benutzer")
                                .child(finalUserId)
                                .child("hausId")
                                .setValue(hausId)
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(AddUserActivity.this,
                                        username + " wurde hinzugefügt", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AddUserActivity.this,
                                        "Fehler beim Aktualisieren: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                    addUserButton.setEnabled(true);
                                });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AddUserActivity.this,
                                "Fehler beim Hinzufügen: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                            addUserButton.setEnabled(true);
                        });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AddUserActivity.this,
                        "Datenbankfehler: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    addUserButton.setEnabled(true);
                }
            });
    }
}