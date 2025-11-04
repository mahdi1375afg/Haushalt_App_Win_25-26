package com.example.haushalt_app_java.profile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.haushalt_app_java.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class profile_update_Activity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";

    private EditText nameEditText;
    private Button speichernButton;
    private Button abbrechenButton;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance(DB_URL);

        nameEditText = findViewById(R.id.nameEditText);
        speichernButton = findViewById(R.id.speichernButton);
        abbrechenButton = findViewById(R.id.abbrechenButton);

        ladeAktuellenNamen();

        speichernButton.setOnClickListener(v -> speichernAenderungen());
        abbrechenButton.setOnClickListener(v -> finish());
    }

    private void ladeAktuellenNamen() {
        if (currentUser != null) {
            db.getReference().child("Benutzer").child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.child("name").getValue(String.class);
                        if (name != null && !name.isEmpty()) {
                            nameEditText.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(profile_update_Activity.this,
                            "Fehler beim Laden", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    private void speichernAenderungen() {
        String neuerName = nameEditText.getText().toString().trim();

        if (neuerName.isEmpty()) {
            nameEditText.setError("Name darf nicht leer sein");
            return;
        }

        if (currentUser == null) return;

        // Nur Name in Realtime Database aktualisieren (ohne hausId/nutzerId zu ändern)
        db.getReference().child("Benutzer").child(currentUser.getUid())
            .child("name").setValue(neuerName)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Name aktualisiert", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Fehler beim Speichern", Toast.LENGTH_SHORT).show();
                }
            });
    }
}