package com.example.haushalt_app_java;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.haushalt_app_java.domain.Nutzer;

public class RegisterActivity2 extends AppCompatActivity {

    private EditText eamil;
    private EditText password;
    private Button register;
    private FirebaseAuth auth;
    private EditText userName;
    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        eamil = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        auth = FirebaseAuth.getInstance();
        userName = findViewById(R.id.userName);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_email = eamil.getText().toString();
                String text_password = password.getText().toString();

                if (TextUtils.isEmpty(text_email)||TextUtils.isEmpty(text_password)){
                    Toast.makeText(RegisterActivity2.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                } else if (text_password.length()<6) {
                    Toast.makeText(RegisterActivity2.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(text_email, text_password);
                    // Proceed with registration logic
                }
            }
        });


    }

        private void registerUser(String email, String password) {
            String name = userName.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Bitte Namen eingeben", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();

                            // DisplayName in Firebase Auth setzen
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // Benutzer in Realtime Database speichern
                                        String userId = user.getUid();

                                         DatabaseReference userRef = FirebaseDatabase.getInstance(DB_URL)
                                             .getReference().child("Benutzer").child(userId);

                                        Nutzer nutzer = new Nutzer(userId, name);

                                        userRef.setValue(nutzer)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(RegisterActivity2.this, "Registrierung erfolgreich!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(RegisterActivity2.this, StartActivity.class);
                                                startActivity(intent);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(RegisterActivity2.this, "Fehler beim Speichern: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                    }
                                });
                        } else {
                            Toast.makeText(RegisterActivity2.this, "Registrierung fehlgeschlagen: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
}