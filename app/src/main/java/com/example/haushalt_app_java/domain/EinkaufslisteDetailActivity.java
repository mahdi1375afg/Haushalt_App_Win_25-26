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
    private List<Produkt> hausProdukte = new ArrayList<>();


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
        loadHausProdukte();

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

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void loadHausProdukte() {
        DatabaseReference ref = FirebaseDatabase.getInstance(DB_URL)
                .getReference("Hauser")
                .child(hausId)
                .child("produkte");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hausProdukte.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Produkt p = s.getValue(Produkt.class);
                    if (p != null) {
                        p.setProdukt_id(s.getKey());
                        hausProdukte.add(p);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }


    private void showAddProduktDialog() {
        if (hausProdukte.isEmpty()) {
            Toast.makeText(this, "Keine Produkte im Haushalt!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Produkt auswählen");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        Spinner spinner = new Spinner(this);
        List<String> namen = new ArrayList<>();
        for (Produkt p : hausProdukte) {
            namen.add(p.getName());
        }

        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_dark,
                namen
        );
        spinAdapter.setDropDownViewResource(R.layout.spinner_item_dark);
        spinner.setAdapter(spinAdapter);
        spinner.setPopupBackgroundResource(R.color.ux_color_surface);
        layout.addView(spinner);

        EditText menge = new EditText(this);
        menge.setHint("Menge eingeben");
        menge.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(menge);

        builder.setView(layout);
        builder.setPositiveButton("Hinzufügen", (dialog, which) -> {
            int pos = spinner.getSelectedItemPosition();
            Produkt selected = hausProdukte.get(pos);

            int pMenge;
            try {
                pMenge = Integer.parseInt(menge.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Ungültige Menge!", Toast.LENGTH_SHORT).show();
                return;
            }

            for (Produkt existing : produkte) {
                if (existing.getName().equalsIgnoreCase(selected.getName())) {
                    int newMenge = existing.getMenge() + pMenge;
                    existing.setMenge(newMenge);

                    service.updateProduktInListe(
                            hausId,
                            listeId,
                            existing.getProdukt_id(),
                            existing,
                            () -> {
                                Toast.makeText(this, "Menge aktualisiert!", Toast.LENGTH_SHORT).show();
                                loadProdukte();
                            },
                            () -> Toast.makeText(this, "Fehler beim Aktualisieren!", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }
            }

            Produkt newProd = new Produkt(
                    null,
                    hausId,
                    selected.getName(),
                    pMenge,
                    selected.getKategorie(),
                    selected.getMindBestand(),
                    selected.getEinheit()
            );

            service.addProduktZuListe(
                    hausId,
                    listeId,
                    newProd,
                    () -> {
                        Toast.makeText(this, "Produkt hinzugefügt!", Toast.LENGTH_SHORT).show();
                        loadProdukte();
                    },
                    () -> Toast.makeText(this, "Fehler beim Hinzufügen!", Toast.LENGTH_SHORT).show()
            );
        });

        builder.setNegativeButton("Abbrechen", null);
        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getWindow().setBackgroundDrawableResource(R.color.ux_color_surface);

        menge.setTextColor(getResources().getColor(R.color.ux_color_on_surface));
        menge.setHintTextColor(getResources().getColor(android.R.color.darker_gray));

        TextView selectedView = (TextView) spinner.getSelectedView();
        if (selectedView != null) {
            selectedView.setTextColor(getResources().getColor(R.color.ux_color_on_surface));
        }

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

        int textColor = getResources().getColor(R.color.ux_color_on_surface);

        name.setTextColor(textColor);
        menge.setTextColor(textColor);
        einheit.setTextColor(textColor);

        name.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
        menge.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
        einheit.setHintTextColor(getResources().getColor(android.R.color.darker_gray));

        TextView title = dialog.findViewById(android.R.id.title);
        if (title != null) title.setTextColor(textColor);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(textColor);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(textColor);
    }



}