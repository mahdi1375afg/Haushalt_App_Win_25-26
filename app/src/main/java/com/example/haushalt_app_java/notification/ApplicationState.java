package com.example.haushalt_app_java.notification;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ApplicationState extends Application implements Application.ActivityLifecycleCallbacks {


    // Channel for the persistent foreground service notification
    public static final String FOREGROUND_SERVICE_CHANNEL_ID = "DatabaseSyncChannel";

    // Channel for informational notifications
    public static final String GENERAL_NOTIFICATIONS_CHANNEL_ID = "GeneralNotificationChannel";
    private static boolean isAppInForeground = false;
    private int runningActivities = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            // 1. Configure the Foreground Service Channel
            NotificationChannel serviceChannel = new NotificationChannel(
                    FOREGROUND_SERVICE_CHANNEL_ID,
                    "Background-Sync", // "Background Sync"
                    NotificationManager.IMPORTANCE_MIN // Use MIN to be unobtrusive
            );
            serviceChannel.setDescription("Required for Background-Data-Synchronisation. Should be as unobtrusive as possible.");
            manager.createNotificationChannel(serviceChannel);


            // 2. Configure the General Notifications Channel
            NotificationChannel generalChannel = new NotificationChannel(
                    GENERAL_NOTIFICATIONS_CHANNEL_ID,
                    "General-Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT // Use DEFAULT to make them noticeable
            );
            generalChannel.setDescription("Channel for all standard nofications.");
            manager.createNotificationChannel(generalChannel);
        }
    }

    public static boolean isAppInForeground() {
        return isAppInForeground;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (++runningActivities == 1) {
            isAppInForeground = true;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {}

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (--runningActivities == 0) {
            isAppInForeground = false;
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {}
}
