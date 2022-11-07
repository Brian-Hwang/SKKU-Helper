package edu.skku.skkuhelper;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

class Notice {
    public String title;
    public String name;
    public String date;
    public String link;
    //public int hits;
    public boolean bookmark;
    public String summary;
    public Notice(String title, String name, String date, String link, boolean bookmark, String summary) {
        this.title = title;
        this.name = name;
        this.date = date;
        this.link = link;
        //this.hits = hits;
        this.bookmark = bookmark;
        this.summary = summary;
    }
}
public class ListViewAdapter_Notification extends BaseAdapter {

        private ArrayList<Notice> items;
        private Context mContext;
        public ListViewAdapter_Notification(ArrayList<Notice> items, Context mContext) {
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
                view = layoutInflater.inflate(R.layout.notification_part, viewGroup, false);
            }

            TextView textViewTitle = view.findViewById(R.id.textViewtitle2);
            TextView textViewName = view.findViewById(R.id.textViewName);
            TextView textViewDate = view.findViewById(R.id.textViewDate);
            TextView textViewSummary = view.findViewById(R.id.textViewSummary);
            ToggleButton btn1 = view.findViewById(R.id.alarmToggle);

            textViewSummary.setVisibility(View.GONE);
            textViewTitle.setText(items.get(i).title);
            textViewName.setText(items.get(i).name);
            textViewDate.setText(items.get(i).date);

            btn1.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                            //toggle 버튼이 on된 경우
                            if(isChecked){
                                textViewSummary.setVisibility(View.VISIBLE);
                                textViewSummary.setText(items.get(i).summary);
                            }
                            else{
                                textViewSummary.setVisibility(View.GONE);
                            }

                        }
                    }
            );

            return view;

        }
}
