
package com.example.haushalt_app_java;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class MainActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";

    private Button logout;

    private FirebaseDatabase database;// zentrale, regionsspezifische Instanz
    private FloatingActionButton pAddScreen;
    private ListView listView;
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

        DatabaseReference dbref = database.getReference().child("Produkt");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear();
                for (DataSnapshot snap: snapshot.getChildren()) {
                    Produkt produkt = snap.getValue(Produkt.class);
                    String txt = produkt.getName() + " - " + produkt.getMenge() + " " + produkt.getEinheit() + " - " + produkt.getKategorie();
                    items.add(txt);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });




    }
}