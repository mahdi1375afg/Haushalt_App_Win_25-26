package com.example.haushalt_app_java.product_activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.domain.Haushalt;
import com.example.haushalt_app_java.haushalt_activity.AddHaushaltActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Haushalt_Auswahl_Produkt_Activity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";

    private RecyclerView recyclerView;
    private HaushaltAdapter adapter;
    private List<Haushalt> haushalte = new ArrayList<>();
    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_haushalt_auswahl_produkt);

        db = FirebaseDatabase.getInstance(DB_URL);
        recyclerView = findViewById(R.id.recyclerViewHaushalte);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HaushaltAdapter(haushalte, haushalt -> {
            Intent intent = new Intent(Haushalt_Auswahl_Produkt_Activity.this, MainActivity.class);
            intent.putExtra("haus_id", haushalt.getHaus_id());
            intent.putExtra("haus_name", haushalt.getName());
            startActivity(intent);
            finish();
        });

        recyclerView.setAdapter(adapter);
        ladeHaushalte();
    }

    private void ladeHaushalte() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.getReference().child("Benutzer").child(userId).child("haushaltId")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    haushalte.clear();

                    if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                        zeigeKeinHaushaltDialog();
                        return;
                    }


                    for (DataSnapshot hausIdSnapshot : snapshot.getChildren()) {
                        String hausId = hausIdSnapshot.getKey();

                        db.getReference().child("Hauser").child(hausId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot hausSnapshot) {
                                    String name = hausSnapshot.child("name").getValue(String.class);
                                    if (name != null) {
                                        haushalte.add(new Haushalt(hausId, name, null));
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(Haushalt_Auswahl_Produkt_Activity.this,
                                        "Fehler beim Laden", Toast.LENGTH_SHORT).show();
                                }
                            });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Haushalt_Auswahl_Produkt_Activity.this,
                        "Fehler beim Laden", Toast.LENGTH_SHORT).show();
                }
            });

    }
    private void zeigeKeinHaushaltDialog() {
        // Activity-Hintergrund auf die Farbe setzen
        findViewById(android.R.id.content).setBackgroundColor(getResources().getColor(R.color.ux_color_primary));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Kein Haushalt")
                .setMessage("Du bist keinem Haushalt zugewiesen. Möchtest du einen erstellen?")
                .setPositiveButton("Erstellen", (d, which) -> {
                    startActivity(new Intent(this, AddHaushaltActivity.class));
                    finish();

                })
                .setNegativeButton("Abbrechen", (d, which) -> finish())
                .setCancelable(false)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                new android.graphics.drawable.ColorDrawable(getResources().getColor(R.color.ux_color_primary))
            );
        }
    }

}