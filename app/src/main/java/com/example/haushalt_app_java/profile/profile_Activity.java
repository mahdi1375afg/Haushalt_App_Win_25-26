package com.example.haushalt_app_java.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.StartActivity;
import com.example.haushalt_app_java.haushalt_activity.HaushaltActivity;
import com.example.haushalt_app_java.product_activity.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class profile_Activity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private static final int REQUEST_UPDATE_PROFILE = 1;

    private ListView kontoListe;
    private Button konto_delete;
    private Button konto_bearbeiten;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase db;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> kontoInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance(DB_URL);

        kontoListe = findViewById(R.id.kontoListe);
        konto_delete = findViewById(R.id.konot_delete);
        konto_bearbeiten = findViewById(R.id.konto_bearbeiten);

        loadKontoInfo();

        konto_bearbeiten.setOnClickListener(v -> {
            Intent intent = new Intent(profile_Activity.this, profile_update_Activity.class);
            startActivityForResult(intent, REQUEST_UPDATE_PROFILE);
        });

        konto_delete.setOnClickListener(v -> kontoLoeschen());

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                return true;
            } else if (itemId == R.id.nav_household) {
                startActivity(new Intent(profile_Activity.this, HaushaltActivity.class));
                return true;
            } else if (itemId == R.id.nav_products) {
                startActivity(new Intent(profile_Activity.this, MainActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadKontoInfo() {
        kontoInfoList = new ArrayList<>();





        if (currentUser != null) {
            // Nur Name aus Realtime Database laden
            db.getReference().child("Benutzer").child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                        String name = userSnapshot.child("name").getValue(String.class);
                        if (name != null && !name.isEmpty()) {
                            kontoInfoList.add("Name: " + name);
                        } else {
                            kontoInfoList.add("Name: Nicht gesetzt");
                        }

                        if (adapter == null) {
                            adapter = new ArrayAdapter<>(profile_Activity.this,
                                android.R.layout.simple_list_item_1, kontoInfoList);
                            kontoListe.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("ProfileActivity",
                            "Fehler beim Laden: " + error.getMessage());
                        kontoInfoList.add("Name: Fehler beim Laden");
                        if (adapter == null) {
                            adapter = new ArrayAdapter<>(profile_Activity.this,
                                android.R.layout.simple_list_item_1, kontoInfoList);
                            kontoListe.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
        }
        if (currentUser != null) {
            kontoInfoList.add("Email: " + currentUser.getEmail());
            kontoInfoList.add("UID: " + currentUser.getUid());
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, kontoInfoList);
        kontoListe.setAdapter(adapter);
    }

    private void kontoLoeschen() {
        new AlertDialog.Builder(this)
            .setTitle("Konto löschen")
            .setMessage("Möchten Sie Ihr Konto wirklich dauerhaft löschen?")
            .setPositiveButton("Löschen", (dialog, which) -> {
                if (currentUser != null) {
                    String userId = currentUser.getUid();

                    // 1. Aus allen Haushalten entfernen
                    db.getReference().child("Hauser")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot hausSnapshot : snapshot.getChildren()) {
                                    hausSnapshot.child("mitgliederIds").getRef()
                                        .child(userId).removeValue();
                                }

                                // 2. Benutzerdaten löschen
                                db.getReference().child("Benutzer").child(userId).removeValue();

                                // 3. Firebase Auth löschen
                                currentUser.delete().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(profile_Activity.this,
                                            "Konto gelöscht", Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                        Intent intent = new Intent(profile_Activity.this, StartActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(profile_Activity.this,
                                            "Fehler: " + task.getException().getMessage(),
                                            Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(profile_Activity.this,
                                    "Fehler beim Löschen", Toast.LENGTH_SHORT).show();
                            }
                        });
                }
            })
            .setNegativeButton("Abbrechen", null)
            .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UPDATE_PROFILE && resultCode == RESULT_OK) {
            loadKontoInfo();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}