package com.example.haushalt_app_java.domain;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.domain.Einkaufsliste;
import com.example.haushalt_app_java.domain.EinkaufslisteService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class EinkaufslistenActivity extends AppCompatActivity {

    private ListView listView;
    private FloatingActionButton fabAdd;
    private ArrayAdapter<String> adapter;
    private List<String> listNames;
    private List<String> listIds;
    private EinkaufslisteService service;
    private String hausId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_einkaufslisten);

        listView = findViewById(R.id.einkaufslisten_listview);
        fabAdd = findViewById(R.id.fab_add_liste);
        listNames = new ArrayList<>();
        listIds = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listNames);
        listView.setAdapter(adapter);

        service = new EinkaufslisteService();
        hausId = getIntent().getStringExtra("hausId");

        loadEinkaufslisten();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String listeId = listIds.get(position);
            Intent intent = new Intent(EinkaufslistenActivity.this, EinkaufslisteDetailActivity.class);
            intent.putExtra("hausId", hausId);
            intent.putExtra("listeId", listeId);
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String listeId = listIds.get(position);
            service.deleteEinkaufsliste(hausId, listeId,
                    () -> {
                        Toast.makeText(this, "Liste gelöscht", Toast.LENGTH_SHORT).show();
                        loadEinkaufslisten();
                    },
                    () -> Toast.makeText(this, "Fehler beim Löschen", Toast.LENGTH_SHORT).show());
            return true;
        });

        fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void loadEinkaufslisten() {
        service.getEinkaufslisten(hausId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listNames.clear();
                listIds.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Einkaufsliste liste = s.getValue(Einkaufsliste.class);
                    if (liste != null) {
                        listIds.add(liste.getEinkaufslist_id());
                        listNames.add(liste.getName());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EinkaufslistenActivity.this, "Fehler beim Laden", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        EditText input = new EditText(this);
        input.setHint("Name der Einkaufsliste");

        new android.app.AlertDialog.Builder(this)
                .setTitle("Neue Einkaufsliste")
                .setView(input)
                .setPositiveButton("Erstellen", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) return;
                    String id = FirebaseDatabase.getInstance().getReference()
                            .child("Hauser").child(hausId)
                            .child("einkaufslisten").push().getKey();
                    Einkaufsliste neueListe = new Einkaufsliste(id, hausId, name);
                    service.addEinkaufsliste(neueListe, this::loadEinkaufslisten, null);
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }
}
