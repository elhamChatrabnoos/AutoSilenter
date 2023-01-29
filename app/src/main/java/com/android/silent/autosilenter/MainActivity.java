package com.android.silent.autosilenter;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.silent.autosilenter.Activities.AboutAppActivity;
import com.android.silent.autosilenter.Activities.EditEventActivity;
import com.android.silent.autosilenter.Adapters.EventAdapter;
import com.android.silent.autosilenter.Daos.EventDao;
import com.android.silent.autosilenter.DataBases.Ddatabase;
import com.android.silent.autosilenter.Dialogs.LanguageSetDialog;
import com.android.silent.autosilenter.Dialogs.NotificationSetDialog;
import com.android.silent.autosilenter.Dialogs.SilentDurationDialog;
import com.android.silent.autosilenter.Models.EventModel;
import com.android.silent.autosilenter.Services.ChangeSettingService;
import com.android.silent.autosilenter.Services.QuickSilentService;
import com.android.silent.autosilenter.databinding.ActivityMainBinding;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

public class MainActivity extends AppCompatActivity implements
        SilentDurationDialog.InputListener,
        EventAdapter.ForeignOperation {

    private ActivityMainBinding binding;
    public static final String QUICK_MODES = "quickModes";
    public static final String SWITCHER_MODE = "switcherMode";
    public static final String SILENT_DURATION = "silentDuration";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor sharedPrefEditor;
    private AudioManager audioManager;
    private int inputDuration = 0;
    private Intent normalModeService;
    public static EventAdapter eventAdapter;
    private Intent silentModeService;
    private EventDao eventDao;
    public static FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // for disable night mode for app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // turn off battery optimization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ignoreBatteryOptimization();
        }

        setLanguage();
        showDataInRecycler();
        defineIntentServices();
        getPermission();
        getPhoneMode();
        defineQuickSilentSwitcher();
        binding.bottomView.setBackground(null);
        clickedButtons();

        // set firebase
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }


    public static void sendLogToFirebase(String itemId, String itemName, String itemType) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, itemType);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void ignoreBatteryOptimization() {
        Intent intent = new Intent();
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        }
    }

    private void defineIntentServices() {
        normalModeService = new Intent(this, QuickSilentService.class);
        silentModeService = new Intent(this, ChangeSettingService.class);
    }

    private void setLanguage() {
        // to change language depend on which user select in app setting
        LanguageSetDialog.sharedPreferences = getSharedPreferences(LanguageSetDialog.LANGUAGE_PREF, Context.MODE_PRIVATE);
        LanguageSetDialog.editor = LanguageSetDialog.sharedPreferences.edit();

        if (LanguageSetDialog.sharedPreferences.getBoolean(LanguageSetDialog.ENGLISH_LAN, true)) {
            LanguageSetDialog.changeLanguage("en", getBaseContext());
            updateViewsResource();
        } else if (LanguageSetDialog.sharedPreferences.getBoolean(LanguageSetDialog.PERSIAN_LAN, true)) {
            LanguageSetDialog.changeLanguage("fa", getBaseContext());
            updateViewsResource();
        }
    }

    private void updateViewsResource() {
        // when apps language change update resource of views
        binding.bottomView.getMenu().clear();
        binding.bottomView.inflateMenu(R.menu.bottom_menu);
        binding.txtQuickSilent.setText(R.string.quickSilent_txt);
        binding.setNotification.setText(R.string.setting_notify);
        binding.setAboutApp.setText(R.string.setting_aboutApp);
        binding.setLanguage.setText(R.string.setting_language);
        binding.helpTitle1.setText(R.string.help_title1);
        binding.subTitle1.setText(R.string.subText_title1);
        binding.helpTitle2.setText(R.string.help_title2);
        binding.subTitle2.setText(R.string.sub_title2);
        binding.helpTitle3.setText(R.string.help_title3);
        binding.subTitle3.setText(R.string.sub_title3);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        finish();
    }

    public List<EventModel> getDataFromDatabase() {
        eventDao = Ddatabase.getDatabase(this).getEventDao();
        return eventDao.returnEvents();
    }

    private void showDataInRecycler() {
        // add data to recyclerView
        eventAdapter = new EventAdapter(getDataFromDatabase(), this);
        binding.eventRecycler.setAdapter(eventAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2,
                RecyclerView.VERTICAL, false);
        binding.eventRecycler.setLayoutManager(layoutManager);
    }

    private void clickedButtons() {
        binding.bottomView.setOnItemSelectedListener(item -> {
            if (item.getTitle().equals(getString(R.string.home_txt))) {
                sendLogToFirebase("HomeBtn", "homeBtn", "image");
                setVisibilityLayouts(View.VISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
                binding.mainLayout.setBackgroundResource(R.drawable.back11);
            }
            if (item.getTitle().equals(getString(R.string.share_txt))) {
                sendLogToFirebase("SettingBtn", "settingButton", "image");
                shareAppDialog();
            }
            if (item.getTitle().equals(getString(R.string.guide_txt))) {
                sendLogToFirebase("GuideBtn", "guidBtn", "image");
                setVisibilityLayouts(View.INVISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                binding.mainLayout.setBackgroundResource(R.drawable.back_light2);
            }
            if (item.getTitle().equals(getString(R.string.setting_txt))) {
                sendLogToFirebase("SettingBtn", "settingButton", "image");
                setVisibilityLayouts(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
                binding.mainLayout.setBackgroundResource(R.drawable.back11);
            }
            return true;
        });

        binding.setLanguage.setOnClickListener(view -> {
            LanguageSetDialog languageSetDialog = new LanguageSetDialog();
            languageSetDialog.show(getSupportFragmentManager(), "tag");
        });

        binding.setNotification.setOnClickListener(view -> {
            NotificationSetDialog notificationDialog = new NotificationSetDialog();
            notificationDialog.show(getSupportFragmentManager(), "tag");
        });

        binding.btnAdd.setOnClickListener(view -> {
            sendLogToFirebase("AddButton", "addButton", "FAB");
            startEventActivity();
        });

        binding.setAboutApp.setOnClickListener(view -> {
            Intent intent = new Intent(this, AboutAppActivity.class);
            startActivity(intent);
        });
    }

    private void setVisibilityLayouts(int home1, int home2, int help, int setting) {
        binding.quickSilentLayout.setVisibility(home1);
        binding.recyclerLayout.setVisibility(home2);
        binding.helpLayout.setVisibility(help);
        binding.settingLayout.setVisibility(setting);
    }


    private void startEventActivity() {
        Intent eventActivity = new Intent(MainActivity.this, EditEventActivity.class);
        startActivity(eventActivity);
    }

    // get star to app
    private void rateAppDialog() {
        Intent rate = new Intent(Intent.ACTION_EDIT);
        rate.setData(Uri.parse("bazaar://details?id=" + "com.android.silent.autosilenter"));
        rate.setPackage("com.android.silent.autosilenter");
        rate.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(rate);
    }


    private void shareAppDialog() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, R.string.custom_app_name);
        String shareMsg = getString(R.string.recommend_useApp_txt);
        shareMsg = shareMsg + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
        share.putExtra(Intent.EXTRA_TEXT, shareMsg);
        startActivity(Intent.createChooser(share, getString(R.string.share_title)));
    }

    public void defineQuickSilentSwitcher() {
        sharedPreferences = this.getSharedPreferences(QUICK_MODES, Context.MODE_MULTI_PROCESS);
        sharedPrefEditor = sharedPreferences.edit();
        boolean switcherMode = sharedPreferences.getBoolean(SWITCHER_MODE, false);

        // show time in the textBox
//        String silentDuration = sharedPreferences.getString(SILENT_DURATION, "");
        if (switcherMode) {
            binding.quickOn.setChecked(true);
//            binding.silentDurationTxt.setText(silentDuration);
        }

        // normalMode become true when silent mode finish
        if (QuickSilentService.isNormalMode() || !QuickSilentService.isServiceOn()) {
            binding.quickOn.setChecked(false);
            sharedPrefEditor.putBoolean(SWITCHER_MODE, false);
            sharedPrefEditor.commit();
        }
        // when quick on switcher clicked
        binding.quickOn.setOnClickListener(view -> {
            sendLogToFirebase("switcherQuickSilent", "switcherQuickSilent", "switcher");

            if (binding.quickOn.isChecked()) {
                sharedPrefEditor.putBoolean(SWITCHER_MODE, true);
                getSilentDuration();
            } else {
                sharedPrefEditor.putBoolean(SWITCHER_MODE, false);
                activateNormalMode();
                QuickSilentService.setNormalMode(false);
                stopService(normalModeService);
            }
            sharedPrefEditor.commit();
        });
    }

    private void activateNormalMode() {
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    private void silentPhone() {
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        startNormalService();
    }

    private void startNormalService() {
        Intent normalModeService = new Intent(this, QuickSilentService.class);
        normalModeService.putExtra("inputDuration", inputDuration);
        ContextCompat.startForegroundService(this, normalModeService);
    }

    private void getSilentDuration() {
        // get silent duration from dialog
        SilentDurationDialog silentDurationDialog = new SilentDurationDialog();
        silentDurationDialog.show(getSupportFragmentManager(), null);
    }

    private void getPhoneMode() {
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    }

    private void getPermission() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !nm.isNotificationPolicyAccessGranted()) {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
        }
    }

    @Override
    public void getInput(int itemDuration) {
        inputDuration = itemDuration;
        switch (itemDuration) {
            case 0:
//                binding.silentDurationTxt.setText(getString(R.string.first_item_txt));
                sharedPrefEditor.putString(SILENT_DURATION, getString(R.string.first_item_txt));
                break;
            case 1:
//                binding.silentDurationTxt.setText(getString(R.string.second_item_txt));
                sharedPrefEditor.putString(SILENT_DURATION, getString(R.string.second_item_txt));
                break;
            case 2:
//                binding.silentDurationTxt.setText(getString(R.string.third_item_txt));
                sharedPrefEditor.putString(SILENT_DURATION, getString(R.string.third_item_txt));
                break;
            case 3:
//                binding.silentDurationTxt.setText(getString(R.string.fourth_item_txt));
                sharedPrefEditor.putString(SILENT_DURATION, getString(R.string.fourth_item_txt));
                break;
        }
        sharedPrefEditor.commit();
        String silentDuration = sharedPreferences.getString(SILENT_DURATION, getString(R.string.fourth_item_txt));
        Toast.makeText(this, getString(R.string.silent_mode_for) + silentDuration + getString(R.string.active_verb), Toast.LENGTH_SHORT).show();
        silentPhone();
    }

    @Override
    public void stopService() {
        stopService(silentModeService);
    }

    @Override
    public void editItem(EventModel eventModel) {

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        List<EventModel> eventList = eventDao.returnEvents();
        boolean activeEvent = false;
        for (int i = 0; i < eventList.size(); i++) {
            if (eventList.get(i).isActive()) {
                activeEvent = true;
            }
        }
        if (!activeEvent) {
            stopService(silentModeService);
        }
    }

}