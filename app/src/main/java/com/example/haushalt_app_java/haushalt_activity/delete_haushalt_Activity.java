package com.example.haushalt_app_java.haushalt_activity;

import android.os.Bundle;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import com.example.haushalt_app_java.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Intent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class delete_haushalt_Activity extends AppCompatActivity {

    private Button nein_button;
    private Button ja_button;
    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseDatabase db;
    private String hausId;
    private DatabaseReference hausRef;
    private ImageView back_button;
    private EditText neu_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delete_haushalt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nein_button = findViewById(R.id.nein_button);
        ja_button = findViewById(R.id.ja_button);
        back_button = findViewById(R.id.back_haus);
        neu_name = findViewById(R.id.neu_name);

        back_button.setOnClickListener(v -> finish());

        db = FirebaseDatabase.getInstance(DB_URL);

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("hausId")) {
            finish();
            return;
        }

        hausId = intent.getStringExtra("hausId");
        hausRef = db.getReference().child("Haushalte").child(hausId);

        // ✅ JA-Button: Haushalt löschen
        ja_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ✅ Lösche hausId bei ALLEN Mitgliedern
                hausRef.child("mitgliederIds").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot mitgliedSnap : snapshot.getChildren()) {
                            String mitgliedId = mitgliedSnap.getKey();

                            // ✅ Entferne hausId beim Benutzer
                            db.getReference("Benutzer").child(mitgliedId).child("hausId").removeValue();
                        }

                        // ✅ Lösche den Haushalt selbst
                        hausRef.removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(delete_haushalt_Activity.this,
                                    "Haushalt gelöscht", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(delete_haushalt_Activity.this,
                                    "Fehler beim Löschen", Toast.LENGTH_SHORT).show();
                            });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(delete_haushalt_Activity.this,
                            "Fehler beim Laden der Mitglieder", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // ✅ NEIN-Button: Haushalt umbenennen
        nein_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = neu_name.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(delete_haushalt_Activity.this,
                        "Bitte Namen eingeben", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ Benenne Haushalt um
                hausRef.child("name").setValue(name)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(delete_haushalt_Activity.this,
                            "Name geändert", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(delete_haushalt_Activity.this,
                            "Fehler beim Aktualisieren", Toast.LENGTH_SHORT).show();
                    });
            }
        });
    }
}