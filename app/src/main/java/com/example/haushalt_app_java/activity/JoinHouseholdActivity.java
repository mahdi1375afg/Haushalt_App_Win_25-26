package com.example.haushalt_app_java.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.haushalt_activity.HaushaltActivity;
import com.example.haushalt_app_java.utils.HausIdManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JoinHouseholdActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseDatabase db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseDatabase.getInstance(DB_URL);
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
        DatabaseReference mitgliederRef = db.getReference().child("Hauser").child(hausId).child("mitgliederIds").child(userId);
        mitgliederRef.setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                HausIdManager.getInstance().setHausId(hausId);
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
