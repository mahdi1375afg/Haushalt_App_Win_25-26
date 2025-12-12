package com.example.haushalt_app_java.haushalt;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.haushalt_app_java.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AddHaushaltActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseDatabase db;
    private EditText hName;
    private Button hAddName;
    private ImageView back;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_haushalt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseDatabase.getInstance(DB_URL);
        hName = findViewById(R.id.hName);
        hAddName = findViewById(R.id.hAddName);
        back = findViewById(R.id.back_button);

        back.setOnClickListener(v -> finish());

        hAddName.setOnClickListener(v -> {
            String haushaltName = hName.getText().toString().trim();

            if (haushaltName.isEmpty()) {
                Toast.makeText(this, "Bitte einen Namen eingeben", Toast.LENGTH_SHORT).show();
                return;
            }

            haushaltErstellen(haushaltName);
        });
    }

    private void haushaltErstellen(String name) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.getReference("Benutzer").child(userId).child("hausId")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(AddHaushaltActivity.this,
                            "Sie sind bereits einem Haushalt zugeordnet",
                            Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    String hausId = db.getReference("Haushalte").push().getKey();
                    if (hausId == null) return;

                    Map<String, Object> haushalt = new HashMap<>();
                    haushalt.put("haus_id", hausId);
                    haushalt.put("name", name);

                    // ✅ RICHTIG: Mitglieder als verschachtelte Map
                    Map<String, Object> mitglieder = new HashMap<>();
                    mitglieder.put(userId, true);
                    haushalt.put("mitgliederIds", mitglieder);

                    db.getReference("Haushalte").child(hausId).setValue(haushalt)
                        .addOnSuccessListener(aVoid -> {
                            db.getReference("Benutzer").child(userId).child("hausId")
                                .setValue(hausId)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(AddHaushaltActivity.this,
                                        "Haushalt erstellt", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                });
                        });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }
}