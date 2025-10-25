package com.example.haushalt_app_java.haushalt_activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.util.Log;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddHaushaltActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseDatabase db;
    private TextView hName;
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
        hAddName.setOnClickListener(v -> createHaushalt());
    }

    private void createHaushalt() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Nicht eingeloggt!", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = hName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Bitte Namen eingeben", Toast.LENGTH_SHORT).show();
            return;
        }

        String creatorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference neuerHaushaltRef = db.getReference().child("Hauser").push();
        String haushaltId = neuerHaushaltRef.getKey();

        if (haushaltId == null) {
            Toast.makeText(this, "Fehler beim Generieren der ID", Toast.LENGTH_SHORT).show();
            return;
        }

        neuerHaushaltRef.child("haus_id").setValue(haushaltId);
        neuerHaushaltRef.child("name").setValue(name);
        neuerHaushaltRef.child("lowercaseName").setValue(name.toLowerCase());
        neuerHaushaltRef.child("mitgliederIds").child(creatorId).setValue(true)
            .addOnSuccessListener(aVoid -> {
                ensureUserExists(creatorId, haushaltId);
                Toast.makeText(AddHaushaltActivity.this,
                    "Haushalt '" + name + "' erstellt!", Toast.LENGTH_SHORT).show();
                hName.setText("");

                // ✅ WICHTIG: Signalisiere Erfolg
                setResult(RESULT_OK);
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(AddHaushaltActivity.this,
                    "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void ensureUserExists(String userId, String haushaltId) {
        DatabaseReference userRef = db.getReference().child("Benutzer").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    String displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                    String userName = (displayName != null && !displayName.isEmpty())
                        ? displayName
                        : "Unbekannt";

                    userRef.child("name").setValue(userName);
                    userRef.child("userId").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AddHaushalt", "Fehler: " + error.getMessage());
            }
        });
    }
}