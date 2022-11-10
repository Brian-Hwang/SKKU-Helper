package edu.skku.skkuhelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import edu.skku.skkuhelper.roomdb.SKKUAssignment;
import edu.skku.skkuhelper.roomdb.SKKUAssignmentDB;
import edu.skku.skkuhelper.roomdb.SKKUAssignmentDao;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BlankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment extends Fragment {
    View v;
    private int check = 0;
    private ArrayList<LA> items1;
    private ArrayList<LA> items2;
    private ListViewAdapter_LR listViewAdapter;
    private ListView listView;
    List<SKKUAssignment> icampusList;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public boolean isFinished = false;
    public BlankFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BlankFragment newInstance(String param1, String param2) {
        BlankFragment fragment = new BlankFragment();
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
        items1 = new ArrayList<LA>();
        items2 = new ArrayList<LA>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_blank, container, false);
        listView = v.findViewById(R.id.listViewIcampus);
        Button btn1 = v.findViewById(R.id.buttonLecture);
        Button btn2 = v.findViewById(R.id.buttonAssignment);
//        btn1.setEnabled(false);
//        btn2.setEnabled(false);
        SKKUAssignmentDB db = Room.databaseBuilder(getActivity().getApplicationContext(), SKKUAssignmentDB.class, "SKKUassignment.db").build();
        SKKUAssignmentDao assignmentDao = db.SKKUassignmentDao();
        final android.os.Handler handler=new Handler();

        class InsertRunnable implements Runnable {
            @Override
            public void run() {
                icampusList = assignmentDao.getAll();
                Date date1 = new Date();
                String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(date1);
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                for(int count = 0; count < icampusList.size(); count++) {
                    String dateString = String.valueOf(1900 + icampusList.get(count).dueDate.getYear() + "-" + String.valueOf(icampusList.get(count).dueDate.getMonth() + 1) + "-" + String.valueOf(icampusList.get(count).dueDate.getDate()));
                    Date date2 = null;
                    try {
                        date2 = fmt.parse(dateString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long diff = Math.abs(date1.getTime() - date2.getTime());
                    long diffDays = diff / (24 * 60 * 60 * 1000);
                    if(icampusList.get(count).isLecture) {
                        items1.add(new LA(String.valueOf(icampusList.get(count).courseName), (String.valueOf(icampusList.get(count).assignmentName)), icampusList.get(count).assignmentId, fmt.format(date2), diffDays, icampusList.get(count).isAlarm, icampusList.get(count).url));
                    }
                    else {
                        items2.add(new LA(String.valueOf(icampusList.get(count).courseName), (String.valueOf(icampusList.get(count).assignmentName)), icampusList.get(count).assignmentId, fmt.format(date2), diffDays, icampusList.get(count).isAlarm, icampusList.get(count).url));
                    }
                }
                isFinished =true;
            }
        }

        InsertRunnable insertRunnable = new InsertRunnable();
        Thread addThread = new Thread(insertRunnable);
        addThread.start();

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFinished){
                    check = 1;
                    items1.sort(sortByTotalCall);
                    listViewAdapter = new ListViewAdapter_LR(items1, getActivity().getApplicationContext(), icampusList);
                    listView.setAdapter(listViewAdapter);
                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFinished){
                    check = 2;
                    items2.sort(sortByTotalCall);
                    listViewAdapter = new ListViewAdapter_LR(items2, getActivity().getApplicationContext(), icampusList);
                    listView.setAdapter(listViewAdapter);
            }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                if(check == 1) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(items1.get(position).url));
                }
                else {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(items2.get(position).url));
                }
                startActivity(intent);
            }
        });
        return v;
    }
    private final static Comparator<LA> sortByTotalCall = new Comparator<LA>() {
        @Override
        public int compare(LA o1, LA o2) {
            return Long.compare(o1.restDate, o2.restDate);
        }
    };

}