package com.example.haushalt_app_java.product_activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.StartActivity;
import com.example.haushalt_app_java.domain.EinkaufslistenActivity;
import com.example.haushalt_app_java.domain.Produkt;
import com.example.haushalt_app_java.haushalt_activity.HaushaltActivity;
import com.example.haushalt_app_java.profile.profile_Activity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private ArrayList<String> items = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private boolean lowStockDialogShown = false;

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

        database = FirebaseDatabase.getInstance(DB_URL);

        // Lade hausId und danach UI
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

        // Produkt hinzufügen
        pAddScreen.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, p_addActivity2.class);
            intent.putExtra("haus_id", currentHausId);
            startActivity(intent);
        });

        // Logout
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, StartActivity.class));
            finish();
        });

        // ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (produkten == null || position < 0 || position >= produkten.size()) return;
            Produkt p = produkten.get(position);
            if (p == null) return;

            Intent i = new Intent(MainActivity.this, pUpdateActivity.class);
            i.putExtra("produkt_id", p.getProdukt_id());
            i.putExtra("haus_id", p.getHaus_id());
            i.putExtra("name", p.getName());
            i.putExtra("menge", p.getMenge());
            i.putExtra("einheit", p.getEinheit());
            i.putExtra("kategorie", p.getKategorie());
            i.putExtra("mindBestand", p.getMindBestand());
            startActivityForResult(i, REQ_UPDATE);
        });

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // WICHTIG: aktuellen Tab markieren
        bottomNav.setSelectedItemId(R.id.nav_products);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_products) {
                return true;
            }
            if (itemId == R.id.nav_household) {
                startActivity(new Intent(MainActivity.this, HaushaltActivity.class));
                return true;
            }
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, profile_Activity.class));
                return true;
            }
            if (itemId == R.id.nav_einkaufslisten) {
                Intent intent = new Intent(MainActivity.this, EinkaufslistenActivity.class);
                intent.putExtra("hausId", currentHausId);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void loadProducts() {
        DatabaseReference produkteRef = database.getReference()
                .child("Hauser")
                .child(currentHausId)
                .child("produkte");

        produkteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear();
                produkten.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Produkt produkt = snap.getValue(Produkt.class);
                    if (produkt == null) continue;

                    produkt.setProdukt_id(snap.getKey());
                    produkt.setHaus_id(currentHausId);
                    produkten.add(produkt);

                    String name = produkt.getName() != null ? produkt.getName() : "";
                    String einheit = produkt.getEinheit() != null ? produkt.getEinheit() : "";
                    String kategorie = produkt.getKategorie() != null ? produkt.getKategorie() : "";
                    items.add(name + " - " + produkt.getMenge() + " " + einheit + " - " + kategorie);
                }

                adapter.notifyDataSetChanged();
                checkAndShowLowStockDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Fehler beim Laden: " + error.getMessage());
            }
        });
    }

    private void checkAndShowLowStockDialog() {
        if (lowStockDialogShown) return;
        if (produkten == null || produkten.isEmpty()) return;

        ArrayList<Produkt> low = new ArrayList<>();
        for (Produkt p : produkten) {
            if (p == null) continue;

            if (p.getMindBestand() > 0 && p.getMenge() < p.getMindBestand()) {
                low.add(p);
            }
        }

        if (low.isEmpty()) return;

        String[] items = new String[low.size()];
        for (int i = 0; i < low.size(); i++) {
            Produkt p = low.get(i);
            items[i] = p.getName() + " — " + p.getMenge() + " " + p.getEinheit()
                    + "  (min. " + p.getMindBestand() + ")";
        }

        new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                .setTitle("Unter Mindestbestand")
                .setMessage("Diese Produkte sind unter dem Mindestbestand:")
                .setItems(items, null)
                .setPositiveButton("Einkaufsliste erstellen", (d, which) -> {
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

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_products);
    }
}
