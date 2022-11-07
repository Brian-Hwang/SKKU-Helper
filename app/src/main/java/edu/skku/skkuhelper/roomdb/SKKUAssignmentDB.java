package edu.skku.skkuhelper.roomdb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {SKKUAssignment.class}, version = 1)
@TypeConverters({SKKUAssignmentConverters.class})

public abstract class SKKUAssignmentDB extends RoomDatabase {
    private static SKKUAssignmentDB database = null;

    public abstract SKKUAssignmentDao SKKUassignmentDao();


    public static SKKUAssignmentDB getInstance(Context context) {
        if (database == null) {
            database = Room.databaseBuilder(context.getApplicationContext(),
                    SKKUAssignmentDB.class, "SKKUassignment.db")
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
