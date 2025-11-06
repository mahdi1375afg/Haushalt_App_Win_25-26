
package com.example.haushalt_app_java.product_activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
import com.example.haushalt_app_java.domain.Produkt;
import com.example.haushalt_app_java.haushalt_activity.HaushaltActivity;
import com.example.haushalt_app_java.profile.profile_Activity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import android.util.Log;
import java.util.ArrayList;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private static final int REQ_UPDATE = 1001;
    private Button logout;
    private String currentHausId = null;

    private FirebaseDatabase database;// zentrale, regionsspezifische Instanz
    private FloatingActionButton pAddScreen;
    private ListView listView;
    private ArrayList<Produkt> produkten = new ArrayList<>();

    private boolean lowStockDialogShown = false;

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
        pAddScreen = findViewById(R.id.pAddScreen);
        listView = findViewById(R.id.listViewp);

        // Add Produkt Screen öffnen
        pAddScreen.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, p_addActivity2.class);
            startActivity(intent);
        });


        // Logout Funktionalität
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, StartActivity.class));
            finish();
        });



        ArrayList<String> items = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (produkten == null || position < 0 || position >= produkten.size()) return;
                Produkt p = produkten.get(position);
                if (p == null) return;
                Log.d("MainActivity", "Öffne Update für Produkt: " + p.getProdukt_id() + " in Haus: " + p.getHaus_id());

                Intent i = new Intent(MainActivity.this, pUpdateActivity.class);
                i.putExtra("produkt_id", p.getProdukt_id());
                i.putExtra("haus_id", p.getHaus_id());
                i.putExtra("name", p.getName());
                i.putExtra("menge", p.getMenge());
                i.putExtra("einheit", p.getEinheit());
                i.putExtra("kategorie", p.getKategorie());
                i.putExtra("mindBestand", p.getMindBestand());
                startActivityForResult(i, REQ_UPDATE);
            }
        });

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = database.getReference().child("Benutzer").child(userId);

        //listview von prdukte anzeigen
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(MainActivity.this, "Benutzer nicht gefunden", Toast.LENGTH_SHORT).show();
                    return;
                }

                currentHausId = snapshot.child("hausId").getValue(String.class);
                if (currentHausId == null || currentHausId.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Kein Haushalt zugewiesen", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Jetzt Produkte aus dem Haushalt laden
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
                            String txt = name + " - " + produkt.getMenge() + " " + einheit + " - " + kategorie;
                            items.add(txt);
                        }
                        adapter.notifyDataSetChanged();
                        new android.os.Handler().postDelayed(() -> checkAndShowLowStockDialog(), 400);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MainActivity", "Fehler beim Laden: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Fehler beim Laden", Toast.LENGTH_SHORT).show();
            }
        });

        // Bottom Navigation Setup
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_products) {
                return true;
            } else if (itemId == R.id.nav_household) {
                Intent intent = new Intent(MainActivity.this, HaushaltActivity.class);
                startActivity(intent);
                return true;
            }
            else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(MainActivity.this, profile_Activity.class);
                startActivity(intent);
                return true;

            }
            return false;
        });
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
                        android.widget.Toast.makeText(MainActivity.this, "Kein Haushalt zugewiesen", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                    com.example.haushalt_app_java.domain.AutomatischeEinkaufslisteService svc =
                            new com.example.haushalt_app_java.domain.AutomatischeEinkaufslisteService();

                    svc.automatischErstelleEinkaufsliste(
                            currentHausId,
                            () -> android.widget.Toast.makeText(MainActivity.this, "Einkaufsliste erstellt", android.widget.Toast.LENGTH_SHORT).show(),
                            () -> android.widget.Toast.makeText(MainActivity.this, "Fehler beim Erstellen", android.widget.Toast.LENGTH_SHORT).show()
                    );
                })
                .setNegativeButton("Später", null)
                .show();

        lowStockDialogShown = true;
    }

}