package com.example.haushalt_app_java;

import android.os.Bundle;
import android.widget.TextView;
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
import com.google.firebase.database.DatabaseReference;
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
    private TextView hName;
    private ListView hList;
    private FloatingActionButton hAdd;
    private ArrayAdapter<String> adapter;
    private List<String> mitgliederNamen;
    private Set<String> geladeneIds;

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
        hName = findViewById(R.id.hName);
        hList = findViewById(R.id.hList);
        db = FirebaseDatabase.getInstance(DB_URL);

        mitgliederNamen = new ArrayList<>();
        geladeneIds = new HashSet<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mitgliederNamen);
        hList.setAdapter(adapter);

        loadHaushaltDaten();

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
        DatabaseReference userRef = db.getReference().child("Benutzer").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String haushaltId = snapshot.child("hausId").getValue(String.class);

                if (haushaltId == null || haushaltId.isEmpty()) {
                    hName.setText("Kein Haushalt");
                    mitgliederNamen.clear();
                    geladeneIds.clear();
                    adapter.notifyDataSetChanged();
                    return;
                }

                DatabaseReference haushaltRef = db.getReference().child("Hauser").child(haushaltId);

                haushaltRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String hausName = snapshot.child("name").getValue(String.class);
                        hName.setText(hausName != null ? hausName : "Haushalt");

                        mitgliederNamen.clear();
                        geladeneIds.clear();
                        adapter.notifyDataSetChanged();

                        DataSnapshot mitgliederSnapshot = snapshot.child("mitgliederIds");

                        if (mitgliederSnapshot.getChildrenCount() == 0) {
                            return;
                        }

                        for (DataSnapshot mitgliedSnapshot : mitgliederSnapshot.getChildren()) {
                            String mitgliedId = mitgliedSnapshot.getValue(String.class);

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
                                    }
                                });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HaushaltActivity.this, "Fehler beim Laden", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HaushaltActivity.this, "Fehler beim Laden", Toast.LENGTH_SHORT).show();
            }
        });
    }
}