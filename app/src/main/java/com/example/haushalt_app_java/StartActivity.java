package com.example.haushalt_app_java;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.Toast;

import com.example.haushalt_app_java.notification.NotificationService;
import com.google.firebase.database.FirebaseDatabase;


public class StartActivity extends AppCompatActivity {

    private Button register;
    private Button login;

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1002;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        register= findViewById(R.id.register);
        login = findViewById(R.id.login);

    register.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(StartActivity.this, RegisterActivity2.class));
            finish();
        }
    });
    login.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(StartActivity.this, LoginActivity2.class));

        }

    });

    requestNotificationPermission();

    }
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            } else {
                NotificationService.sendNotification(this, "Welcome!", "Thank you for using our app. Permission for Notifications has already been given.", 2);
            }
        } else {
            NotificationService.sendNotification(this, "Welcome!", "Thank you for using our app. Permission for Notifications has already been given.", 2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                NotificationService.sendNotification(this, "Welcome!", "Thank you for using our app.", 2);
            } else {
                Toast.makeText(this, "Notification permission is required to show notifications in the background.", Toast.LENGTH_LONG).show();
            }
        }
    }
}