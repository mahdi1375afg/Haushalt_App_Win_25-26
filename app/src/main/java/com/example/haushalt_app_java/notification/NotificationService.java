package com.example.haushalt_app_java.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.haushalt_app_java.R;

public class NotificationService {

    public static void sendGeneralNotification(Context context, String title, String message, int notificationId){
        sendNotification(context, title, message, ApplicationState.GENERAL_NOTIFICATIONS_CHANNEL_ID, notificationId, NotificationCompat.PRIORITY_DEFAULT);
    }

    public static void sendNotification(Context context, String title, String message, String channelId, int notificationId, int priority) {
        if (ApplicationState.isAppInForeground()) {
            // If the app is in the foreground, you might want to handle this differently.
            // For instance, you could show a Toast or an in-app banner instead of a notification.
            Toast.makeText(context, title + ": " + message, Toast.LENGTH_SHORT).show();
            return; // Don't proceed to create a system notification.
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(priority);

        notificationManager.notify(notificationId, builder.build());
    }

    public static Notification createNotification(Context context, String title, String message, String channelId, int priority) {
        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(priority)
                .build();
    }

}
