package com.example.haushalt_app_java.domain;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.domain.EinkaufslisteService;
import com.example.haushalt_app_java.domain.Produkt;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class EinkaufslisteDetailActivity extends AppCompatActivity {
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
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Hauser")
                .child(hausId)
                .child("einkaufslisten")
                .child(listeId)
                .child("produkte");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.clear();
                produkte.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Produkt p = s.getValue(Produkt.class);
                    if (p != null) {
                        produkte.add(p);
                        adapter.add(p.getName() + " - " + p.getMenge() + " " + p.getEinheit());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EinkaufslisteDetailActivity.this, "Fehler beim Laden", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddProduktDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Produkt hinzufügen");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        EditText name = new EditText(this);
        name.setHint("Produktname");
        layout.addView(name);

        EditText menge = new EditText(this);
        menge.setHint("Menge");
        layout.addView(menge);

        EditText einheit = new EditText(this);
        einheit.setHint("Einheit (z.B. Stück)");
        layout.addView(einheit);

        builder.setView(layout);
        builder.setPositiveButton("Hinzufügen", (dialog, which) -> {
            String pname = name.getText().toString();
            int pmenge = Integer.parseInt(menge.getText().toString());
            String peinheit = einheit.getText().toString();

            Produkt p = new Produkt(
                    null, hausId, pname, pmenge, "Sonstiges", 0, peinheit
            );

            service.addProduktZuListe(hausId, listeId, p, this::loadProdukte, null);
        });
        builder.setNegativeButton("Abbrechen", null);
        builder.show();
    }
}
