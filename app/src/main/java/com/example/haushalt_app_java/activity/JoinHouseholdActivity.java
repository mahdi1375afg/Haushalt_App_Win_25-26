package com.example.haushalt_app_java.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.haushalt_app_java.haushalt_activity.AddUserActivity;
import com.example.haushalt_app_java.haushalt_activity.HaushaltActivity;
import com.example.haushalt_app_java.utils.HausIdManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class JoinHouseholdActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;

    private String householdName = "not yet determined";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance(DB_URL);
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null && data.getScheme().equals("haushaltapp") && data.getHost().equals("join")) {
            String hausId = data.getQueryParameter("hausId");
            if (hausId != null && !hausId.isEmpty()) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    addUserToHousehold(currentUser.getUid(), hausId);
                } else {
                    // User is not logged in, redirect to login and handle join after login
                    Toast.makeText(this, "Bitte zuerst anmelden.", Toast.LENGTH_SHORT).show();
                    // You could store the hausId in SharedPreferences and retrieve it after login
                    finish();
                }
            } else {
                Toast.makeText(this, "Ungültiger Einladungslink", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            finish();
        }
    }

    private void addUserToHousehold(String userId, String hausId) {
        DatabaseReference mitgliederRef = database.getReference().child("Hauser").child(hausId).child("mitgliederIds").child(userId);
        mitgliederRef.setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                HausIdManager.getInstance().setHausId(hausId);

                // ✅ Füge zu Haushalt hinzu
                database.getReference("Hauser").child(hausId)
                        .child("mitgliederIds").child(userId).setValue(true);

//                String householdName = database.getReference("Hauser")
//                                                .child(hausId)
//                                                .child("name").get();

                // Get a reference to the "name" field in the specific household
                DatabaseReference householdNameRef = database.getReference("Hauser")
                        .child(hausId)
                        .child("name");

                // Asynchronously fetch the data
                householdNameRef.get().addOnSuccessListener(dataSnapshot -> {
                    // This block executes ONLY if the data is fetched successfully
                    if (dataSnapshot.exists()) {
                        householdName = dataSnapshot.getValue(String.class);
                        // Now you have the name! Update the UI or use the value here.
                        // For example:
                        // textView.setText(householdName);
                        Log.d("FirebaseSuccess", "Successfully fetched household name: " + householdName);
                    } else {
                        // Handle the case where the "name" field does not exist for this household
                        Log.w("FirebaseWarning", "Household name does not exist for ID: " + hausId);
                    }
                }).addOnFailureListener(e -> {
                    // This block executes if the read operation fails (e.g., network error, permissions denied)
                    Log.e("FirebaseError", "Error fetching household name", e);
                    // You could show an error message to the user here
                });

                // IMPORTANT: Any code here will execute immediately, BEFORE the data has been fetched.
                // Do not try to use `householdName` here.

                Log.e("UserId", userId);
                Log.e("HausId", hausId);

                // ✅ Setze hausId beim Benutzer
                database.getReference("Benutzer").child(userId)
                        .child("hausId").setValue(hausId)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(JoinHouseholdActivity.this,
                                    "Du wurdest hinzugefügt zum Haushalt " + householdName, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(JoinHouseholdActivity.this,
                                    "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });

                Toast.makeText(this, "Haushalt beigetreten!", Toast.LENGTH_SHORT).show();
                Intent mainIntent = new Intent(this, HaushaltActivity.class);
                mainIntent.putExtra("hausId", hausId);
                startActivity(mainIntent);
            } else {
                Toast.makeText(this, "Fehler beim Beitritt zum Haushalt", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }
}
