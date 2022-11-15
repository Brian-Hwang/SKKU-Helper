package edu.skku.skkuhelper.roomdb;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

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
	public Date date;
	public String link;
	public int check; //0:존재 1:최신 2:기한마감

}

