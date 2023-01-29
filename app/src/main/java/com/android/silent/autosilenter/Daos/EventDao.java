package com.android.silent.autosilenter.Daos;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.android.silent.autosilenter.Models.EventModel;

import java.util.List;

@Dao
public interface EventDao {
    @Insert
    long insertEvent(EventModel eventModel);

    @Delete
    int deleteEvent(EventModel eventModel);

    @Update
    int updateEvent(EventModel eventModel);

    @Query("SELECT * FROM tbl_events")
    List<EventModel> returnEvents();

    @Query("DELETE FROM tbl_events")
    void deleteAllEvents();

    @Query("SELECT * FROM tbl_events WHERE eventName LIKE :eventName ")
    List<EventModel> returnSearchedEvent(String eventName);


}
