package edu.skku.skkuhelper.roomdb;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import java.util.Date;


@Entity
public class SKKUNotice {
	@PrimaryKey(autoGenerate = false)
	public long noticeId;
	public String title;
	public String sum;
	public int tag;
	public int watch;
	public String writer;
	public String date;
	public String link;
	public int check; //0:존재 1:최신 2:기한마감
	
	
	public String printts(){
		return "\"title\":"+title+","+"\"check\":"+check+"\"sum\":"+sum;
	}
	
	/*
	public void input(String[] inputdata){	
		this.title = title;
		this.sum = sum;
		this.writer = writer;
	}
	*/
	public void makezero() {
		this.check = 0;
	}
	public void makeone() {
		this.check = 1;
	}
	
	/*
	public Long Alarm(int check){
		return 123L;
	}
	*/
}
/*
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
*/


