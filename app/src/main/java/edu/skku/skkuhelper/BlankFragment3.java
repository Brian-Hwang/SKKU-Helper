package edu.skku.skkuhelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import edu.skku.skkuhelper.roomdb.SKKUAssignmentDB;
import edu.skku.skkuhelper.roomdb.SKKUAssignmentDao;
import edu.skku.skkuhelper.roomdb.SKKUNotice;
import edu.skku.skkuhelper.roomdb.SKKUNoticeDB;
import edu.skku.skkuhelper.roomdb.SKKUNoticeDao;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BlankFragment3#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment3 extends Fragment {
    View v;
    private ArrayList<Notice> items;
    private ListViewAdapter_Notification listViewAdapter;
    private ListView listView;
    private List<SKKUNotice> noticeList;
    //tag example
    String[] tagList = {"전체", "학사", "입학", "취업", "채용/모집", "장학", "행사/세미나", "일반", "소프트웨어대학_학부", "소프트웨어대학_일반", "소프트웨어대학_대학원"};
    int tag = -1;
    String[] sortList = {"최신순", "조회순"};
    String sort = "최신순";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BlankFragment3() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment3.
     */
    // TODO: Rename and change types and number of parameters
    public static BlankFragment3 newInstance(String param1, String param2) {
        BlankFragment3 fragment = new BlankFragment3();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_blank3, container, false);
        listView = v.findViewById(R.id.listViewNotification);
        Spinner spinner2 = v.findViewById(R.id.spinner2);
        Spinner spinner3 = v.findViewById(R.id.spinner3);
        Button btn = v.findViewById(R.id.buttonApply);
        TextView text = v.findViewById(R.id.Empty);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, tagList);
        spinner2.setAdapter(adapter);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, sortList);
        spinner3.setAdapter(adapter2);

        SKKUNoticeDB db = Room.databaseBuilder(getActivity().getApplicationContext(), SKKUNoticeDB.class, "SKKUNotice.db").build();
        SKKUNoticeDao noticeDao = db.SKKUnoticeDao();

        class InsertRunnable implements Runnable {
            @Override
            public void run() {
                noticeList = noticeDao.getAll();

            }
        }

        InsertRunnable insertRunnable = new InsertRunnable();
        Thread addThread = new Thread(insertRunnable);
        addThread.start();

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tag = i - 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sort = sortList[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                items = new ArrayList<Notice>();
                for(int count = 0; count < noticeList.size(); count++) {
                    if(tag == -1) {
                        items.add(new Notice(String.valueOf(noticeList.get(count).title), (String.valueOf(noticeList.get(count).writer)), String.valueOf(noticeList.get(count).date), noticeList.get(count).link, noticeList.get(count).watch, noticeList.get(count).sum));
                        continue;
                    }
                    if(noticeList.get(count).tag == tag) {
                        items.add(new Notice(String.valueOf(noticeList.get(count).title), (String.valueOf(noticeList.get(count).writer)), String.valueOf(noticeList.get(count).date), noticeList.get(count).link, noticeList.get(count).watch, noticeList.get(count).sum));
                    }

                }
                if(sort == "조회순") {
                    Collections.sort(items, sortByTotalCall);
                    Collections.reverse(items);
                }
                listViewAdapter = new ListViewAdapter_Notification(items, getActivity().getApplicationContext());
                listView.setAdapter(listViewAdapter);
                listView.setEmptyView(text);
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(items.get(position).link));
                startActivity(intent);
            }
        });

        return v;
    }
    private final static Comparator<Notice> sortByTotalCall = new Comparator<Notice>() {
        @Override
        public int compare(Notice o1, Notice o2) {
            return Integer.compare(o1.watch, o2.watch);
        }
    };

}
