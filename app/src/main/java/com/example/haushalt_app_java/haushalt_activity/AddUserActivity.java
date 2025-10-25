package com.example.haushalt_app_java.haushalt_activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.haushalt_app_java.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AddUserActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private EditText editTextUsername;
    private Button addUserButton;
    private ImageView backButton;
    private FirebaseDatabase database;
    private List<String> haushaltIds;
    private List<String> haushaltNamen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        editTextUsername = findViewById(R.id.name_input);
        addUserButton = findViewById(R.id.add_user);
        backButton = findViewById(R.id.back_button);
        database = FirebaseDatabase.getInstance(DB_URL);

        haushaltIds = new ArrayList<>();
        haushaltNamen = new ArrayList<>();

        backButton.setOnClickListener(v -> finish());
        loadHaushalte();
        addUserButton.setOnClickListener(v -> addUserToExistingHaushalt());
    }

    private void loadHaushalte() {
        database.getReference().child("Hauser")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    haushaltIds.clear();
                    haushaltNamen.clear();

                    for (DataSnapshot hausSnapshot : snapshot.getChildren()) {
                        String hausId = hausSnapshot.getKey();
                        String hausName = hausSnapshot.child("name").getValue(String.class);

                        if (hausId != null && hausName != null) {
                            haushaltIds.add(hausId);
                            haushaltNamen.add(hausName);
                        }
                    }

                    if (haushaltIds.isEmpty()) {
                        Toast.makeText(AddUserActivity.this,
                            "Keine Haushalte gefunden", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AddUserActivity.this,
                        "Fehler beim Laden der Haushalte", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void addUserToExistingHaushalt() {
        String username = editTextUsername.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Bitte Benutzernamen eingeben", Toast.LENGTH_SHORT).show();
            return;
        }

        if (haushaltIds.isEmpty()) {
            Toast.makeText(this, "Keine Haushalte verfügbar", Toast.LENGTH_SHORT).show();
            return;
        }

        String bestehendeHaushaltId = haushaltIds.get(0);
        String haushaltName = haushaltNamen.get(0);

        database.getReference().child("Benutzer")
            .orderByChild("name").equalTo(username)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(AddUserActivity.this,
                            "Benutzer '" + username + "' nicht gefunden", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userId = userSnapshot.getKey();

                        DatabaseReference mitgliederRef = database.getReference()
                            .child("Hauser")
                            .child(bestehendeHaushaltId)
                            .child("mitgliederIds")
                            .child(userId);

                        mitgliederRef.setValue(true)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AddUserActivity.this,
                                    username + " wurde hinzugefügt", Toast.LENGTH_SHORT).show();
                                editTextUsername.setText("");

                                // ✅ WICHTIG: Signalisiere Erfolg
                                setResult(RESULT_OK);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddUserActivity.this,
                                    "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                        break;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AddUserActivity.this,
                        "Datenbankfehler", Toast.LENGTH_SHORT).show();
                }
            });
    }
}