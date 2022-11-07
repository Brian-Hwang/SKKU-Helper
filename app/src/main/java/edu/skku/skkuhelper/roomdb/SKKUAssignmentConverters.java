package edu.skku.skkuhelper.roomdb;

import androidx.room.TypeConverter;

import java.util.Date;

public class SKKUAssignmentConverters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
