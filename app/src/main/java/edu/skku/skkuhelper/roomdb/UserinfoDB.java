package edu.skku.skkuhelper.roomdb;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;


@Database(entities = {Userinfo.class}, version = 1)

public abstract class UserinfoDB extends RoomDatabase {
    private static UserinfoDB database = null;

    public abstract UserInfoDao UserinfoDao();


    public static UserinfoDB getInstance(Context context) {
        if (database == null) {
            database = Room.databaseBuilder(context.getApplicationContext(),
                            UserinfoDB.class, "SKKUassignment.db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();

        }
        return database;
    }

    public static void destroyInstance() {
        database = null;
    }
}