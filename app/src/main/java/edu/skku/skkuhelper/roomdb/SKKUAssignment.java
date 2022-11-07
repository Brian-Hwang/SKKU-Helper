package edu.skku.skkuhelper.roomdb;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class SKKUAssignment {
    @PrimaryKey(autoGenerate = false)
    public long assignmentId;

    public String assignmentName;
    public String courseName;
    public long courseId;
    public boolean isLecture;
    public Date dueDate;
    public String url;
    public long isAlarm;
}
