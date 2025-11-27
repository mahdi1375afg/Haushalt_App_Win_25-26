package com.example.haushalt_app_java.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.haushalt_app_java.R;

public class NotificationService {

    public static void sendNotification(Context context, String title, String message, int notificationId) {
        if (ApplicationState.isAppInForeground()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ApplicationState.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(notificationId, builder.build());
    }
}
