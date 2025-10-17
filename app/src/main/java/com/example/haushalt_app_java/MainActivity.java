
package com.example.haushalt_app_java;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";

    private Button logout;
    private Button add;
    private EditText edit;

    private FirebaseDatabase database;// zentrale, regionsspezifische Instanz
    private FloatingActionButton pAddScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //by default activity
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase DB korrekt initialisieren (Region!)
        database = FirebaseDatabase.getInstance(DB_URL);

        // UI-Elemente binden
        logout = findViewById(R.id.logout);
        edit = findViewById(R.id.edit);
        add = findViewById(R.id.add);
        pAddScreen = findViewById(R.id.pAddScreen);

        pAddScreen.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, p_addActivity2.class);
            startActivity(intent);
        });



        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, StartActivity.class));
            finish();
        });

        // Beispielschreiben: nutzt die korrekte DB-Instanz
        /*DatabaseReference ref = database.getReference().child("com.example.haushalt_app_java.domain.Produkt").child("produkt_id");
        Map<String, Object> produkt = new HashMap<>();
        produkt.put("name", "Kartoffeln");
        produkt.put("Bestand", "5kg");
        produkt.put("com.example.haushalt_app_java.domain.kategorie", "Gemüse");
        produkt.put("mindBestand", "2kg");
        produkt.put("datum", ServerValue.TIMESTAMP);

        ref.setValue(produkt)
           .addOnSuccessListener(aVoid -> Toast.makeText(this, "Gespeichert", Toast.LENGTH_SHORT).show())
           .addOnFailureListener(e -> Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        */

        //add button zum Hinzufügen eines Produkts
        add.setOnClickListener(v -> {
            String text_name = edit.getText().toString();
            if (text_name.isEmpty()) {
                Toast.makeText(MainActivity.this, "Bitte Produktname eingeben", Toast.LENGTH_SHORT).show();
            } else {
                database.getReference().child("test").setValue(text_name)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FIREBASE_SUCCESS", "Daten erfolgreich gespeichert");
                            Toast.makeText(MainActivity.this, "com.example.haushalt_app_java.domain.Produkt erfolgreich hinzugefügt", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FIREBASE_ERROR", "Fehler beim Speichern: " + e.getMessage(), e);
                            Toast.makeText(MainActivity.this, "Fehler beim Hinzufügen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        })
                        .addOnCompleteListener(task -> {
                            Log.d("FIREBASE_COMPLETE", "Operation abgeschlossen, erfolgreich: " + task.isSuccessful());
                        });
            }
        });
    }
}