package edu.skku.skkuhelper.roomdb;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;


@Entity
public class SKKUNotice {
    @PrimaryKey(autoGenerate = false)
	public long noticeid;
	
	@ColumnInfo(name="title")
    public string title;
	
	@ColumnInfo(name="sum")
    public String sum;
	
	@ColumnInfo(name="tag")
    public int tag;
	
	@ColumnInfo(name="watch")
    public int watch;
	
	@ColumnInfo(name="writer")
    public string writer;
	
	@ColumnInfo(name="ddate")
    public string ddate;
	
	@ColumnInfo(name="link")
    public String link;
	

	
    //public long isAlarm;
}


