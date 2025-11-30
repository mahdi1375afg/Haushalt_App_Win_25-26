package com.example.haushalt_app_java.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.haushalt_app_java.haushalt_activity.HaushaltActivity;
import com.example.haushalt_app_java.utils.HausIdManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JoinHouseholdActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance(DB_URL);
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null && "haushaltapp".equals(data.getScheme()) && "join".equals(data.getHost())) {
            String newHausId = data.getQueryParameter("hausId");
            if (newHausId != null && !newHausId.isEmpty()) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    checkUserAndJoinHousehold(currentUser.getUid(), newHausId);
                } else {
                    Toast.makeText(this, "Bitte zuerst anmelden.", Toast.LENGTH_SHORT).show();
                    // Optionally, redirect to login and pass the deep link URI
                    finish();
                }
            } else {
                Toast.makeText(this, "Ungültiger Einladungslink.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            finish();
        }
    }

    private void checkUserAndJoinHousehold(String userId, String newHausId) {
        DatabaseReference userHausIdRef = database.getReference("Benutzer").child(userId).child("hausId");
        userHausIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String oldHausId = snapshot.exists() ? snapshot.getValue(String.class) : null;

                if (oldHausId != null && !oldHausId.isEmpty()) {
                    if (oldHausId.equals(newHausId)) {
                        Toast.makeText(JoinHouseholdActivity.this, "Du bist bereits in diesem Haushalt.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // User is in another household, show confirmation dialog
                    new MaterialAlertDialogBuilder(JoinHouseholdActivity.this)
                            .setTitle("Haushalt wechseln?")
                            .setMessage("Du bist bereits in einem anderen Haushalt. Möchtest du ihn verlassen und diesem neuen Haushalt beitreten?")
                            .setPositiveButton("Beitreten", (dialog, which) -> {
                                removeUserFromOldHousehold(userId, oldHausId, () -> addUserToHousehold(userId, newHausId));
                            })
                            .setNegativeButton("Abbrechen", (dialog, which) -> {
                                Toast.makeText(JoinHouseholdActivity.this, "Beitritt abgebrochen.", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .setOnCancelListener(dialog -> finish())
                            .show();
                } else {
                    // User is not in any household, join directly
                    addUserToHousehold(userId, newHausId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(JoinHouseholdActivity.this, "Fehler: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void removeUserFromOldHousehold(String userId, String oldHausId, Runnable onComplete) {
        if (oldHausId == null || userId == null) {
            onComplete.run();
            return;
        }
        DatabaseReference oldMitgliederRef = database.getReference("Hauser").child(oldHausId).child("mitgliederIds").child(userId);
        oldMitgliederRef.removeValue().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w("JoinHousehold", "Konnte Benutzer nicht aus altem Haushalt entfernen: " + task.getException());
                // Still continue, the user's profile will be overwritten anyway
            }
            onComplete.run();
        });
    }

    private void addUserToHousehold(String userId, String hausId) {
        // 1. Add user to the new household's member list
        database.getReference("Hauser").child(hausId).child("mitgliederIds").child(userId)
                .setValue(true)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Fehler beim Beitritt zum Haushalt.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnSuccessListener(aVoid -> {
                    // 2. Update the user's profile with the new hausId
                    database.getReference("Benutzer").child(userId).child("hausId").setValue(hausId)
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Fehler beim Aktualisieren des Profils.", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnSuccessListener(bVoid -> {
                                // 3. Both DB operations were successful. Update local state and navigate.
                                HausIdManager.getInstance().setHausId(hausId);
                                Toast.makeText(this, "Haushalt erfolgreich beigetreten!", Toast.LENGTH_SHORT).show();

                                Intent mainIntent = new Intent(this, HaushaltActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                mainIntent.putExtra("hausId", hausId);
                                startActivity(mainIntent);
                                finish(); // Finish this activity
                            });
                });
    }
}
