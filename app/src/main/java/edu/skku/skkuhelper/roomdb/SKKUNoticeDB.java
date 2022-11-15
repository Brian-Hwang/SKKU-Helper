package edu.skku.skkuhelper.roomdb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {SKKUNotice.class}, version = 1)
@TypeConverters({SKKUNoticeConverters.class})

public abstract class SKKUNoticeDB extends RoomDatabase {
    private static SKKUNoticeDB database = null;
	//수정필요
    public abstract SKKUNoticeDao SKKUnoticeDao();


    public static SKKUNoticeDB getInstance(Context context) {
        if (database == null) {
            database = Room.databaseBuilder(context.getApplicationContext(),
                    SKKUNoticeDB.class, "SKKUNotice.db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();

        }
        return database;
    }
	public static void destroydatabase() {
        database = null;
    }
}
