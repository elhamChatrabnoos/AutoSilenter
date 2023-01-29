package com.android.silent.autosilenter.Activities;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.android.silent.autosilenter.Adapters.EventAdapter;
import com.android.silent.autosilenter.Daos.EventDao;
import com.android.silent.autosilenter.DataBases.Ddatabase;
import com.android.silent.autosilenter.MainActivity;
import com.android.silent.autosilenter.Models.EventModel;
import com.android.silent.autosilenter.R;
import com.android.silent.autosilenter.Services.ChangeSettingService;
import com.android.silent.autosilenter.databinding.ActivityEventBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.kasra.picker.customviews.DateRangeCalendarView;
import com.kasra.picker.dialog.DatePickerDialog;
import com.kasra.picker.utils.PersianCalendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import saman.zamani.persiandate.PersianDate;

public class EditEventActivity extends AppCompatActivity implements View.OnClickListener, EventAdapter.ForeignOperation {
    public static final String ENGLISH = "English";
    private ActivityEventBinding binding;
    private int selectedHour;
    private int selectedMinute;
    private boolean startTimeSelect = false, endTimeSelect = false;
    private int currentHour = 0;
    private int currentMinute = 0;
    private int startHour = -1, startMinute = -1;
    private String endTime = "";
    private String selectedTime;
    private final int number = 8;
    private final boolean[] daysOfWeek = new boolean[number];
    private boolean everyDay;
    public static final String everyDayTxt = "EveryDay";
    private boolean daysSelected = false;
    private EventModel eventModel;
    private String[] days;
    private EventDao eventDao;
    private boolean startDateSelected;
    private boolean endDateSelected;
    private PersianCalendar inputStartDate;
    private PersianCalendar inputEndDate;
    private boolean dateSelected = false;
    private TextView btnSave;
    private ImageView btnBack;
    private int settingSelected = 0;
    public static Process permission;
    private boolean updateItem = false;
    private PersianCalendar currentDate;
    private Calendar inputStartDateEn;
    private Calendar inputEndDateEn;
    private Calendar currentDateEn;
    public static String[] weekDaysName = {"sunday", "monday",
            "tuesday", "wednesday", "thursday",
            "friday", "saturday"};

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Arrays.fill(daysOfWeek, false);
        getEventInfoToUpdate();
        initializeDatabaseVar();
        makeViewsClickable();
        deleteWifiSetting();
    }

    private void deleteWifiSetting() {
        // if android version was above android 10 wifi setting don't show
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.settingLayout.removeView(binding.wifiNetworkTxt);
        }
    }

    private void makeViewsClickable() {
        btnSave = findViewById(R.id.btn_title);
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        binding.btnStartTime.setOnClickListener(this);
        binding.btnEndTime.setOnClickListener(this);
        binding.startDate.setOnClickListener(this);
        binding.endDate.setOnClickListener(this);
        binding.everyDay.setOnClickListener(this);
        binding.saturday.setOnClickListener(this);
        binding.sunday.setOnClickListener(this);
        binding.monday.setOnClickListener(this);
        binding.tuesday.setOnClickListener(this);
        binding.wednesday.setOnClickListener(this);
        binding.thursday.setOnClickListener(this);
        binding.friday.setOnClickListener(this);
        binding.silentTxt.setOnClickListener(this);
        binding.vibrateTxt.setOnClickListener(this);
        binding.wifiNetworkTxt.setOnClickListener(this);
        binding.closeIconEnd.setOnClickListener(this);
        binding.closeIconStart.setOnClickListener(this);
    }

    private void getEventInfoToUpdate() {
        // get information of item that user wants to edit
        eventModel = getIntent().getParcelableExtra(EventAdapter.EVENT_MODEL);
        updateItem = getIntent().getBooleanExtra(EventAdapter.ITEM_CLICKED, false);
        if (updateItem) {
            setField();
            settingOfSelectedItem();
            checkDayOfSelectedItem();
        }
    }

    private void setField() {
        binding.eventName.setText(eventModel.getEventName());
        setTimeToTextView();
        setDateFieldsToTextView();
        changeNumberFormat();
        fillInputDateField();
        // check the checkbox items
        binding.silentTxt.setChecked(eventModel.isSilentMode());
        binding.vibrateTxt.setChecked(eventModel.isVibrateMode());
        binding.wifiNetworkTxt.setChecked(eventModel.isDisableWifi());
    }

    private void setTimeToTextView() {
        // separate hour and minute from time field
        int minute = EventAdapter.getHourMinuteOf(eventModel.getEventStartTime(), 1);
        int hour = EventAdapter.getHourMinuteOf(eventModel.getEventStartTime(), 0);
        startTimeSelect = true;
        setStartEndBtnText(hour, minute);
        binding.btnStartTime.setText(eventModel.getEventStartTime());
        binding.btnEndTime.setText(eventModel.getEventEndTime());
        endTime = eventModel.getEventEndTime();
    }

    private void changeNumberFormat() {
        // if system language is persian edittext format change to persian
        if (!getCurrentLanguage().equals(ENGLISH)) {
            changeNumberToPersian(binding.btnStartTime);
            changeNumberToPersian(binding.btnEndTime);
            changeNumberToPersian(binding.startDate);
            changeNumberToPersian(binding.endDate);
        }
    }

    private void setDateFieldsToTextView() {
        binding.startDate.setText(eventModel.getEventStartDate());
        binding.endDate.setText(eventModel.getEventEndDate());
        // if app language is english and selected date is persian empty field of date
        if (getCurrentLanguage().equals(ENGLISH) && binding.startDate.getText().toString().contains("140")) {
            binding.startDate.setText(convertPersianDateToGrg(eventModel.getEventStartDate()));
            binding.endDate.setText(convertPersianDateToGrg(eventModel.getEventEndDate()));
        }
        // convert date to persian format if app language is persian and selected date is gregorian
        if (!getCurrentLanguage().equals(ENGLISH) && binding.startDate.getText().toString().contains("202")) {
            Calendar startC = convertStringToCalendar(binding.startDate.getText().toString(), getString(R.string.date_format));
            binding.startDate.setText(convertCalendarToPersian(startC));
            Calendar endC = convertStringToCalendar(binding.endDate.getText().toString(), getString(R.string.date_format));
            binding.endDate.setText(convertCalendarToPersian(endC));
        }
    }

    public static String convertPersianDateToGrg(String date) {
        // if app language is english and date selected in persian before, change it to gregorian
        PersianCalendar persianDate1 = convertStringToPersianDate(date, "yyyy/MM/dd");
        PersianDate persianDate2 = new PersianDate();
        persianDate2.setShYear(persianDate1.getPersianYear());
        persianDate2.setShMonth(persianDate1.getPersianMonth());
        persianDate2.setShDay(persianDate1.getPersianDay());

        return persianDate2.getGrgYear() + "/" + (persianDate2.getGrgMonth() + 1) + "/" + persianDate2.getGrgDay();

    }

    private void fillInputDateField() {
        // convert dates into calendar type
        if (getCurrentLanguage().equals(ENGLISH)) {
            inputStartDateEn = convertStringToCalendar(eventModel.getEventStartDate(), getString(R.string.date_format));
            inputEndDateEn = convertStringToCalendar(eventModel.getEventEndDate(), getString(R.string.date_format));
        } else {
            inputStartDate = convertStringToPersianDate(eventModel.getEventStartDate(), getString(R.string.date_format));
            inputEndDate = convertStringToPersianDate(eventModel.getEventEndDate(), getString(R.string.date_format));
        }
    }

    private void checkDayOfSelectedItem() {
        // if event days id everyday select all days when clicked to update
        if (eventModel.getEventDays().contains(everyDayTxt)) {
            binding.everyDay.setChecked(true);
            checkEveryDay(true);
        } else {
            // check which day selected
            String days = eventModel.getEventDays();
            if (days.contains(weekDaysName[0])) {
                binding.sunday.setChecked(true);
                setDaysChecked(true, 1);
            }
            if (days.contains(weekDaysName[1])) {
                binding.monday.setChecked(true);
                setDaysChecked(true, 2);
            }
            if (days.contains(weekDaysName[2])) {
                binding.tuesday.setChecked(true);
                setDaysChecked(true, 3);
            }
            if (days.contains(weekDaysName[3])) {
                binding.wednesday.setChecked(true);
                setDaysChecked(true, 4);
            }
            if (days.contains(weekDaysName[4])) {
                binding.thursday.setChecked(true);
                setDaysChecked(true, 5);
            }
            if (days.contains(weekDaysName[5])) {
                binding.friday.setChecked(true);
                setDaysChecked(true, 6);
            }
            if (days.contains(weekDaysName[6])) {
                binding.saturday.setChecked(true);
                setDaysChecked(true, 7);
            }

        }

    }

    private void settingOfSelectedItem() {
        // check which setting selected to apply the event
        if (eventModel.isDisableNetwork()) {
            settingSelected++;
        }
        if (eventModel.isDisableWifi()) {
            settingSelected++;
        }
        if (eventModel.isSilentMode()) {
            settingSelected++;
        }
        if (eventModel.isVibrateMode()) {
            settingSelected++;
        }
        if (eventModel.isAirPlaneMode()) {
            settingSelected++;
        }
    }

    // when every item clicked on screen
    @Override
    public void onClick(View view) {
        if (view == binding.eventName) {
            MainActivity.sendLogToFirebase("EventName", "eventName", "EditText");
        }

        if (view == binding.btnStartTime) {
            MainActivity.sendLogToFirebase("StartTimeBtn", "startTimeBtn", "EditText");
            startTimeSelect = true;
            showTimePickerDialog();
        }

        if (view == btnBack) {
            finish();
            Intent intent = new Intent(EditEventActivity.this, MainActivity.class);
            startActivity(intent);
        }

        if (view == binding.btnEndTime) {
            MainActivity.sendLogToFirebase("EndTimeBtn", "endTimeBtn", "EditText");
            if (startHour > -1 && startMinute > -1) {
                endTimeSelect = true;
                startTimeSelect = false;
                showTimePickerDialog();
            } else {
                Toast.makeText(EditEventActivity.this, R.string.startTime_error, Toast.LENGTH_SHORT).show();
            }
        }

        if (binding.everyDay == view) {
            MainActivity.sendLogToFirebase("EveryDayBox", "everyDayBox", "CheckBox");

            if (!binding.everyDay.isChecked()) {
                checkEveryDay(false);
            } else {
                checkEveryDay(true);
            }
        }

        if (view == binding.saturday) {
            setDaysChecked(binding.saturday.isChecked(), 7);
        }

        if (view == binding.sunday) {
            setDaysChecked(binding.sunday.isChecked(), 1);
        }

        if (view == binding.monday) {
            setDaysChecked(binding.monday.isChecked(), 2);
        }

        if (view == binding.tuesday) {
            setDaysChecked(binding.tuesday.isChecked(), 3);
        }

        if (view == binding.wednesday) {
            setDaysChecked(binding.wednesday.isChecked(), 4);
        }

        if (view == binding.thursday) {
            setDaysChecked(binding.thursday.isChecked(), 5);
        }

        if (view == binding.friday) {
            setDaysChecked(binding.friday.isChecked(), 6);
        }

        if (view == binding.startDate) {
            MainActivity.sendLogToFirebase("StartDateBtn", "startDateBtn", "EditText");
            startDateSelected = true;
            endDateSelected = false;
            showDatePickerDialog();
        }
        if (view == binding.endDate) {
            // we want send s.th to firebase when end date edittext clicked
            MainActivity.sendLogToFirebase("EndDateBtn", "endDateBtn", "EditText");
            endDateSelected = true;
            startDateSelected = false;
            // replace current date to start date if end date selected before start date field
            if (binding.startDate.getText().toString().equals("")) {
                if (!getCurrentLanguage().equals(ENGLISH)) {
                    binding.startDate.setText(getCurrentDate());
                    changeNumberToPersian(binding.startDate);
                    inputStartDate = currentDate;
                } else {
                    binding.startDate.setText(getCurrentEnglishDate());
                    inputStartDateEn = currentDateEn;
                }

            }
            showDatePickerDialog();
        }

        if (view == btnSave) {
            checkNullField();
        }
        if (view == binding.silentTxt) {
            MainActivity.sendLogToFirebase("SilentSwitcher", "silentSwitcher", "Switcher");
            // when vibrate mode clicked change value of silent mode
            if (binding.vibrateTxt.isChecked()) {
                binding.vibrateTxt.setChecked(false);
                eventModel.setVibrateMode(false);
                settingSelected -= 1;
            }
            if (binding.silentTxt.isChecked()) {
                eventModel.setSilentMode(true);
                settingSelected += 1;
            } else {
                eventModel.setSilentMode(false);
                settingSelected -= 1;
            }
        }

        if (view == binding.vibrateTxt) {
            MainActivity.sendLogToFirebase("VibrateSwitcher", "vibrateSwitcher", "Switcher");
            if (binding.silentTxt.isChecked()) {
                binding.silentTxt.setChecked(false);
                eventModel.setSilentMode(false);
                settingSelected -= 1;
            }
            if (binding.vibrateTxt.isChecked()) {
                eventModel.setVibrateMode(true);
                settingSelected += 1;
            } else {
                eventModel.setVibrateMode(false);
                settingSelected -= 1;
            }
        }

        if (view == binding.wifiNetworkTxt) {
            if (binding.wifiNetworkTxt.isChecked()) {
                eventModel.setDisableWifi(true);
                settingSelected += 1;
            } else {
                eventModel.setDisableWifi(false);
                settingSelected -= 1;
            }
        }

        if (view == binding.closeIconStart) {
            binding.startDate.setText("");
            startDateSelected = false;
        }

        if (view == binding.closeIconEnd) {
            binding.endDate.setText("");
            endDateSelected = false;
        }
    }

    private String getCurrentEnglishDate() {
        currentDateEn = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format));
        return dateFormat.format(currentDateEn.getTime());
    }

    private void checkNullField() {
        if (!binding.eventName.getText().toString().equals("")) {
            if (!binding.btnStartTime.getText().toString().equals("")) {
                if (!binding.btnEndTime.getText().toString().equals("")) {
                    if (checkTimeAfterCurrent(startHour, startMinute)) {
                        if (settingSelected > 0) {
                            if (!binding.startDate.getText().toString().equals("")) {
                                if (!binding.endDate.getText().toString().equals("")) {
                                    if (isAnyDaySelected()) {
                                        dateSelected = true;
                                        saveAndBack();
                                    } else {
                                        Toast.makeText(this, R.string.select_day_error, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(this, R.string.define_end_date_msg, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                saveAndBack();
                            }
                        } else {
                            Toast.makeText(this, R.string.select_setting_error, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, R.string.startTime_error2, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(EditEventActivity.this,
                            R.string.fill_endTime_msg, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EditEventActivity.this,
                        R.string.fill_startTime_msg, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(EditEventActivity.this,
                    R.string.fill_eventName_msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAndBack() {
        // check if any event is active
        if (activationService() < 1) {
            startSilentService();
        }
        saveData();
        emptyField();
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // if service is not active activate it
    private int activationService() {
        int numberActive = 0;
        eventDao = Ddatabase.getDatabase(this).getEventDao();
        List<EventModel> eventModelList = eventDao.returnEvents();
        for (int i = 0; i < eventModelList.size(); i++) {
            if (eventModelList.get(i).isActive()) {
                numberActive += 1;
            }
        }
        return numberActive;
    }

    private void saveData() {
        eventModel.setEventName(binding.eventName.getText().toString());
        // save numbers as english format in database
        eventModel.setEventStartTime(changeNumberFormat(binding.btnStartTime.getText().toString()));
        eventModel.setEventEndTime(changeNumberFormat(binding.btnEndTime.getText().toString()));
        eventModel.setActive(true);
        setDaysSelected();
        if (dateSelected) {
            if (getCurrentLanguage().equals(ENGLISH)) {
                eventModel.setEventStartDate(convertCalendarToPersian(inputStartDateEn));
                eventModel.setEventEndDate(convertCalendarToPersian(inputEndDateEn));
            } else {
                eventModel.setEventStartDate(binding.startDate.getText().toString());
                eventModel.setEventEndDate(binding.endDate.getText().toString());
            }
        } else {
            eventModel.setEventStartDate("");
            eventModel.setEventEndDate("");
        }
        if (updateItem) {
            eventDao.updateEvent(eventModel);
        } else {
            eventDao.insertEvent(eventModel);
        }
    }

    private String convertCalendarToPersian(Calendar calendarDate) {
        PersianCalendar persianCalendar = new PersianCalendar();
        Date date = calendarDate.getTime();
        persianCalendar.setTime(date);
        return persianCalendar.getPersianShortDate();
    }


    public static String changeNumberFormat(String str) {
        return str.replace("۰", "0")
                .replace("۱", "1")
                .replace("۲", "2")
                .replace("۳", "3")
                .replace("۴", "4")
                .replace("۵", "5")
                .replace("۶", "6")
                .replace("۷", "7")
                .replace("۸", "8")
                .replace("۹", "9");
    }

    // separated days which selected
    private void setDaysSelected() {
        isAnyDaySelected();
        if (daysSelected) {
            days = new String[number];
            for (int i = 0; i < daysOfWeek.length; i++) {
                if (daysOfWeek[i]) {
                    getDay(i);
                }
            }
            if (everyDay) {
                eventModel.setEventDays(everyDayTxt);
            } else {
                // to use the days in service make it like simple string
                eventModel.setEventDays(Arrays.toString(days)
                        .replace("[", "")
                        .replace("]", "")
                        .replace(" ", "")
                        .replace("null", "")
                        .replace(",", " "));
            }
        } else {
            eventModel.setEventDays("");
        }
    }

    private void getDay(int i) {
        // save days in english format into database
        switch (i) {
            case 1:
                days[1] = weekDaysName[0];
                break;
            case 2:
                days[2] = weekDaysName[1];
                break;
            case 3:
                days[3] = weekDaysName[2];
                break;
            case 4:
                days[4] = weekDaysName[3];
                break;
            case 5:
                days[5] = weekDaysName[4];
                break;
            case 6:
                days[6] = weekDaysName[5];
                break;
            case 7:
                days[7] = weekDaysName[6];
                break;
        }
    }

    private void initializeDatabaseVar() {
        Ddatabase ddatabase = Ddatabase.getDatabase(this);
        eventDao = ddatabase.getEventDao();
        if (!updateItem) {
            eventModel = new EventModel();
        }
    }

    private boolean isAnyDaySelected() {
        daysSelected = false;
        for (boolean b : daysOfWeek) {
            if (b) {
                daysSelected = true;
                break;
            }
        }
        return daysSelected;
    }

    // when every day clicked all day checked or reverse
    private void checkEveryDay(boolean checked) {
        Arrays.fill(daysOfWeek, checked);
        checkedAllDays(checked);
        everyDay = checked;
    }

    // check if any days selected
    private void setDaysChecked(boolean isChecked, int index) {
        if (isChecked) {
            daysOfWeek[index] = true;
        } else {
            daysOfWeek[index] = false;
        }
        boolean allDaysSelected = true;
        for (int i = 1; i < daysOfWeek.length; i++) {
            if (!daysOfWeek[i]) {
                allDaysSelected = false;
            }
        }
        // if all days selected manipulate every day box checked
        if (allDaysSelected) {
            binding.everyDay.setChecked(true);
            everyDay = true;
        } else {
            // when days select every day checked false
            if (binding.everyDay.isChecked()) {
                binding.everyDay.setChecked(false);
                everyDay = false;
            }
        }
    }

    private void checkedAllDays(boolean checked) {
        binding.saturday.setChecked(checked);
        binding.sunday.setChecked(checked);
        binding.monday.setChecked(checked);
        binding.tuesday.setChecked(checked);
        binding.wednesday.setChecked(checked);
        binding.thursday.setChecked(checked);
        binding.friday.setChecked(checked);
    }

    private void startSilentService() {
        Intent silentModeIntent = new Intent(getApplicationContext(), ChangeSettingService.class);
        ContextCompat.startForegroundService(getApplicationContext(), silentModeIntent);
    }

    private void emptyField() {
        binding.btnStartTime.setText(null);
        binding.btnEndTime.setText(null);
        binding.eventName.setText(null);
        binding.startDate.setText(null);
        binding.endDate.setText(null);
        checkedAllDays(false);
    }

    public static String getCurrentLanguage() {
        return Locale.getDefault().getDisplayLanguage();
    }

    private void showDatePickerDialog() {
        // if system language is english show gregorian datePicker
        if (getCurrentLanguage().equals("English")) {
            MaterialDatePicker<Long> datePickerEn = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("select date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            datePickerEn.addOnPositiveButtonClickListener(selection -> {
                // set selected date to calendar object
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selection);
                calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                if (startDateSelected) {
                    inputStartDateEn = calendar;
                    checkDateAfterCurrent();
                }
                if (endDateSelected) {
                    inputEndDateEn = calendar;
                    checkDateAfterStartedDate();
                }
            });
            datePickerEn.show(getSupportFragmentManager(), "tag");
        }
        // if system language is persian show persian datePicker
        else {
            DatePickerDialog datePickerDialog = new DatePickerDialog(EditEventActivity.this);
            datePickerDialog.setSelectionMode(DateRangeCalendarView.SelectionMode.Single,
                    DateRangeCalendarView.HolidayMode.Enable);
            datePickerDialog.setCanceledOnTouchOutside(true);
            datePickerDialog.setTextSizeDate(15);
            datePickerDialog.setOnSingleDateSelectedListener(date -> {
                if (startDateSelected) {
                    inputStartDate = date;
                    checkDateAfterCurrent();
                }
                if (endDateSelected) {
                    inputEndDate = date;
                    checkDateAfterStartedDate();
                }
            });
            datePickerDialog.setShowGregorianDate(true);
            datePickerDialog.setSelectedDateCircleColor(Color.rgb(80, 25, 115));
            datePickerDialog.setAcceptButtonColor(Color.rgb(80, 25, 115));
            datePickerDialog.showDialog();
        }
    }

    private String convertDateToString(Calendar date) {
        return DateFormat.format("yyy/MM/dd", date).toString();
    }

    private void checkDateAfterStartedDate() {
        // if selected start date is persian date
        if (inputStartDate != null) {
            if (inputEndDate.after(inputStartDate)
                    || inputEndDate.getPersianShortDate().equals(inputStartDate.getPersianShortDate())) {
                binding.endDate.setText(inputEndDate.getPersianShortDate());
                changeNumberToPersian(binding.endDate);
                Log.d("3030", "1: ");
            } else {
                Toast.makeText(this, R.string.end_date_error_msg, Toast.LENGTH_SHORT).show();
            }
        } else {
            // if selected start date is english date
            if (inputEndDateEn.after(inputStartDateEn)
                    || convertDateToString(inputEndDateEn).equals(convertDateToString(inputStartDateEn))) {
                binding.endDate.setText(convertDateToString(inputEndDateEn));
            } else {
                Toast.makeText(this, R.string.end_date_error_msg, Toast.LENGTH_SHORT).show();
            }
        }
        endDateSelected = false;
    }

    private void checkDateAfterCurrent() {
        // calculate persian calendar
        if (inputStartDate != null) {
            PersianCalendar currentDate = new PersianCalendar();
            if (inputStartDate.after(currentDate) ||
                    inputStartDate.getPersianShortDate().equals(currentDate.getPersianShortDate())) {
                binding.startDate.setText(inputStartDate.getPersianShortDate());
                changeNumberToPersian(binding.startDate);
            } else {
                Toast.makeText(this, R.string.date_input_error_msg, Toast.LENGTH_SHORT).show();
            }
            // clear endDate filed when start date selected after endDate
            if (!inputStartDate.before(inputEndDate) && !inputStartDate.equals(inputEndDate)) {
                binding.endDate.setText(null);
            }
        }
        // calculate english date
        else {
            Calendar currentDate = Calendar.getInstance();
            if (inputStartDateEn.after(currentDate) ||
                    convertDateToString(inputStartDateEn).equals(convertDateToString(currentDate))) {
                binding.startDate.setText(convertDateToString(inputStartDateEn));
            } else {
                Toast.makeText(this, R.string.date_input_error_msg, Toast.LENGTH_SHORT).show();
            }
            // clear endDate filed when start date selected after endDate
            if (!inputStartDateEn.before(inputEndDateEn) && !inputStartDateEn.equals(inputEndDateEn)) {
                binding.endDate.setText(null);
            }
        }
        startDateSelected = false;
    }

    private void changeNumberToPersian(EditText editText) {
        Typeface typeface = ResourcesCompat.getFont(this, R.font.vazir_regular);
        editText.setTypeface(typeface);
    }

    public String getCurrentDate() {
        currentDate = new PersianCalendar();
        return currentDate.getPersianShortDate();
    }

    private void showTimePickerDialog() {
        getCurrentTime();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timePicker, i, i1) -> {
            // selected time should be after current time
            checkTimeAfterCurrent(i, i1);
            if (startTimeSelect) {
                setStartEndBtnText(i, i1);
                if (!endTime.equals("") && !checkTimeBeforeEndTime(i, i1)) {
                    binding.btnEndTime.setText(null);
                }
            }

            if (endTimeSelect) {
                setStartEndBtnText(i, i1);
            }
        }, currentHour, currentMinute, true);
        timePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_btn_save), timePickerDialog);
        timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dialog_btn_cancel), timePickerDialog);
        timePickerDialog.create();
        timePickerDialog.show();
    }

    private boolean checkTimeBeforeEndTime(int hour, int minute) {
        boolean correctTime = false;
        int endHour = EventAdapter.getHourMinuteOf(endTime, 0);
        int endMinute = EventAdapter.getHourMinuteOf(endTime, 1);
        if (hour < endHour || (hour == endHour && minute < endMinute)) {
            correctTime = true;
        }
        return correctTime;
    }

    private boolean checkTimeAfterCurrent(int i, int i1) {
        isAnyDaySelected();
        boolean correctTime = false;
        getCurrentTime();
        if (startTimeSelect) {
            if (i > currentHour || (i == currentHour && i1 > currentMinute)
                    || daysSelected) {
                setStartEndBtnText(i, i1);
                correctTime = true;
            }
        }
        return correctTime;
    }

    private void setStartEndBtnText(int hour, int minute) {
        selectedHour = hour;
        selectedMinute = minute;
        selectedTime = String.format(getString(R.string.time_format), hour, minute);
        if (startTimeSelect) {
            setStartTime();
        }
        if (endTimeSelect) {
            if (selectedHour > startHour || (selectedHour == startHour && selectedMinute > startMinute)) {
                binding.btnEndTime.setText(selectedTime);
                endTime = selectedTime;
            } else {
                Toast.makeText(EditEventActivity.this, R.string.endTime_error, Toast.LENGTH_SHORT).show();
            }
            endTimeSelect = false;
            startTimeSelect = true;
        }
    }

    private void setStartTime() {
        startHour = selectedHour;
        startMinute = selectedMinute;
        binding.btnStartTime.setText(selectedTime);
    }

    private void getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        currentMinute = calendar.get(Calendar.MINUTE);
    }

    public static Calendar convertStringToCalendar(String targetDate, String format) {
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        try {
            Date date = dateFormat.parse(targetDate);
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e) {
            return calendar;
        }
    }


    public static PersianCalendar convertStringToPersianDate(String targetDate, String format) {
        // convert string to persian date
        Calendar calendar = convertStringToCalendar(targetDate, format);
        PersianCalendar persianCalendar = new PersianCalendar();
        persianCalendar.setPersianDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        return persianCalendar;
    }


    @Override
    public void stopService() {

    }

    @Override
    public void editItem(EventModel eventModel) {
        binding.eventName.setText(getIntent().getStringExtra("eventName"));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}