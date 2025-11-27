package com.example.haushalt_app_java.domain;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.haushalt_app_java.R;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class EinkaufslisteDetailActivity extends AppCompatActivity {
    private static final String TAG = "EinkaufslisteDetail";
    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";

    private String hausId, listeId;
    private ListView produktList;
    private ArrayAdapter<String> adapter;
    private List<Produkt> produkte;
    private EinkaufslisteService service;
    private FloatingActionButton fabAddProdukt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_einkaufsliste_detail);

        hausId = getIntent().getStringExtra("hausId");
        listeId = getIntent().getStringExtra("listeId");

        //  Logging zur Fehlersuche
        Log.d(TAG, "hausId: " + hausId);
        Log.d(TAG, "listeId: " + listeId);

        if (hausId == null || listeId == null) {
            Toast.makeText(this, "Fehler: Keine IDs gefunden", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        produktList = findViewById(R.id.produkt_list);
        fabAddProdukt = findViewById(R.id.fab_add_produkt);
        produkte = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        produktList.setAdapter(adapter);
        service = new EinkaufslisteService();

        loadProdukte();

        fabAddProdukt.setOnClickListener(v -> showAddProduktDialog());
    }

    private void loadProdukte() {
        DatabaseReference ref = FirebaseDatabase.getInstance(DB_URL)
                .getReference("Hauser")
                .child(hausId)
                .child("einkaufslisten")
                .child(listeId)
                .child("produkte");

        Log.d(TAG, "Firebase Pfad: " + ref.toString());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.clear();
                produkte.clear();

                if (!snapshot.exists()) {
                    Toast.makeText(EinkaufslisteDetailActivity.this,
                            "Keine Produkte vorhanden",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot s : snapshot.getChildren()) {
                    Produkt p = s.getValue(Produkt.class);
                    if (p != null) {
                        p.setProdukt_id(s.getKey());
                        produkte.add(p);

                        String displayText = p.getName() + " - " +
                                p.getMenge() + " " +
                                p.getEinheit();
                        adapter.add(displayText);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EinkaufslisteDetailActivity.this,
                        "Fehler beim Laden: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        produktList.setOnItemLongClickListener((parent, view, position, id) -> {
            Produkt selected = produkte.get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Aktion wählen");
            builder.setMessage("Was möchtest du mit diesem Produkt tun?");
            builder.setPositiveButton("Bearbeiten", (dialog, which) -> showEditProduktDialog(selected));
            builder.setNegativeButton("Löschen", (dialog, which) -> {
                service.deleteProduktInListe(
                        hausId,
                        listeId,
                        selected.getProdukt_id(),
                        () -> {
                            Toast.makeText(this, "Produkt gelöscht", Toast.LENGTH_SHORT).show();
                            loadProdukte();
                        },
                        () -> Toast.makeText(this, "Fehler beim Löschen", Toast.LENGTH_SHORT).show()
                );
            });
            builder.setNeutralButton("Abbrechen", null);

            AlertDialog dialog = builder.create();
            dialog.show();

            // Hintergrund im Surface-Dark-Theme
            dialog.getWindow().setBackgroundDrawableResource(R.color.ux_color_surface);

            // Text & Titel in deiner hellen Surface-Textfarbe
            int textColor = getResources().getColor(R.color.ux_color_on_surface);

            TextView title = dialog.findViewById(android.R.id.title);
            TextView message = dialog.findViewById(android.R.id.message);

            if (title != null) title.setTextColor(textColor);
            if (message != null) message.setTextColor(textColor);

            return true;
        });
    }

    private void showAddProduktDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Produkt hinzufügen");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText name = new EditText(this);
        name.setHint("Produktname");
        layout.addView(name);

        EditText menge = new EditText(this);
        menge.setHint("Menge");
        menge.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(menge);

        EditText einheit = new EditText(this);
        einheit.setHint("Einheit (z.B. Stück)");
        layout.addView(einheit);

        builder.setView(layout);
        builder.setPositiveButton("Hinzufügen", (dialog, which) -> {
            String pname = name.getText().toString().trim();
            String mengeStr = menge.getText().toString().trim();
            String peinheit = einheit.getText().toString().trim();

            if (pname.isEmpty() || mengeStr.isEmpty() || peinheit.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
                return;
            }

            int pmenge;
            try {
                pmenge = Integer.parseInt(mengeStr);
                if (pmenge <= 0) {
                    Toast.makeText(this, "Menge muss größer als 0 sein", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Ungültige Mengenangabe", Toast.LENGTH_SHORT).show();
                return;
            }

            Produkt p = new Produkt(
                    null,
                    hausId,
                    pname,
                    pmenge,
                    "Sonstiges",
                    0,
                    peinheit
            );

            service.addProduktZuListe(hausId, listeId, p,
                () -> {
                    Toast.makeText(this, "Produkt hinzugefügt", Toast.LENGTH_SHORT).show();
                    loadProdukte();
                },
                () -> Toast.makeText(this, "Fehler beim Hinzufügen", Toast.LENGTH_SHORT).show()
            );
        });
        builder.setNegativeButton("Abbrechen", null);
        builder.show();
    }

    private void showEditProduktDialog(Produkt produkt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Produkt bearbeiten");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText name = new EditText(this);
        name.setHint("Produktname");
        name.setText(produkt.getName());
        layout.addView(name);

        EditText menge = new EditText(this);
        menge.setHint("Menge");
        menge.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        menge.setText(String.valueOf(produkt.getMenge()));
        layout.addView(menge);

        EditText einheit = new EditText(this);
        einheit.setHint("Einheit (z. B. Stück)");
        einheit.setText(produkt.getEinheit());
        layout.addView(einheit);

        builder.setView(layout);

        builder.setPositiveButton("Speichern", (dialogInterface, which) -> {
            try {
                produkt.setName(name.getText().toString());
                produkt.setMenge(Integer.parseInt(menge.getText().toString()));
                produkt.setEinheit(einheit.getText().toString());

                service.updateProduktInListe(
                        hausId,
                        listeId,
                        produkt.getProdukt_id(),
                        produkt,
                        () -> {
                            Toast.makeText(this, "Produkt aktualisiert", Toast.LENGTH_SHORT).show();
                            loadProdukte();
                        },
                        () -> Toast.makeText(this, "Fehler beim Speichern", Toast.LENGTH_SHORT).show()
                );

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Ungültige Menge", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Abbrechen", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getWindow().setBackgroundDrawableResource(R.color.ux_color_surface);

        name.setTextColor(getResources().getColor(R.color.ux_color_on_surface));
        menge.setTextColor(getResources().getColor(R.color.ux_color_on_surface));
        einheit.setTextColor(getResources().getColor(R.color.ux_color_on_surface));

        name.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
        menge.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
        einheit.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
    }


}