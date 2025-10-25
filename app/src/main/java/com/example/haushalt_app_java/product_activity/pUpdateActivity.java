package com.example.haushalt_app_java.product_activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.haushalt_app_java.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class pUpdateActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseDatabase database;

    private EditText pName, pMenge, pEinheit, pKategorie, pMindBestand;
    private Button btnUpdate, btnDelete;

    private String produktId;
    private String hausId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p_update);

        database = FirebaseDatabase.getInstance(DB_URL);

        pName = findViewById(R.id.pName);
        pMenge = findViewById(R.id.pMenge);
        pEinheit = findViewById(R.id.pEinheit);
        pKategorie = findViewById(R.id.pKategorie);
        pMindBestand = findViewById(R.id.pMindBestand);
        btnUpdate = findViewById(R.id.add);
        btnDelete = findViewById(R.id.btnDelete);

        // Daten aus Intent holen
        produktId = getIntent().getStringExtra("produkt_id");
        hausId = getIntent().getStringExtra("haus_id");

        Log.d("pUpdateActivity", "Produkt ID: " + produktId);
        Log.d("pUpdateActivity", "Haus ID: " + hausId);

        if (produktId == null || hausId == null) {
            Toast.makeText(this, "Fehler: IDs fehlen - produktId=" + produktId + ", hausId=" + hausId, Toast.LENGTH_LONG).show();
        }

        pName.setText(getIntent().getStringExtra("name"));
        pMenge.setText(String.valueOf(getIntent().getIntExtra("menge", 0)));
        pEinheit.setText(getIntent().getStringExtra("einheit"));
        pKategorie.setText(getIntent().getStringExtra("kategorie"));
        pMindBestand.setText(String.valueOf(getIntent().getIntExtra("mindBestand", 0)));

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProdukt();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteProdukt();
            }
        });
    }

    private void updateProdukt() {
        if (produktId == null || hausId == null) {
            Toast.makeText(this, "Fehler: Produkt-ID fehlt", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = pName.getText().toString().trim();
        String einheit = pEinheit.getText().toString().trim();
        String kategorie = pKategorie.getText().toString().trim();

        int menge = 0;
        int mindBestand = 0;
        try {
            menge = Integer.parseInt(pMenge.getText().toString().trim());
            mindBestand = Integer.parseInt(pMindBestand.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ungültige Zahlen", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference produktRef = database.getReference()
                .child("Hauser")
                .child(hausId)
                .child("produkte")
                .child(produktId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("name_lower", name.toLowerCase());
        updates.put("menge", menge);
        updates.put("einheit", einheit);
        updates.put("kategorie", kategorie);
        updates.put("mindBestand", mindBestand);

        produktRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(pUpdateActivity.this, "Produkt aktualisiert", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(pUpdateActivity.this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteProdukt() {
        if (produktId == null || hausId == null) {
            Toast.makeText(this, "Fehler: Produkt-ID fehlt", Toast.LENGTH_SHORT).show();
            return;
        }

        database.getReference()
                .child("Hauser")
                .child(hausId)
                .child("produkte")
                .child(produktId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(pUpdateActivity.this, "Produkt gelöscht", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(pUpdateActivity.this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}