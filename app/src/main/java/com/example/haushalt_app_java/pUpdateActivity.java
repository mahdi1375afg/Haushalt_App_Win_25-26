package com.example.haushalt_app_java;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class pUpdateActivity extends AppCompatActivity {
    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseDatabase db;
    private EditText pEinheit;
    private EditText pName;
    private EditText pMenge;
    private EditText pKategorie;
    private EditText pMindBestand;
    private Button add;
    private String produktId;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_p_update);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pEinheit = findViewById(R.id.pEinheit);
        pName = findViewById(R.id.pName);
        pMenge = findViewById(R.id.pMenge);
        pKategorie = findViewById(R.id.pKategorie);
        pMindBestand = findViewById(R.id.pMindBestand);
        add = findViewById(R.id.add);

        db = FirebaseDatabase.getInstance(DB_URL);
        ref = db.getReference().child("Produkt");

        // Intent-Daten auslesen und Felder befüllen
        Intent in = getIntent();
        produktId = in.getStringExtra("produkt_id");
        pName.setText(in.getStringExtra("name"));
        pMenge.setText(String.valueOf(in.getIntExtra("menge", 0)));
        pEinheit.setText(in.getStringExtra("einheit"));
        pKategorie.setText(in.getStringExtra("kategorie"));
        pMindBestand.setText(String.valueOf(in.getIntExtra("mindBestand", 0)));

        add.setOnClickListener(v -> updateProdukt());
    }

    private void updateProdukt() {
        Map<String, Object> updates = new HashMap<>();

        // Nur geänderte Felder hinzufügen
        String name = pName.getText().toString().trim();
        if (!name.isEmpty()) {
            updates.put("name", name);
            updates.put("name_lower", name.toLowerCase());
        }

        String mengeStr = pMenge.getText().toString().trim();
        if (!mengeStr.isEmpty()) {
            updates.put("menge", parseIntSafe(mengeStr));
        }

        String einheit = pEinheit.getText().toString().trim();
        if (!einheit.isEmpty()) {
            updates.put("einheit", einheit);
        }

        String kategorie = pKategorie.getText().toString().trim();
        if (!kategorie.isEmpty()) {
            updates.put("kategorie", kategorie);
        }

        String mindStr = pMindBestand.getText().toString().trim();
        if (!mindStr.isEmpty()) {
            updates.put("mindBestand", parseIntSafe(mindStr));
        }

        if (updates.isEmpty()) {
            Toast.makeText(this, "Keine Änderungen vorgenommen", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nur geänderte Felder in Firebase aktualisieren
        ref.child(produktId).updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Produkt aktualisiert", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Update fehlgeschlagen", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}