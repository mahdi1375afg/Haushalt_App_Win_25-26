package com.example.haushalt_app_java.product_activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.StartActivity;
import com.example.haushalt_app_java.activity.EinkaufslisteActivity;
import com.example.haushalt_app_java.activity.VorratActivity;
import com.example.haushalt_app_java.domain.Produkt;
import com.example.haushalt_app_java.domain.Kategorie;
import com.example.haushalt_app_java.haushalt_activity.HaushaltActivity;
import com.example.haushalt_app_java.notification.DatabaseChangeService;
import com.example.haushalt_app_java.profile.profile_Activity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.widget.ArrayAdapter;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private static final int REQ_UPDATE = 1001;

    private Button logout;
    private String currentHausId;
    private FirebaseDatabase database;
    private FloatingActionButton pAddScreen;
    private ListView listView;
    private ArrayList<Produkt> produkten = new ArrayList<>();
    private ProductAdapter productAdapter;
    private boolean lowStockDialogShown = false;
    private Spinner spinnerKategorie;
    private ArrayList<Produkt> alleProdukten = new ArrayList<>();
    private String selectedKategorie = "Alle";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm.isIgnoringBatteryOptimizations(getPackageName())) {
            // Start the DatabaseChangeService as a foreground service
            Intent serviceIntent = new Intent(this, DatabaseChangeService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        }

        database = FirebaseDatabase.getInstance(DB_URL);

        // ✅ Lade hausId aus Firebase Realtime Database
        loadHausIdAndInitialize();
    }

    private void loadHausIdAndInitialize() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference benutzerRef = database.getReference()
            .child("Benutzer")
            .child(currentUserId)
            .child("hausId");

        benutzerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentHausId = snapshot.getValue(String.class);

                    if (currentHausId == null || currentHausId.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Bitte wähle einen Haushalt", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, HaushaltActivity.class));
                        finish();
                        return;
                    }

                    com.example.haushalt_app_java.utils.HausIdManager.getInstance().setHausId(currentHausId);

                    // ✅ UI initialisieren und Produkte laden
                    initializeUI();
                    loadProducts();
                } else {
                    Toast.makeText(MainActivity.this, "Kein Haushalt zugewiesen", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, HaushaltActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Fehler beim Laden der hausId: " + error.getMessage());
                Toast.makeText(MainActivity.this, "Fehler beim Laden", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeUI() {
        logout = findViewById(R.id.logout);
        pAddScreen = findViewById(R.id.pAddScreen);
        listView = findViewById(R.id.listViewp);
        spinnerKategorie = findViewById(R.id.spinnerKategorie);
        setupKategorieSpinner();

        // FloatingActionButton zum Hinzufügen
        pAddScreen.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddProductActivity.class);
            intent.putExtra("haus_id", currentHausId);
            startActivity(intent);
        });

        // Logout Button
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, StartActivity.class));
            finish();
        });

        // ListView Adapter
        productAdapter = new ProductAdapter(this, produkten, currentHausId);
        listView.setAdapter(productAdapter);

        // ListView Item Click
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (produkten == null || position < 0 || position >= produkten.size()) return;
            Produkt p = produkten.get(position);
            if (p == null) return;

            Intent i = new Intent(MainActivity.this, pUpdateActivity.class);
            i.putExtra("produkt_id", p.getProdukt_id());
            i.putExtra("haus_id", p.getHaus_id());
            i.putExtra("name", p.getName());
            i.putExtra("einheit", p.getEinheit());
            i.putExtra("kategorie", p.getKategorie());
            i.putExtra("mindBestand", p.getMindBestand());
            startActivityForResult(i, REQ_UPDATE);
        });

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_products) {
                return true;
            } else if (itemId == R.id.nav_household) {
                startActivity(new Intent(MainActivity.this, HaushaltActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, profile_Activity.class));
                return true;
            } else if (itemId == R.id.nav_einkaufslisten) {
                Intent intent = new Intent(MainActivity.this, EinkaufslisteActivity.class);
                intent.putExtra("HAUSHALT_ID", currentHausId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_vorrat) { // Added Vorrat navigation
                Intent intent = new Intent(MainActivity.this, VorratActivity.class);
                intent.putExtra("HAUSHALT_ID", currentHausId);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void setupKategorieSpinner() {
        ArrayList<String> kategorien = new ArrayList<>();
        kategorien.add("Alle");
        kategorien.add(Kategorie.LEBENSMITTEL.getDisplayName());
        kategorien.add(Kategorie.GETRAENKE.getDisplayName());
        kategorien.add(Kategorie.HYGIENE.getDisplayName());
        kategorien.add(Kategorie.HAUSHALT.getDisplayName());
        kategorien.add(Kategorie.SONSTIGES.getDisplayName());

        ArrayAdapter<String> kategorieAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,  // ✅ Dein eigenes Layout
                kategorien
        );
        kategorieAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);  // ✅ Für Dropdown
        spinnerKategorie.setAdapter(kategorieAdapter);

        spinnerKategorie.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedKategorie = kategorien.get(position);
                filterProdukte();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedKategorie = "Alle";
                filterProdukte();
            }
        });
    }


    private void loadProducts() {
        DatabaseReference produkteRef = database.getReference()
                .child("Haushalte")
                .child(currentHausId)
                .child("produkte");

        produkteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                alleProdukten.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Produkt produkt = snap.getValue(Produkt.class);
                    if (produkt == null) continue;

                    produkt.setProdukt_id(snap.getKey());
                    produkt.setHaus_id(currentHausId);
                    alleProdukten.add(produkt);
                }

                filterProdukte();
                //checkAndShowLowStockDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Fehler beim Laden: " + error.getMessage());
            }
        });
    }

    private void filterProdukte() {
        items.clear();
        produkten.clear();

        for (Produkt produkt : alleProdukten) {
            String produktKategorie = produkt.getKategorie() != null ? produkt.getKategorie() : "";

            if (selectedKategorie.equals("Alle") || produktKategorie.equals(selectedKategorie)) {
                produkten.add(produkt);

                String name = produkt.getName() != null ? produkt.getName() : "";
                String einheit = produkt.getEinheit() != null ? produkt.getEinheit() : "";
                String kategorie = produkt.getKategorie() != null ? produkt.getKategorie() : "";
                String txt = name + " - " + produkt.getMenge() + " " + einheit + " - " + kategorie;
                items.add(txt);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void checkAndShowLowStockDialog() {
        if (lowStockDialogShown) return;
        if (produkten == null || produkten.isEmpty()) return;

        ArrayList<Produkt> low = new ArrayList<>();
        for (Produkt p : produkten) {
            if (p == null) continue;

            int menge = 0;
            int mind = 0;
            try {
                menge = Integer.parseInt(String.valueOf(p.getMenge()));
                mind = Integer.parseInt(String.valueOf(p.getMindBestand()));
            } catch (NumberFormatException e) {
                Log.w("MainActivity", "Ungültige Zahlenwerte bei Produkt: " + p.getName());
            }

            if (mind > 0 && menge < mind) {
                low.add(p);
            }
        }

        if (low.isEmpty()) return;

        String[] items = new String[low.size()];
        for (int i = 0; i < low.size(); i++) {
            Produkt p = low.get(i);
            String name = (p.getName() != null) ? p.getName() : "(Ohne Name)";
            String einheit = (p.getEinheit() != null) ? p.getEinheit() : "";
            items[i] = name + " — " + p.getMenge() + " " + einheit + "  (min. " + p.getMindBestand() + ")";
        }

        new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                .setTitle("Unter Mindestbestand")
                .setMessage("Diese Produkte sind unter dem Mindestbestand:")
                .setItems(items, null)
                .setPositiveButton("Einkaufsliste erstellen", (d, which) -> {
                    if (currentHausId == null || currentHausId.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Kein Haushalt zugewiesen", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    com.example.haushalt_app_java.domain.AutomatischeEinkaufslisteService svc =
                            new com.example.haushalt_app_java.domain.AutomatischeEinkaufslisteService();

                    svc.automatischErstelleEinkaufsliste(
                            currentHausId,
                            () -> Toast.makeText(MainActivity.this, "Einkaufsliste erstellt", Toast.LENGTH_SHORT).show(),
                            () -> Toast.makeText(MainActivity.this, "Fehler beim Erstellen", Toast.LENGTH_SHORT).show()
                    );
                })
                .setNegativeButton("Später", null)
                .show();

        lowStockDialogShown = true;
    }
}