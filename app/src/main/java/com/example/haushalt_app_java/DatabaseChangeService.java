package com.example.haushalt_app_java;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DatabaseChangeService extends Service {

    private static final String TAG = "DatabaseChangeService";
    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private boolean isFirstDataChange = true;

    @Override
    public void onCreate() {
        super.onCreate();
        // For performance, it's better to listen to a more specific path than the root.
        databaseReference = FirebaseDatabase.getInstance(DB_URL).getReference("Benutzer");
        Log.d(TAG, "Service created.");
        setupValueEventListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started.");
        databaseReference.addValueEventListener(valueEventListener);
        return START_STICKY;
    }

    private void setupValueEventListener() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Database has changed.");

                if (isFirstDataChange) {
                    isFirstDataChange = false;
                    return;
                }

                NotificationService.sendNotification(
                        getApplicationContext(),
                        "Database Update",
                        "Your data has been updated in the background.",
                        6 // A new notification ID
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed.");
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
