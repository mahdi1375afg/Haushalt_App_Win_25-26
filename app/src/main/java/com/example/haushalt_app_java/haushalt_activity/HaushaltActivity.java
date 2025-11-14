package com.example.haushalt_app_java.haushalt_activity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.domain.EinkaufslistenActivity;
import com.example.haushalt_app_java.product_activity.MainActivity;
import com.example.haushalt_app_java.profile.profile_Activity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.content.Intent;
import android.widget.PopupMenu;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HaushaltActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";

    // Request Codes für alle Activities
    private static final int REQUEST_ADD_HAUSHALT = 1;
    private static final int REQUEST_ADD_USER = 2;
    private static final int REQUEST_DELETE_USER = 3;
    private static final int REQUEST_DELETE_HAUSHALT = 4;

    private FirebaseDatabase db;
    private ListView hList;
    private FloatingActionButton hAdd;
    private ArrayAdapter<String> adapter;
    private List<String> mitgliederNamen;
    private Set<String> geladeneIds;
    private ListView haushaltListView;
    private List<String> haushaltIds;
    private List<String> haushaltNamen;
    private ArrayAdapter<String> haushaltAdapter;
    private String selectedHausId = null;
    private String currentHausId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_haushalt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        hAdd = findViewById(R.id.hAdd);
        hList = findViewById(R.id.hList);
        haushaltListView = findViewById(R.id.haushalt_listview);
        db = FirebaseDatabase.getInstance(DB_URL);

        mitgliederNamen = new ArrayList<>();
        geladeneIds = new HashSet<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mitgliederNamen);
        hList.setAdapter(adapter);

        haushaltIds = new ArrayList<>();
        haushaltNamen = new ArrayList<>();
        haushaltAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, haushaltNamen);
        haushaltListView.setAdapter(haushaltAdapter);


        // ✅ Hole hausId aus Intent oder Manager
        currentHausId = getIntent().getStringExtra("hausId");
        if (currentHausId == null) {
            currentHausId = com.example.haushalt_app_java.utils.HausIdManager.getInstance().getHausId();
        }

        // ✅ Speichere hausId falls noch nicht gesetzt
        if (currentHausId != null) {
            com.example.haushalt_app_java.utils.HausIdManager.getInstance().setHausId(currentHausId);
        }
        // Bottom Navigation

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView2);
        bottomNav.setSelectedItemId(R.id.nav_household);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_household) {
                return true;
            } else if (itemId == R.id.nav_products) {
                Intent intent = new Intent(HaushaltActivity.this, MainActivity.class);
                intent.putExtra("haus_id", currentHausId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(HaushaltActivity.this, profile_Activity.class));
                return true;
            } else if (itemId == R.id.nav_einkaufslisten) {
                Intent intent = new Intent(HaushaltActivity.this, EinkaufslistenActivity.class);
                intent.putExtra("hausId", currentHausId);
                startActivity(intent);
                return true;
            }
            return false;
        });


        loadHaushaltDaten();

        // Click: Mitglieder anzeigen
        haushaltListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedHausId = haushaltIds.get(position);
            ladeMitglieder(selectedHausId);
        });

        // Long-Click: Haushalt bearbeiten/löschen
        haushaltListView.setOnItemLongClickListener((parent, view, position, id) -> {
            String hausId = haushaltIds.get(position);
            Intent intent = new Intent(HaushaltActivity.this, delete_haushalt_Activity.class);
            intent.putExtra("hausId", hausId);
            startActivityForResult(intent, REQUEST_DELETE_HAUSHALT);  // ✅ Mit Request Code
            return true;
        });

        // Click: Mitglied löschen
        hList.setOnItemClickListener((parent, view, position, id) -> {
            String mitgliedName = mitgliederNamen.get(position);
            Intent intent = new Intent(HaushaltActivity.this, delete_mitglied_Activity.class);
            intent.putExtra("mitgliedName", mitgliedName);
            intent.putExtra("hausId", selectedHausId);
            startActivityForResult(intent, REQUEST_DELETE_USER);  // ✅ Mit Request Code
        });

        // Floating Button: Menü für Haushalt/User hinzufügen
        hAdd.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(HaushaltActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.add_haushalt, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.add_user) {
                    Intent intent = new Intent(HaushaltActivity.this, AddUserActivity.class);
                    startActivityForResult(intent, REQUEST_ADD_USER);  // ✅ Mit Request Code
                    return true;
                } else if (itemId == R.id.add_household) {
                    Intent intent = new Intent(HaushaltActivity.this, AddHaushaltActivity.class);
                    startActivityForResult(intent, REQUEST_ADD_HAUSHALT);  // ✅ Mit Request Code
                    return true;
                }
                return false;
            });

            popup.show();
        });
    }

    // ✅ WICHTIG: Auto-Reload nach jeder Änderung
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            android.util.Log.d("HaushaltActivity", "✓ Änderung erkannt - Lade Daten neu");

            // Lade IMMER Haushalte neu
            loadHaushaltDaten();

            // Bei User-Operationen: Lade auch Mitglieder neu
            if ((requestCode == REQUEST_ADD_USER || requestCode == REQUEST_DELETE_USER)
                && selectedHausId != null) {
                ladeMitglieder(selectedHausId);
            }

            // Bei Haushalt-Löschung: Lösche Mitglieder-Liste
            if (requestCode == REQUEST_DELETE_HAUSHALT) {
                selectedHausId = null;
                mitgliederNamen.clear();
                adapter.notifyDataSetChanged();
            }

            Toast.makeText(this, "Daten aktualisiert", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHaushaltDaten();
    }

    private void loadHaushaltDaten() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Nicht eingeloggt!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.getReference().child("Hauser")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    haushaltIds.clear();
                    haushaltNamen.clear();

                    for (DataSnapshot hausSnapshot : snapshot.getChildren()) {
                        String hausId = hausSnapshot.getKey();
                        DataSnapshot mitgliederSnapshot = hausSnapshot.child("mitgliederIds");

                        if (mitgliederSnapshot.hasChild(userId)) {
                            String hausName = hausSnapshot.child("name").getValue(String.class);
                            if (hausName != null) {
                                haushaltIds.add(hausId);
                                haushaltNamen.add(hausName);
                            }
                        }
                    }

                    haushaltAdapter.notifyDataSetChanged();
                    android.util.Log.d("HaushaltActivity", "✓ " + haushaltIds.size() + " Haushalte geladen");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HaushaltActivity.this,
                        "Fehler beim Laden der Haushalte", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void ladeMitglieder(String haushaltId) {
        mitgliederNamen.clear();
        geladeneIds.clear();
        adapter.notifyDataSetChanged();

        db.getReference().child("Hauser").child(haushaltId).child("mitgliederIds")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String mitgliedId;

                        Object value = child.getValue();
                        if (value instanceof String) {
                            mitgliedId = (String) value;
                        } else if (value instanceof Boolean && (Boolean) value) {
                            mitgliedId = child.getKey();
                        } else {
                            continue;
                        }

                        if (mitgliedId == null || geladeneIds.contains(mitgliedId)) {
                            continue;
                        }

                        geladeneIds.add(mitgliedId);

                        db.getReference().child("Benutzer").child(mitgliedId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    String name = userSnapshot.child("name").getValue(String.class);
                                    if (name != null && !mitgliederNamen.contains(name)) {
                                        mitgliederNamen.add(name);
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    android.util.Log.e("HaushaltActivity",
                                        "Fehler beim Laden des Benutzers: " + error.getMessage());
                                }
                            });
                    }

                    android.util.Log.d("HaushaltActivity", "✓ " + mitgliederNamen.size() + " Mitglieder geladen");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HaushaltActivity.this,
                        "Fehler beim Laden der Mitglieder", Toast.LENGTH_SHORT).show();
                }
            });
    }
}