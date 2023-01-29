package com.android.silent.autosilenter.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.silent.autosilenter.MainActivity;
import com.android.silent.autosilenter.R;

import java.util.Calendar;

public class QuickSilentService extends Service {

    public static final String CHANNEL_ID = "channel id";
    public static final String CHANNEL_NAME = "Silent_mode";
    private String notificationContent = "";
    private Calendar targetTime;
    private static boolean normalMode = false;
    private static boolean serviceOn = false;
    private static final int NOTIFICATION_ID = 100;
    private NotificationManager manager;
    private int silentDuration;
    private Thread checkTimeThread;
    private AudioManager audioManager;
    private PowerManager.WakeLock wakeLock;
    private boolean loopVar = true;

    public static boolean isNormalMode() {
        return normalMode;
    }
    public static void setNormalMode(boolean normalMode) {
        QuickSilentService.normalMode = normalMode;
    }
    public static boolean isServiceOn() {
        return serviceOn;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        silentDuration = intent.getIntExtra("inputDuration", 0);

        keepAwakeService();
        checkInputDuration();
        createNotification();
        startNotification();
        startThread();
        serviceOn = true;

        return START_REDELIVER_INTENT;
    }

    private void keepAwakeService() {
        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP, getPackageName() + ":Call");
        wakeLock.acquire();
    }

    private void startThread() {
        checkTimeThread = new Thread(() -> {
            while(loopVar){
                try {
                    checkSystemTimeToNormal();
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        checkTimeThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        loopVar = false;
        checkTimeThread.interrupt();
        stopForeground(true);
    }

    private void startNotification() {
        // run activity when notification clicked
        Intent mainActivity = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,
                mainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
        // show notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(notificationContent)
                .setSmallIcon(R.drawable.clock_icon)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotification() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID , CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        else{

        }
    }

    private void checkInputDuration() {
        targetTime = Calendar.getInstance();
        // set target time
        switch (silentDuration){
            case 0 :
                targetTime.add(Calendar.MINUTE, 30 );
                notificationContent = getString(R.string.silent_mode_for) + getString(R.string.first_item_txt) + getString(R.string.active_verb);
                break;
            case 1:
                targetTime.add(Calendar.HOUR, 1);
                notificationContent = getString(R.string.silent_mode_for) + getString(R.string.second_item_txt) + getString(R.string.active_verb);
                break;
            case 2:
                targetTime.add(Calendar.HOUR, 2);
                notificationContent = getString(R.string.silent_mode_for) + getString(R.string.third_item_txt) + getString(R.string.active_verb);
                break;
            case 3:
                notificationContent = getString(R.string.silent_mode_for) + getString(R.string.fourth_item_txt) + getString(R.string.active_verb);
                targetTime.add(Calendar.HOUR, 3);
                break;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkSystemTimeToNormal() {
        Calendar currentTime = Calendar.getInstance();
        String stringCurrentTime = currentTime.get(Calendar.HOUR) + ":" + currentTime.get(Calendar.MINUTE);
        String stringTargetTime = targetTime.get(Calendar.HOUR) + ":" + targetTime.get(Calendar.MINUTE);

        // change system mode to normal
        if(stringCurrentTime.equals(stringTargetTime)){
            // get system mode
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            normalMode = true;
            endService();
        }
    }

    public void endService() {
        wakeLock.release();
        stopForeground(true);
        Intent intent = new Intent(QuickSilentService.this, QuickSilentService.class);
        stopService(intent);
    }

}
