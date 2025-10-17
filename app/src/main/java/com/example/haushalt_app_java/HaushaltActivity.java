package com.example.haushalt_app_java;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import android.content.Intent;
import android.view.View;
import android.widget.PopupMenu;


public class HaushaltActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseDatabase db; // zentrale, regionsspezifische Instanz
    private EditText hName;
    private ListView hList;
    private FloatingActionButton hAdd;

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
        hAdd.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(HaushaltActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.add_haushalt, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.add_user) {
                    // Nutzer hinzufügen
                    Intent intent = new Intent(HaushaltActivity.this, AddUserActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.add_household) {
                    // Haushalt hinzufügen
                    Intent intent = new Intent(HaushaltActivity.this, AddHaushaltActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            });

            popup.show();
        });


    }
}