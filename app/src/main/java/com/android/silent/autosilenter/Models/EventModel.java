package com.android.silent.autosilenter.Models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.kasra.picker.utils.PersianCalendar;

@Entity(tableName = "tbl_events")
public class EventModel implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;

    public EventModel() {

    }

    protected EventModel(Parcel in) {
        id = in.readLong();
        eventName = in.readString();
        active = in.readByte() != 0;
        silentMode = in.readByte() != 0;
        darkMode = in.readByte() != 0;
        disableWifi = in.readByte() != 0;
        disableNetwork = in.readByte() != 0;
        vibrateMode  = in.readByte() != 0;
        airPlaneMode = in.readByte() != 0;
        eventStartTime = in.readString();
        eventEndTime = in.readString();
        eventStartDate = in.readString();
        eventEndDate = in.readString();
        eventDays = in.readString();
    }

    public static final Creator<EventModel> CREATOR = new Creator<EventModel>() {
        @Override
        public EventModel createFromParcel(Parcel in) {
            return new EventModel(in);
        }

        @Override
        public EventModel[] newArray(int size) {
            return new EventModel[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getEventStartTime() {
        return eventStartTime;
    }

    public void setEventStartTime(String eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public String getEventEndTime() {
        return eventEndTime;
    }

    public void setEventEndTime(String eventEndTime) {
        this.eventEndTime = eventEndTime;
    }

    public String getEventStartDate() {
        return eventStartDate;
    }

    public void setEventStartDate(String eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    public String getEventEndDate() {
        return eventEndDate;
    }

    public void setEventEndDate(String eventEndDate) {
        this.eventEndDate = eventEndDate;
    }

    public String getEventDays() {
        return eventDays;
    }

    public void setEventDays(String eventDays) {
        this.eventDays = eventDays;
    }

    public boolean isSilentMode() {
        return silentMode;
    }

    public void setSilentMode(boolean silentMode) {
        this.silentMode = silentMode;
    }

    public boolean isDisableWifi() {
        return disableWifi;
    }

    public void setDisableWifi(boolean disableWifi) {
        this.disableWifi = disableWifi;
    }

    public boolean isDisableNetwork() {
        return disableNetwork;
    }

    public void setDisableNetwork(boolean disableNetwork) {
        this.disableNetwork = disableNetwork;
    }




    private String eventName = "";
    private boolean active = false;
    private boolean silentMode = false;
    private boolean darkMode = false;
    private boolean disableWifi = false;
    private boolean disableNetwork = false;
    private boolean vibrateMode = false;
    private boolean airPlaneMode = false;
    private String eventStartTime = "";
    private String eventEndTime = "";
    private String eventStartDate = "";
    private String eventEndDate = "";
    private String eventDays = "";

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public boolean isVibrateMode() {
        return vibrateMode;
    }

    public void setVibrateMode(boolean vibrateMode) {
        this.vibrateMode = vibrateMode;
    }

    public boolean isAirPlaneMode() {
        return airPlaneMode;
    }

    public void setAirPlaneMode(boolean airPlaneMode) {
        this.airPlaneMode = airPlaneMode;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(eventName);
        parcel.writeByte((byte) (active ? 1 : 0));
        parcel.writeByte((byte) (silentMode ? 1 : 0));
        parcel.writeByte((byte) (darkMode ? 1 : 0));
        parcel.writeByte((byte) (disableWifi ? 1 : 0));
        parcel.writeByte((byte) (disableNetwork ? 1 : 0));
        parcel.writeByte((byte) (vibrateMode ? 1 : 0));
        parcel.writeByte((byte) (airPlaneMode ? 1 : 0));
        parcel.writeString(eventStartTime);
        parcel.writeString(eventEndTime);
        parcel.writeString(eventStartDate);
        parcel.writeString(eventEndDate);
        parcel.writeString(eventDays);
    }
}
