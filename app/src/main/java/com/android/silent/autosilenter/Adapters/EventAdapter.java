package com.android.silent.autosilenter.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.silent.autosilenter.Activities.EditEventActivity;
import com.android.silent.autosilenter.Daos.EventDao;
import com.android.silent.autosilenter.DataBases.Ddatabase;
import com.android.silent.autosilenter.Models.EventModel;
import com.android.silent.autosilenter.R;
import com.android.silent.autosilenter.Services.ChangeSettingService;
import com.android.silent.autosilenter.databinding.EventsRecyclerLayoutBinding;

import java.util.Calendar;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    List<EventModel> eventModelList ;
    Context context;
    private EventDao eventDao;
    private final ForeignOperation foreignOperation;
    public static final String EVENT_MODEL = "event";
    public static final String ITEM_CLICKED = "clickedItem";

    public EventAdapter(List<EventModel> eventModelList, Context context) {
        this.eventModelList = eventModelList;
        this.context = context;
        eventDao = Ddatabase.database.getEventDao();
        foreignOperation = (ForeignOperation) context;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EventViewHolder(EventsRecyclerLayoutBinding.inflate(LayoutInflater
                .from(context), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.binding.reEventName.setText(eventModelList.get(position).getEventName());
        holder.binding.reStartTime.setText(context.getString(R.string.from_hour) + "  " + eventModelList.get(position).getEventStartTime() );
        holder.binding.reEndTime.setText(context.getString(R.string.till_hour) + "  " + eventModelList.get(position).getEventEndTime());
        holder.binding.reEventSwitcher.setChecked(eventModelList.get(position).isActive());
        setDays(holder, position);
        changeLanguageView(holder, position);
        setDate(holder, position);
    }

    private void setDate(EventViewHolder holder, int position) {
        // change date to gregorian if language app is english and date selected in persian form
        String startDate = eventModelList.get(position).getEventStartDate();
        String endDate = eventModelList.get(position).getEventEndDate();

        if (EditEventActivity.getCurrentLanguage().equals(EditEventActivity.ENGLISH) && startDate.contains("140")){
            Log.d("2020", "setDate: ");
            holder.binding.reStartDate.setText(context.getString(R.string.from_date) + "  " + EditEventActivity.convertPersianDateToGrg(startDate));
            holder.binding.reEndDate.setText(context.getString(R.string.to_date) + "  " + EditEventActivity.convertPersianDateToGrg(endDate));
        }
        // show dates textview just when dates not null
        else if (eventModelList.get(position).getEventStartDate().equals("")){
            holder.binding.thirdLayout.removeView(holder.binding.reStartDate);
            holder.binding.forthLayout.removeView(holder.binding.reEndDate);
        }
        else{
            Log.d("2020", "else date: " + eventModelList.get(position).getEventStartDate());
            holder.binding.reStartDate.setText(context.getString(R.string.from_date) + "  " + eventModelList.get(position).getEventStartDate());
            holder.binding.reEndDate.setText(context.getString(R.string.to_date) + "  " + eventModelList.get(position).getEventEndDate());
        }
    }

    private void changeLanguageView(EventViewHolder holder, int position) {
        if (!EditEventActivity.getCurrentLanguage().equals(EditEventActivity.ENGLISH)){
            // change language of event name to persian if it has default name
            if (eventModelList.get(position).getEventName().equals("Event")){
                holder.binding.reEventName.setText(context.getString(R.string.event_name_default_text));
            }
            // change show format of days to persian
            if (!eventModelList.get(position).getEventDays().equals("")){
                holder.binding.reDaysOfWeek.setText(context.getString(R.string.days_txt) + " " + changeDaysFormat(eventModelList.get(position).getEventDays()));
            }
            // if language system is persian show numbers in persian format
            Typeface typeface = ResourcesCompat.getFont(context, R.font.vazir_regular);
            holder.binding.reStartDate.setTypeface(typeface);
            holder.binding.reEndDate.setTypeface(typeface);
            holder.binding.reStartTime.setTypeface(typeface);
            holder.binding.reEndTime.setTypeface(typeface);
            holder.binding.reDaysOfWeek.setTypeface(typeface);
            holder.binding.reEventName.setTypeface(typeface);
        }

        if (EditEventActivity.getCurrentLanguage().equals(EditEventActivity.ENGLISH)) {
            // change language of event name to English if it has default name
            if (eventModelList.get(position).getEventName().equals("رویداد")) {
                holder.binding.reEventName.setText(context.getString(R.string.event_name_default_text));
            }
        }
    }

    private void setDays(EventViewHolder holder, int position) {
        // show days textview just when days not null
        if (eventModelList.get(position).getEventDays().equals("")){
            assert holder.binding.daysLayout != null;
            holder.binding.daysLayout.removeView(holder.binding.reDaysOfWeek);
        }
        else{
            holder.binding.reDaysOfWeek.setText(context.getString(R.string.days_txt) + "  " + eventModelList.get(position).getEventDays());
        }
    }

    private String changeDaysFormat(String targetString) {
        return targetString
                .replace(EditEventActivity.weekDaysName[0] , context.getString(R.string.sunday))
                .replace(EditEventActivity.weekDaysName[1] , context.getString(R.string.monday))
                .replace(EditEventActivity.weekDaysName[2] , context.getString(R.string.tuesday))
                .replace(EditEventActivity.weekDaysName[3] , context.getString(R.string.wednesday))
                .replace(EditEventActivity.weekDaysName[4] , context.getString(R.string.thursday))
                .replace(EditEventActivity.weekDaysName[5] , context.getString(R.string.friday))
                .replace(EditEventActivity.weekDaysName[6] , context.getString(R.string.saturday))
                .replace(EditEventActivity.everyDayTxt, context.getString(R.string.everyDay_txt));
    }

    @Override
    public int getItemCount() {
        return eventModelList.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {

        EventsRecyclerLayoutBinding binding;

        public EventViewHolder(EventsRecyclerLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            doIfItemClicked();
        }

        public void doIfItemClicked() {
            binding.deleteImg.setOnClickListener(view -> {
                // if event is active and delete btn clicked stop service
                if (eventModelList.get(getLayoutPosition()).isActive()) {
                    eventModelList.get(getLayoutPosition()).setActive(false);
                    if (anyActiveEvent(true)){
                        foreignOperation.stopService();
                    }
                }
                eventDao.deleteEvent(eventModelList.get(getLayoutPosition()));
                eventModelList.remove(getLayoutPosition());
                notifyItemRemoved(getLayoutPosition());
            });

            // when switcher clicked control event on or off
            binding.reEventSwitcher.setOnClickListener(view -> {
                EventModel event = eventModelList.get(getLayoutPosition());
                if (event.isActive()) {
                    event.setActive(false);
                    updateRecycler(event);
                    if (anyActiveEvent(true)){
                        foreignOperation.stopService();
                    }
                }
                else {
                    if (!event.getEventStartDate().equals("")){
                        if (ChangeSettingService.getCurrentDate()
                                .before(ChangeSettingService.getDateOfFa(event.getEventEndDate()))){
                            enableEvent(event);
                        }
                        else{
                            binding.reEventSwitcher.setEnabled(false);
                            binding.reEventSwitcher.setChecked(false);
                        }
                    }
                    else if (event.getEventDays().equals("")) {
                        if (checkTimeIsOk(event)) {
                            enableEvent(event);
                        }
                        else{
                            // events switcher become disable when its time passed
                             binding.reEventSwitcher.setChecked(false);
                             binding.reEventSwitcher.setEnabled(false);
                        }
                    }
                    else if (!event.getEventDays().equals("")){
                        enableEvent(event);
                    }
                }
            });

            // when each event clicked
            itemView.setOnClickListener(view -> {
                Intent eventActivity = new Intent(context, EditEventActivity.class);
                eventActivity.putExtra(EVENT_MODEL, eventModelList.get(getLayoutPosition()));
                eventActivity.putExtra(ITEM_CLICKED, true);
                foreignOperation.editItem(eventModelList.get(getLayoutPosition()));
                context.startActivity(eventActivity);
            });
        }

        // check current time is before target time to do work
        private boolean checkTimeIsOk(EventModel event) {
            boolean timeIsOk = false;
            // separate minute and hour of system time
            int currentHour = getHourMinuteOf(getCurrentTime(), 0);
            int currentMinute = getHourMinuteOf(getCurrentTime(), 1);
            int startHour = getHourMinuteOf(event.getEventStartTime(), 0);
            int startMinute = getHourMinuteOf(event.getEventStartTime(), 1);
            if (currentHour < startHour || (currentHour == startHour && currentMinute < startMinute)){
                timeIsOk = true;
            }
            return timeIsOk;
        }


        private void enableEvent(EventModel eventModel) {
            if (anyActiveEvent(false)){
                foreignOperation.stopService();
            }
            else{
                eventModel.setActive(true);
                updateRecycler(eventModel);
                Intent silentModeIntent = new Intent(context, ChangeSettingService.class);
                ContextCompat.startForegroundService(context, silentModeIntent);
            }
        }
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        return String.format(context.getString(R.string.time_format), currentHour, currentMinute);
    }

    // change a string to hour and minute
    public static int getHourMinuteOf(String time, int hourOrMinute){
        String[] hourMin = time.split(":");
        if (hourOrMinute == 0){
            return Integer.parseInt(hourMin[0]);
        }
        else{
            return Integer.parseInt(hourMin[1]);
        }
    }

    // search to find active event
    private boolean anyActiveEvent(boolean eventStatus) {
        boolean continueService = false;
        boolean stopService = false;
        for (int i = 0; i < eventModelList.size(); i++) {
            if (eventModelList.get(i).isActive()) {
                continueService = true;
                break;
            }
        }
        if (!continueService && eventStatus) {
            stopService = true;
        }
        return stopService;
    }


    public void updateRecycler(EventModel eventModel) {
        eventDao = Ddatabase.getDatabase(context).getEventDao();
        for (int i = 0; i < eventModelList.size(); i++) {
            if (eventModelList.get(i).getId() == eventModel.getId()) {
                eventModelList.set(i, eventModel);
                eventDao.updateEvent(eventModel);
                notifyItemChanged(i);
            }
        }
    }

    public interface ForeignOperation {
        void stopService();
        void editItem(EventModel eventModel);
    }

}
