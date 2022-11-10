package edu.skku.skkuhelper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.room.Room;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import edu.skku.skkuhelper.roomdb.SKKUAssignment;
import edu.skku.skkuhelper.roomdb.SKKUAssignmentDB;
import edu.skku.skkuhelper.roomdb.SKKUAssignmentDao;

class LA {
    public String subject;
    public String title;
    public long assignmentId;
    public Date dueDate;
    public boolean isLecture;
    public long isAlarm;
    public String url;
    public LA(String subject, String title, long assignmentId, Date dueDate, boolean isLecture, long isAlarm, String url){
        this.subject = subject;
        this.title = title;
        this.assignmentId = assignmentId;
        this.dueDate = dueDate;
        this.isLecture = isLecture;
        this.isAlarm = isAlarm;
        this.url = url;
    }
}

public class ListViewAdapter_LR extends BaseAdapter {
    private ArrayList<LA> items;
    private Context mContext;
    private List<SKKUAssignment> l;

    public ListViewAdapter_LR(ArrayList<LA> items, Context mContext, List<SKKUAssignment> l) {
        this.mContext = mContext;
        this.items = items;
        this.l = l;
    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.lecture_part, viewGroup, false);
        }
        TextView textViewSubject = view.findViewById(R.id.textViewSubject);
        TextView textViewTitle = view.findViewById(R.id.textViewTitle);
        TextView textViewDeadline = view.findViewById(R.id.textViewDeadline);
        TextView textViewDeadline2 = view.findViewById(R.id.textViewDeadline2);
        TextView textViewAlarm = view.findViewById(R.id.textViewAlarm);
        ImageButton btn = view.findViewById(R.id.imageButtonNotification);
        textViewSubject.setText(items.get(i).subject);
        if(items.get(i).isLecture == true)
            textViewTitle.setText("강의: " + items.get(i).title);
        else
            textViewTitle.setText("과제: " + items.get(i).title);

        String Deadline =  String.valueOf(1900 + items.get(i).dueDate.getYear()) + "년 " + String.valueOf(items.get(i).dueDate.getMonth() + 1) + "월 " + String.valueOf(items.get(i).dueDate.getDate()) + "일 " +(items.get(i).dueDate.getHours()) + ":" + items.get(i).dueDate.getMinutes() + ":" + items.get(i).dueDate.getSeconds();

        textViewDeadline.setText("마감기한: " + Deadline);
        Date currentDate = new Date();
        //Log.d("hours", " " + items.get(i).dueDate.getHours() + ":" + items.get(i).dueDate.getMinutes() + ":" + items.get(i).dueDate.getSeconds());
        long diffDay=(items.get(i).dueDate.getTime()-currentDate.getTime())/(24*60*60*1000);
        long diffHours=(items.get(i).dueDate.getTime()-currentDate.getTime())/(60*60*1000);

        textViewDeadline2.setText(diffDay + "일, " +diffHours + "시간 남음");

        if(items.get(i).isAlarm == 0) {
            Drawable drawable = view.getResources().getDrawable(R.drawable.ic_baseline_notifications_none_24);
            btn.setImageDrawable(drawable);
        }
        else {
            Drawable drawable = view.getResources().getDrawable(R.drawable.ic_baseline_notifications_24);
            btn.setImageDrawable(drawable);
            if (items.get(i).isAlarm == 1) {
                textViewAlarm.setText("6시간 전");
            }
            else if(items.get(i).isAlarm == 2) {
                textViewAlarm.setText("1일 전");
            }
            else {
                textViewAlarm.setText("2일 전");
            }
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                items.get(i).isAlarm = (items.get(i).isAlarm + 1) % 4;
                class InsertRunnable implements Runnable {
                    @Override
                    public void run() {

                        SKKUAssignmentDB db = Room.databaseBuilder(mContext.getApplicationContext(), SKKUAssignmentDB.class, "SKKUassignment.db").build();
                        SKKUAssignmentDao assignmentDao = db.SKKUassignmentDao();

                        for(int count = 0; count < l.size(); count++) {
                            if(l.get(count).assignmentId == items.get(i).assignmentId) {
                                l.get(count).isAlarm = items.get(i).isAlarm;
                                assignmentDao.update(l.get(count));
                            }
                        }

                    }
                }
                InsertRunnable insertRunnable = new InsertRunnable();
                Thread addThread = new Thread(insertRunnable);
                addThread.start();
                if(items.get(i).isAlarm == 0) {
                    Drawable drawable = view.getResources().getDrawable(R.drawable.ic_baseline_notifications_none_24);
                    btn.setImageDrawable(drawable);
                    textViewAlarm.setText("");


                }
                else {
                    Drawable drawable = view.getResources().getDrawable(R.drawable.ic_baseline_notifications_24);
                    btn.setImageDrawable(drawable);
                    if (items.get(i).isAlarm == 1) {
                        textViewAlarm.setText("6시간 전");
                    }
                    else if(items.get(i).isAlarm == 2) {
                        textViewAlarm.setText("1일 전");
                    }
                    else {
                        textViewAlarm.setText("2일 전");
                    }
                }
            }
        });
        return view;
    }

}

