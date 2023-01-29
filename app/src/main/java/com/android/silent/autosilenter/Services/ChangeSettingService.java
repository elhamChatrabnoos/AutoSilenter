package com.android.silent.autosilenter.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.MacAddress;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PatternMatcher;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.silent.autosilenter.Activities.EditEventActivity;
import com.android.silent.autosilenter.Daos.EventDao;
import com.android.silent.autosilenter.DataBases.Ddatabase;
import com.android.silent.autosilenter.MainActivity;
import com.android.silent.autosilenter.Models.EventModel;
import com.android.silent.autosilenter.R;
import com.kasra.picker.utils.PersianCalendar;

import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ChangeSettingService extends Service {
    public static final String CHANNEL_ID = "channel_id";
    public static final String CHANNEL_NAME = "Channel_name";
    public static final int notificationID = 2;
    private boolean silentModeActive = false;
    private boolean vibrateModeActive = false;
    private boolean wifiDisableActive = false;
    public static String notificationMsg;
    private int[] daysSelectedNumber = new int[8];
    private boolean daysSelected = false;
    private int currentDay;
//    private EventModel eventModel;
    private EventDao eventDao;
    private List<EventModel> eventModelList;
    private boolean timeSelected;
    private DaysReceiver daysReceiver;
    private boolean timeRegistered = false;
    private boolean dateSelected;
    private boolean eventChange;
    private NotificationManager manager;
    private Notification notification;
    private boolean okDate;
    private CustomNotification customNotification;
    Context context;
    private PowerManager.WakeLock wakeLock;
    private Thread timeChangeThread;
    private boolean loopVar;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getCurrentDay();
        keepAwakeService();

        notificationMsg = getApplicationContext().getString(R.string.specialTime_notif_msg);
        eventDao = Ddatabase.getDatabase(this).getEventDao();
        eventModelList = eventDao.returnEvents();
        Arrays.fill(daysSelectedNumber, -1);

        // create custom notification
        customNotification = new CustomNotification(getApplicationContext());
        customNotification.ReadyNotification(CHANNEL_ID, CHANNEL_NAME);
        notification = customNotification.makeNotification(notificationMsg, CHANNEL_ID);
        manager = customNotification.ReadyNotification(CHANNEL_ID, CHANNEL_NAME);

        startNotification(notificationMsg);
        checkSelectedDays();

        if (daysSelected || dateSelected) {
            registerDaysReceiver();
        }
        else {
            loopVar = true;
            startTimeChangeThread();
        }

        context = getApplicationContext();
        return START_STICKY;
    }

    private void startTimeChangeThread() {
        Thread thread = new Thread(() -> {
            while(loopVar){
                try {
                    eventModelList = eventDao.returnEvents();
                    for (int i = 0; i < eventModelList.size(); i++){
                        EventModel eventModel = eventModelList.get(i);
                        timeRegistered = true;
                        checkTimeToDoSetting(eventModel);
                        // to update UI in activity and adapter
                        new Handler(Looper.getMainLooper()).post(() ->
                                checkTimeToNormal(eventModel));
                    }
                    Thread.sleep(60000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void registerDaysReceiver() {
        daysReceiver = new DaysReceiver();
        // to start the days receiver when service start
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(daysReceiver, filter);
    }

    private void checkSelectedDays() {
        daysSelected = false;
        dateSelected = false;
        timeSelected = false;

        // search days of week in each event model's days
        eventModelList = eventDao.returnEvents();
        for (int i = 0; i < eventModelList.size(); i++) {
            // if just days selected for event
            if (eventModelList.get(i).getEventStartDate().equals("")
                    && !eventModelList.get(i).getEventDays().equals("")
                    && eventModelList.get(i).isActive()) {
                daysSelected = true;
            }
            // if just time selected for event
            if (eventModelList.get(i).getEventDays().equals("")
                    && eventModelList.get(i).isActive()) {
                timeSelected = true;
            }
            // if date selected for event
            if (!eventModelList.get(i).getEventStartDate().equals("")
                    && eventModelList.get(i).isActive()
                    && (getCurrentDate().before(getDateOfFa(eventModelList.get(i).getEventEndDate())))){
                dateSelected = true;
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (daysSelected || dateSelected) {
            daysSelected = false;
            dateSelected = false;
            unregisterReceiver(daysReceiver);
        }
        wakeLock.release();
        loopVar = false;
        stopForeground(true);
    }

    private void updateNotification(String notificationMsg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notification = customNotification.makeNotification(notificationMsg, CHANNEL_ID);
            manager.notify(notificationID, notification);
        }
        else{
            Intent mainActivity = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,0,
                    mainActivity, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.clock_icon)
                    .setContentTitle(notificationMsg)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(notificationID, notification);
        }
    }

    private void startNotification(String notificationMsg) {
        customNotification.makeNotification(notificationMsg, CHANNEL_ID);
        startForeground(notificationID, notification);
    }

    public class DaysReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            searchOnList();
        }

        private void searchOnList() {
            eventModelList = eventDao.returnEvents();
            for (int i = 0; i < eventModelList.size(); i++) {
                EventModel em = eventModelList.get(i);
                // when days selected but date no
                if (!em.getEventDays().equals("")
                        && em.isActive() && em.getEventStartDate().equals("")) {
                    checkEventDays(em);
                }
                // when days and date selected
                if ( !em.getEventStartDate().equals("") && em.isActive()) {
                    okDate = false;
                    // check date was between range of input dates
                    if ((getCurrentDate().getPersianShortDate().equals(em.getEventStartDate())
                            || getCurrentDate().after(getDateOfFa(em.getEventStartDate())))
                            && (getCurrentDate().getPersianShortDate().equals(em.getEventEndDate())
                            || getCurrentDate().before(getDateOfFa(em.getEventEndDate())))) {
                        okDate = true;
                        checkEventDays(em);
                    }
                }
            }
        }
    }

    private void getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        currentDay = calendar.get(Calendar.DAY_OF_WEEK);
    }

    private void checkEventDays(EventModel event) {
        eventChange = false;
        if(event.getEventDays().contains(EditEventActivity.everyDayTxt)){
            checkAndRegister(event);
        }
       else{
            // get event days like a string and check if days is equal current day
            String[] days = event.getEventDays().split(" ");
            for (int j = 0; j < days.length; j++) {
                if (!days[j].equals("")) {
                    daysSelectedNumber[j] = j ;
                    getCurrentDay();
                    if (currentDay == daysSelectedNumber[j]) {
                        checkAndRegister(event);
                    }
                }
            }
        }
        // when end date is day after normal day
        if (!silentModeActive && !vibrateModeActive && dateSelected && getCurrentDate().getPersianShortDate().equals(event.getEventEndDate())){
            disableEvent(event);
            checkSelectedDays();
            if (!daysSelected && !timeSelected){
                onDestroy();
                timeRegistered = false;
            }
        }
    }

    private void checkAndRegister(EventModel event) {
        if (!event.getEventStartDate().equals("")){
            // check if current date is between target date
            if (okDate){
//                eventModel = event;
                eventChange = true;
                if (!timeRegistered){
                    loopVar = true;
                    startTimeChangeThread();
                }
            }
        }
        else {
//            eventModel = event;
            eventChange = true;
            if (!timeRegistered){
                loopVar = true;
                startTimeChangeThread();
            }
        }
    }

    // to change string type of start date to calendar type
    public static PersianCalendar getDateOfFa(String date) {
        String[] d = date.split("/");
        int year = Integer.parseInt(d[0]);
        int month = Integer.parseInt(d[1]) - 1;
        int day = Integer.parseInt(d[2]);
        PersianCalendar calendar = new PersianCalendar();
        calendar.setPersianDate(year, month, day);
        return calendar;
    }


    public static PersianCalendar getCurrentDate() {
        PersianCalendar calendar = new PersianCalendar();
        return calendar;
    }

    private void checkTimeToNormal(EventModel eventModel) {
            String endTime = eventModel.getEventEndTime();
            // change number format
            if (!EditEventActivity.getCurrentLanguage().equals(EditEventActivity.ENGLISH)) {
                endTime = changeNumberFormat(eventModel.getEventEndTime());
            }
            // activate normal mode if there is not other active event
            if (endTime.equals(getCurrentTime()) && eventModel.isActive()) {
                activateNormalMode();
                // if days is null, disable event
                if (eventModel.getEventDays().equals("")
                        || (!eventModel.getEventStartDate().equals("")
                        && getCurrentDate().getPersianShortDate().equals(eventModel.getEventEndDate()))){
                    disableEvent(eventModel);
                }
                if (timeRegistered){
                    timeRegistered = false;
                }
                // check if there is another event to silent
                checkSelectedDays();
                // off service when there is no active event
                if (!daysSelected && !timeSelected && !dateSelected) {
                    onDestroy();
                }
                // change notification for next active event
                else {
                    updateNotification(notificationMsg);
                    if (timeSelected){
                        loopVar = true;
                        startTimeChangeThread();
                    }
                }
            }
    }

    private void disableEvent(EventModel eventModel) {
        eventModel.setActive(false);
        eventDao.updateEvent(eventModel);
        MainActivity.eventAdapter.updateRecycler(eventModel);
    }

    private void activateNormalMode() {
        if (silentModeActive || vibrateModeActive){
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            silentModeActive = false;
            vibrateModeActive = false;
        }

        if (wifiDisableActive){
            wifiDisableActive = false;
            changeWifiMode(true);
        }

    }

    private void changeWifiMode(boolean enable) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                wifiManager.setWifiEnabled(enable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
             WifiNetworkSpecifier.Builder specifier = new WifiNetworkSpecifier.Builder();
             specifier.setSsidPattern(new PatternMatcher("test", PatternMatcher.PATTERN_PREFIX));
             specifier.setBssidPattern(MacAddress.fromString("10:03:23:00:00:00"),
                                                    MacAddress.fromString("ff:ff:ff:00:00:00"));
            specifier.setSsid("abcdefgh");
            specifier.setWpa2Passphrase("1234567890");
            specifier.build();

            WifiNetworkSpecifier wifiNetworkSpecifier = specifier.build();
             //
             NetworkRequest.Builder request = new NetworkRequest.Builder();
            request.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
            request.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            request.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);
            request.addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED);
            request.setNetworkSpecifier(wifiNetworkSpecifier);
            request.build();
            //
            NetworkRequest networkRequest = request.build();

            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
             ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    Log.d("6060", "available");
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    Log.d("6060", "unavailable");
                }
            };
            connectivityManager.requestNetwork(networkRequest, networkCallback);
//            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
            Log.d("6060", "method");
        }
    }


    // control data internet
    private void changeDataNetMode(String s) {
        try {
            String[] cmds = {s};
            Process p = EditEventActivity.permission;
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            for (String tmpCmd : cmds) {
                os.writeBytes(tmpCmd + "\n");
            }
            os.writeBytes("exit\n");
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private EventModel checkTimeToDoSetting(EventModel em) {
        if (em.getEventDays().equals("") && em.isActive()) {
            activateSetting(em);
        }
        // if days or date selected for event
        else if (em.isActive()){
            checkEventDays(em);
            if (eventChange){
                activateSetting(em);
            }
        }
        return em;
    }

    private void activateSetting(EventModel eventModel) {
        // change number format of start time in persian app
        String startTime = eventModel.getEventStartTime();
        if (!EditEventActivity.getCurrentLanguage().equals(EditEventActivity.ENGLISH)) {
            startTime = changeNumberFormat(eventModel.getEventStartTime());
        }
        if (startTime.equals(getCurrentTime()) && eventModel.isActive()) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            String endTime = eventModel.getEventEndTime();
            if (eventModel.isSilentMode()){
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                silentModeActive = true;
            }
            else if (eventModel.isVibrateMode()){
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                vibrateModeActive = true;
            }

            if (eventModel.isDisableWifi()){
                wifiDisableActive = true;
                changeWifiMode(false);
            }
            updateNotification(getString(R.string.setting_target_msg) + " " + endTime + getString(R.string.beActiveSetting));
        }
    }


    public static String changeNumberFormat(String str) {
        return str.replace("0" , "۰")
                .replace("1", "۱")
                .replace("2", "۲")
                .replace("3", "۳")
                .replace("4", "۴")
                .replace("5", "۵")
                .replace("6", "۶")
                .replace("7", "۷")
                .replace("8", "۸")
                .replace("9", "۹");
    }

    public String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE) ;
        return String.format("%02d:%02d", currentHour, currentMinute);
    }

    private void keepAwakeService() {
        // cpu work also when phone is on sleep mode
        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if(Build.VERSION.SDK_INT >= 23) {
            wakeLock = powerManager.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, ":TAG");
        } else {
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, ":TAG");
        }
        wakeLock.acquire();
    }
}
