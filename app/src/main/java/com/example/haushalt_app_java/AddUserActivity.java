package com.example.haushalt_app_java;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddUserActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseDatabase database;
    private EditText name_input;
    private Button add_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = FirebaseDatabase.getInstance(DB_URL);
        name_input = findViewById(R.id.name_input);
        add_user = findViewById(R.id.add_user);

        add_user.setOnClickListener(v -> {
            String userName = name_input.getText().toString().trim();

            if (userName.isEmpty()) {
                Toast.makeText(this, "Bitte Namen eingeben", Toast.LENGTH_SHORT).show();
                return;
            }

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "Nicht eingeloggt!", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userRef = database.getReference().child("Benutzer").child(currentUserId);

            // Hole die haushaltId des aktuellen Benutzers
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String haushaltId = snapshot.child("hausId").getValue(String.class);

                    if (haushaltId == null || haushaltId.isEmpty()) {
                        Toast.makeText(AddUserActivity.this, "Du bist keinem Haushalt zugeordnet!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Suche Benutzer nach Namen
                    DatabaseReference allUsersRef = database.getReference().child("Benutzer");
                    allUsersRef.orderByChild("name").equalTo(userName)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    Toast.makeText(AddUserActivity.this, "Benutzer nicht gefunden", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Nehme den ersten gefundenen Benutzer
                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    String foundUserId = userSnapshot.getKey();

                                    // Füge Benutzer zum Haushalt hinzu
                                    DatabaseReference haushaltRef = database.getReference()
                                        .child("Hauser").child(haushaltId).child("mitgliederIds");

                                    haushaltRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            boolean alreadyMember = false;
                                            for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                                                if (foundUserId.equals(memberSnapshot.getValue(String.class))) {
                                                    alreadyMember = true;
                                                    break;
                                                }
                                            }

                                            if (alreadyMember) {
                                                Toast.makeText(AddUserActivity.this, "Benutzer ist bereits Mitglied", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Füge neue Mitglieds-ID hinzu
                                                haushaltRef.push().setValue(foundUserId)
                                                    .addOnSuccessListener(aVoid -> {
                                                        // Aktualisiere hausId beim hinzugefügten Benutzer
                                                        database.getReference().child("Benutzer")
                                                            .child(foundUserId).child("hausId").setValue(haushaltId);

                                                        Toast.makeText(AddUserActivity.this, "Benutzer hinzugefügt!", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(AddUserActivity.this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(AddUserActivity.this, "Fehler beim Laden", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    break;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(AddUserActivity.this, "Fehler bei der Suche", Toast.LENGTH_SHORT).show();
                            }
                        });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AddUserActivity.this, "Fehler beim Laden", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}