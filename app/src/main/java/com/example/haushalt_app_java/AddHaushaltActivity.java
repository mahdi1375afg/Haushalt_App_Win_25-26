package com.example.haushalt_app_java;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.haushalt_app_java.domain.Haushalt;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.haushalt_app_java.domain.Nutzer;

import android.util.Log;

import java.util.List;
import java.util.ArrayList;

import static android.widget.Toast.*;

import java.util.ArrayList;
import java.util.List;

public class AddHaushaltActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseDatabase db; // zentrale, regionsspezifische Instanz
    private TextView hName;
    private Button hAddName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_haushalt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseDatabase.getInstance(DB_URL);
        hName = findViewById(R.id.hName);
        hAddName = findViewById(R.id.hAddName);

            hAddName.setOnClickListener(v -> {
                Log.d("AddHaushalt", "Button geklickt");

                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Toast.makeText(this, "Nicht eingeloggt!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String name = hName.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(this, "Bitte Namen eingeben", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Log.d("AddHaushalt", "User ID: " + userId);

                DatabaseReference userRef = db.getReference().child("Benutzer").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d("AddHaushalt", "Firebase Snapshot erhalten");

                    if (snapshot.exists()) {
                        String userName = snapshot.child("name").getValue(String.class);
                        Log.d("AddHaushalt", "Benutzername: " + userName);

                        String haushaltId = db.getReference().child("Hauser").push().getKey();
                        Log.d("AddHaushalt", "Haushalt ID generiert: " + haushaltId);

                        List<Nutzer> mitglieder = new ArrayList<>();
                        Nutzer currentUser = new Nutzer(userId, userName);
                        mitglieder.add(currentUser);

                        Haushalt haushalt = new Haushalt(haushaltId, name, mitglieder);

                        db.getReference().child("Hauser").child(haushaltId).setValue(haushalt)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("AddHaushalt", "Haushalt erfolgreich erstellt!");
                                userRef.child("hausId").setValue(haushaltId);
                                Toast.makeText(AddHaushaltActivity.this, "Haushalt erstellt!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("AddHaushalt", "Fehler beim Erstellen: " + e.getMessage());
                                Toast.makeText(AddHaushaltActivity.this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    } else {
                        Log.e("AddHaushalt", "Benutzer existiert nicht - erstelle neuen Eintrag");

                        // Benutzer automatisch aus Firebase Auth erstellen
                        String displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                        String userName = (displayName != null && !displayName.isEmpty()) ? displayName : "Unbekannt";

                        Nutzer neuerNutzer = new Nutzer(userId, userName);
                        userRef.setValue(neuerNutzer)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("AddHaushalt", "Benutzer erfolgreich erstellt");
                                    Toast.makeText(AddHaushaltActivity.this, "Profil erstellt. Haushalt wird angelegt...", Toast.LENGTH_SHORT).show();

                                    // Jetzt Haushalt erstellen
                                    String haushaltId = db.getReference().child("Hauser").push().getKey();
                                    List<Nutzer> mitglieder = new ArrayList<>();
                                    mitglieder.add(neuerNutzer);

                                    Haushalt haushalt = new Haushalt(haushaltId, name, mitglieder);

                                    db.getReference().child("Hauser").child(haushaltId).setValue(haushalt)
                                            .addOnSuccessListener(aVoid2 -> {
                                                userRef.child("hausId").setValue(haushaltId);
                                                Toast.makeText(AddHaushaltActivity.this, "Haushalt erstellt!", Toast.LENGTH_SHORT).show();
                                                finish();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("AddHaushalt", "Fehler beim Erstellen: " + e.getMessage());
                                    Toast.makeText(AddHaushaltActivity.this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("AddHaushalt", "Firebase Fehler: " + error.getMessage());
                    Toast.makeText(AddHaushaltActivity.this, "Fehler beim Laden des Nutzers", Toast.LENGTH_SHORT).show();
                }
            });
            });


    }
}