package com.example.haushalt_app_java;

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

        loadHaushaltDaten();

        // Normaler Click: Mitglieder anzeigen
        haushaltListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedHausId = haushaltIds.get(position);
            ladeMitglieder(selectedHausId);
        });

        // Long-Click: Zur Delete-Activity wechseln
        haushaltListView.setOnItemLongClickListener((parent, view, position, id) -> {
            String hausId = haushaltIds.get(position);
            Intent intent = new Intent(HaushaltActivity.this, delete_haushalt_Activity.class);
            intent.putExtra("hausId", hausId);
            startActivity(intent);
            return true;
        });

        hAdd.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(HaushaltActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.add_haushalt, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.add_user) {
                    Intent intent = new Intent(HaushaltActivity.this, AddUserActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.add_household) {
                    Intent intent = new Intent(HaushaltActivity.this, AddHaushaltActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            });

            popup.show();
        });
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

        db.getReference().child("Hauser").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                haushaltIds.clear();
                haushaltNamen.clear();

                for (DataSnapshot haushaltSnapshot : snapshot.getChildren()) {
                    DataSnapshot mitgliederSnapshot = haushaltSnapshot.child("mitgliederIds");

                    for (DataSnapshot mitgliedSnapshot : mitgliederSnapshot.getChildren()) {
                        String mitgliedId = mitgliedSnapshot.getValue(String.class);

                        if (userId.equals(mitgliedId)) {
                            String haushaltId = haushaltSnapshot.getKey();
                            String haushaltName = haushaltSnapshot.child("name").getValue(String.class);

                            haushaltIds.add(haushaltId);
                            haushaltNamen.add(haushaltName != null ? haushaltName : "Unbenannt");
                            break;
                        }
                    }
                }

                haushaltAdapter.notifyDataSetChanged();

                if (haushaltIds.isEmpty()) {
                    mitgliederNamen.clear();
                    adapter.notifyDataSetChanged();
                } else if (!haushaltIds.isEmpty() && selectedHausId == null) {
                    selectedHausId = haushaltIds.get(0);
                    ladeMitglieder(selectedHausId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HaushaltActivity.this, "Fehler beim Laden", Toast.LENGTH_SHORT).show();
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
                    for (DataSnapshot mitgliedSnapshot : snapshot.getChildren()) {
                        String mitgliedId = mitgliedSnapshot.getValue(String.class);

                        if (mitgliedId == null || geladeneIds.contains(mitgliedId)) continue;

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
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HaushaltActivity.this, "Fehler beim Laden der Mitglieder", Toast.LENGTH_SHORT).show();
                }
            });
    }
}