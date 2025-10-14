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

public class RegisterActivity2 extends AppCompatActivity {

    private EditText eamil;
    private EditText password;
    private Button register;
    private FirebaseAuth auth;

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
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity2.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration success
                            Toast.makeText(RegisterActivity2.this, "Registration successful", Toast.LENGTH_SHORT).show();
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity2.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity2.this, StartActivity.class);
                                startActivity(intent);
                                finish(); // Optional: schließt die Registrierungsseite
                            }
                        } else {
                            // If registration fails, display a message to the user.
                            Toast.makeText(RegisterActivity2.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });


    }
}