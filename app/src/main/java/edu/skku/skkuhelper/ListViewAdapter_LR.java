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
import android.widget.Toast;

import java.util.ArrayList;

class LA {
    public String subject;
    public String title;
    public String deadline;
    public int rest;
    public boolean notification ;
    public LA(String subject, String title, String deadline, int rest, boolean notification){
        this.subject = subject;
        this.title = title;
        this.deadline = deadline;
        this.rest = rest;
        this.notification = notification;
    }

}

public class ListViewAdapter_LR extends BaseAdapter {
    private ArrayList<LA> items;
    private Context mContext;
    private String page;



    public ListViewAdapter_LR(ArrayList<LA> items, Context mContext) {
        this.mContext = mContext;
        this.items = items;
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
        ImageButton btn = view.findViewById(R.id.imageButtonNotification);
        textViewSubject.setText(items.get(i).subject);
        textViewTitle.setText(items.get(i).title);
        textViewDeadline.setText(items.get(i).deadline);
        textViewDeadline2.setText(items.get(i).rest + " 일 남음");
        if(items.get(i).notification == true) {
            Drawable drawable = view.getResources().getDrawable(R.drawable.ic_baseline_notifications_24);
            btn.setImageDrawable(drawable);
        }
        else {
            Drawable drawable = view.getResources().getDrawable(R.drawable.ic_baseline_notifications_none_24);
            btn.setImageDrawable(drawable);
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(items.get(i).notification == true) {
                    Drawable drawable = view.getResources().getDrawable(R.drawable.ic_baseline_notifications_none_24);
                    btn.setImageDrawable(drawable);
                    items.get(i).notification = false;

                }
                else {
                    Drawable drawable = view.getResources().getDrawable(R.drawable.ic_baseline_notifications_24);
                    btn.setImageDrawable(drawable);
                    items.get(i).notification = true;
                }
            }
        });
        return view;
    }
}
