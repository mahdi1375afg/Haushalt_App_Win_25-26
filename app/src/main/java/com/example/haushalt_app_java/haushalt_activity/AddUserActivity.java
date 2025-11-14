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

public class AddUserActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private EditText editTextUsername;
    private Button addUserButton;
    private ImageView backButton;
    private FirebaseDatabase database;
    private String hausId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        editTextUsername = findViewById(R.id.name_input);
        addUserButton = findViewById(R.id.add_user);
        backButton = findViewById(R.id.back_button);
        database = FirebaseDatabase.getInstance(DB_URL);

        // ✅ Hole hausId aus Intent
        hausId = getIntent().getStringExtra("hausId");

        if (hausId == null) {
            Toast.makeText(this, "Kein Haushalt gefunden", Toast.LENGTH_SHORT).show();
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

        // ✅ Suche Benutzer nach Name
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

                        // ✅ Prüfe, ob Benutzer bereits Haushalt hat
                        if (userSnapshot.child("hausId").exists()) {
                            Toast.makeText(AddUserActivity.this,
                                "Benutzer ist bereits einem Haushalt zugeordnet",
                                Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // ✅ Füge zu Haushalt hinzu
                        database.getReference("Hauser").child(hausId)
                            .child("mitgliederIds").child(userId).setValue(true);

                        // ✅ Setze hausId beim Benutzer
                        database.getReference("Benutzer").child(userId)
                            .child("hausId").setValue(hausId)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AddUserActivity.this,
                                    username + " wurde hinzugefügt", Toast.LENGTH_SHORT).show();
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