package edu.skku.skkuhelper.roomdb;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {UserInfo.class}, version = 1)

public abstract class UserInfoDB extends RoomDatabase {
    private static UserInfoDB database = null;

    public abstract UserInfoDao UserinfoDao();


    public static UserInfoDB getInstance(Context context) {
        if (database == null) {
            database = Room.databaseBuilder(context.getApplicationContext(),
                            UserInfoDB.class, "userifo.db")
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