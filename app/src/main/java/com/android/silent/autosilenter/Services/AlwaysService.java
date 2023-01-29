package com.android.silent.autosilenter.Services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.android.silent.autosilenter.R;

public class AlwaysService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CustomNotification customNotification = new CustomNotification(getApplicationContext());
        customNotification.ReadyNotification("2", "always");
        Notification notification = customNotification.makeNotification(getString(R.string.permanent_notification_msg), "2");
        startForeground(3, notification);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
