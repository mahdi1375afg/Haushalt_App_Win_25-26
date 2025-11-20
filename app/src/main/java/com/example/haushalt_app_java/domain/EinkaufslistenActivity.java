package com.example.haushalt_app_java.domain;

import com.example.haushalt_app_java.haushalt_activity.HaushaltActivity;
import com.example.haushalt_app_java.product_activity.MainActivity;
import com.example.haushalt_app_java.profile.profile_Activity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.haushalt_app_java.R;
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

        if (hausId == null || hausId.isEmpty()) {
            Toast.makeText(this, "Fehler: Keine Haushalt-ID gefunden", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadEinkaufslisten();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String listeId = listIds.get(position);
            Intent intent = new Intent(EinkaufslistenActivity.this,
                    EinkaufslisteDetailActivity.class);
            intent.putExtra("hausId", hausId);
            intent.putExtra("listeId", listeId);
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String listeId = listIds.get(position);
            String listeName = listNames.get(position);

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Liste löschen?")
                    .setMessage("Möchten Sie '" + listeName + "' wirklich löschen?")
                    .setPositiveButton("Löschen", (dialog, which) -> {
                        service.deleteEinkaufsliste(
                                hausId,
                                listeId,
                                () -> {
                                    Toast.makeText(this, "Liste gelöscht",
                                            Toast.LENGTH_SHORT).show();
                                    loadEinkaufslisten();
                                },
                                () -> Toast.makeText(this,
                                        "Fehler beim Löschen",
                                        Toast.LENGTH_SHORT).show()
                        );
                    })
                    .setNegativeButton("Abbrechen", null)
                    .show();
            return true;
        });

        fabAdd.setOnClickListener(v -> showAddDialog());

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_einkaufslisten);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_einkaufslisten) {
                return true;
            }
            if (itemId == R.id.nav_household) {
                Intent intent = new Intent(this, HaushaltActivity.class);
                intent.putExtra("hausId", hausId);
                startActivity(intent);
                return true;
            }
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, profile_Activity.class));
                return true;
            }
            if (itemId == R.id.nav_products) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("hausId", hausId);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void loadEinkaufslisten() {
        service.getEinkaufslisten(hausId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listNames.clear();
                listIds.clear();

                for (DataSnapshot s : snapshot.getChildren()) {
                    String listeId = s.getKey();
                    String listeName = s.child("name").getValue(String.class);

                    if (listeId != null && listeName != null) {
                        listIds.add(listeId);
                        listNames.add(listeName);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EinkaufslistenActivity.this,
                        "Fehler beim Laden: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText input = new EditText(this);
        input.setHint("Name der Einkaufsliste");
        layout.addView(input);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Neue Einkaufsliste")
                .setView(layout)
                .setPositiveButton("Erstellen", (dialog, which) -> {
                    String name = input.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Bitte einen Namen eingeben",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String id = FirebaseDatabase.getInstance().getReference()
                            .child("Hauser").child(hausId)
                            .child("einkaufslisten").push().getKey();

                    if (id == null) {
                        Toast.makeText(this, "Fehler beim Erstellen der ID",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Einkaufsliste neueListe =
                            new Einkaufsliste(id, hausId, name);

                    service.addEinkaufsliste(
                            neueListe,
                            () -> {
                                loadEinkaufslisten();
                                Toast.makeText(this, "Liste erstellt",
                                        Toast.LENGTH_SHORT).show();
                            },
                            () -> Toast.makeText(this,
                                    "Fehler beim Erstellen",
                                    Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_einkaufslisten);
    }
}
