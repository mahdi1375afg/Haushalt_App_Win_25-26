package com.example.haushalt_app_java.notification;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.haushalt_app_java.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DatabaseChangeService extends Service {

    private static final String TAG = "DatabaseChangeService";
    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app/";
    private static final int FOREGROUND_SERVICE_ID = 1;
    private DatabaseReference haushaltReference;
    private ValueEventListener valueEventListener;
    private boolean isFirstDataChange = true;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();
        mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "Service created.");
        setupValueEventListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Log.d(TAG, "DatabaseChangeService started.");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user found. Stopping service.");
            stopSelf();
            return START_NOT_STICKY;
        }

        Notification notification = NotificationService.createNotification(
                getApplicationContext(),
                "Benachrichtigungen aktiviert",
                "Diese Benachrichtigung nicht schließen. Wenn Sie die Benachrichtigung schließen werden Sie nicht mehr zuverlässig über Änderungen in ihrem Haushalt benachrichtigt.",
                ApplicationState.FOREGROUND_SERVICE_CHANNEL_ID,
                NotificationCompat.PRIORITY_MIN
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            startForeground(FOREGROUND_SERVICE_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(FOREGROUND_SERVICE_ID, notification);
        }

        listenToHouseholdChanges(currentUser.getUid());

        return START_STICKY;
    }

    private void listenToHouseholdChanges(String userId) {
        DatabaseReference userHausIdRef = FirebaseDatabase.getInstance(DB_URL).getReference("Benutzer").child(userId).child("hausId");
        userHausIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String hausId = snapshot.getValue(String.class);
                if (hausId != null && !hausId.isEmpty()) {
                    if (haushaltReference != null) {
                        haushaltReference.removeEventListener(valueEventListener);
                    }
                    haushaltReference = FirebaseDatabase.getInstance(DB_URL).getReference("Hauser").child(hausId);
                    isFirstDataChange = true; // Reset for the new listener
                    haushaltReference.addValueEventListener(valueEventListener);
                    Log.d(TAG, "Now listening to changes for hausId: " + hausId);
                } else {
                    Log.w(TAG, "User " + userId + " has no hausId. Stopping service.");
                    stopSelf();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to get hausId for user " + userId, error.toException());
                stopSelf();
            }
        });
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

                NotificationService.sendGeneralNotification(
                        getApplicationContext(),
                        "Änderungen im Haushalt",
                        "Es gab Änderungen bei den Daten in ihrem Haushalt.",
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
        if (haushaltReference != null && valueEventListener != null) {
            haushaltReference.removeEventListener(valueEventListener);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
