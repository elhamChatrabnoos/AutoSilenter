package com.android.silent.autosilenter.DataBases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.android.silent.autosilenter.Daos.EventDao;
import com.android.silent.autosilenter.Models.EventModel;

@Database(version = 4, exportSchema = false, entities = {EventModel.class})
public abstract class Ddatabase extends RoomDatabase {
    public static Ddatabase database;
    public static Ddatabase getDatabase(Context context){
        if (database == null){
            database = Room.databaseBuilder(context.getApplicationContext(), Ddatabase.class, "db_1")
                    .allowMainThreadQueries().fallbackToDestructiveMigration().build();
        }

        return database;
    }

    public abstract EventDao getEventDao();

}
