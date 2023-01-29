package com.android.silent.autosilenter.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.android.silent.autosilenter.MainActivity;
import com.android.silent.autosilenter.R;

public class CustomNotification {
    Context context;
    public CustomNotification(Context context) {
        this.context = context;
    }

    public Notification makeNotification(String notificationMsg, String CHANNEL_ID) {
        // run activity when clicked on notification
        Intent mainActivity = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,
                mainActivity, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.clock_icon)
                .setContentTitle(notificationMsg)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
        return notification;
    }


    public NotificationManager ReadyNotification(String CHANNEL_ID, String CHANNEL_NAME) {
        NotificationManager manager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        return manager;
    }
}
