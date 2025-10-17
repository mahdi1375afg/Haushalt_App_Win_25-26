
package com.example.haushalt_app_java;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.haushalt_app_java.domain.Produkt;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import java.util.ArrayList;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private static final int REQ_UPDATE = 1001;
    private Button logout;

    private FirebaseDatabase database;// zentrale, regionsspezifische Instanz
    private FloatingActionButton pAddScreen;
    private ListView listView;
    private ArrayList<Produkt> produkten = new ArrayList<>();
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

        pAddScreen.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, p_addActivity2.class);
            startActivity(intent);
        });



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
                Intent i = new Intent(MainActivity.this, pUpdateActivity.class);
                i.putExtra("produkt_id", p.getProdukt_id());
                i.putExtra("name", p.getName());
                i.putExtra("menge", p.getMenge());
                i.putExtra("einheit", p.getEinheit());
                i.putExtra("kategorie", p.getKategorie());
                i.putExtra("mindBestand", p.getMindBestand());
                startActivityForResult(i, REQ_UPDATE);
            }
        });


        //add Produkt listener
        DatabaseReference dbref = database.getReference().child("Produkt");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear();
                produkten.clear(); // Liste leeren
                for (DataSnapshot snap: snapshot.getChildren()) {
                    Produkt produkt = snap.getValue(Produkt.class);
                    if (produkt == null) continue;
                    // ID aus dem DB-Key setzen
                    produkt.setProdukt_id(snap.getKey());
                    // Produkt in die Liste einfügen
                    produkten.add(produkt);

                    String name = produkt.getName() != null ? produkt.getName() : "";
                    String einheit = produkt.getEinheit() != null ? produkt.getEinheit() : "";
                    String kategorie = produkt.getKategorie() != null ? produkt.getKategorie() : "";
                    String txt = name + " - " + produkt.getMenge() + " " + einheit + " - " + kategorie;
                    items.add(txt);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Bottom Navigation Setup
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Bereits auf MainActivity - nichts tun
                return true;
            } else if (itemId == R.id.nav_household) {
                // Zu Haushalt navigieren
                Intent intent = new Intent(MainActivity.this, HaushaltActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });




    }
}