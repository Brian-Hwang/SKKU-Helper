package edu.skku.skkuhelper;

import java.util.Date;



public class Noticedata {
	public String title = "";
	public String sum = "";
	public int tag = 0;
	public int watch = 0;
	public String writer = "";
	public String date = "";
	public String link = "";
	public int check = 1; //0:존재 1:최신 2:기한마감
	
	Testdata (String title, String sum, int tag, int watch, String wrtier, String date, String link){
		this.title = title;
		this.sum = sum;
		this.tag = tag;
		this.watch = watch;
		this.writer = writer;
		this.date = date;
		this.link = link;
	}
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