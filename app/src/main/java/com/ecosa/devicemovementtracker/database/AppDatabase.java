package com.ecosa.devicemovementtracker.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;



@Database(entities = {Coordinate.class}, version = 1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase{
    private static AppDatabase appDatabase;

    public abstract DaoAccess daoAccess();
    private Context context;
    public static AppDatabase getInstance(Context context){
         String DB_NAME = "db_coordinate";
        if(appDatabase == null){
            appDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return appDatabase;
    }

    public static void destroyInstance() {
        appDatabase = null;
    }
}
