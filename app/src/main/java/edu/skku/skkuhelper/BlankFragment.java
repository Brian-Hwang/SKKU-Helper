package edu.skku.skkuhelper;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BlankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment extends Fragment {
    View v;
    private ArrayList<LA> items;
    private ListViewAdapter_LR listViewAdapter;
    private ListView listView;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_blank, container, false);
        listView = v.findViewById(R.id.listViewIcampus);
        Button btn1 = v.findViewById(R.id.buttonLecture);
        Button btn2 = v.findViewById(R.id.buttonAssignment);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //example
                items = new ArrayList<LA>();
                items.add(new LA("과목1", "이름1", "마감기한1", 4, false));
                items.add(new LA("과목2", "이름1", "마감기한1", 4, false));
                items.add(new LA("과목3", "이름1", "마감기한1", 4, true));
                items.add(new LA("과목4", "이름1", "마감기한1", 4, false));
                items.add(new LA("과목5", "이름1", "마감기한1", 4, true));
                items.add(new LA("과목6", "이름1", "마감기한1", 4, true));

                listViewAdapter = new ListViewAdapter_LR(items, getActivity().getApplicationContext());
                listView.setAdapter(listViewAdapter);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                items = new ArrayList<LA>();
                items.add(new LA("과제1", "이름1", "마감기한1", 4, false));
                items.add(new LA("과제2", "이름1", "마감기한1", 4, false));
                items.add(new LA("과제3", "이름1", "마감기한1", 4, true));

                listViewAdapter = new ListViewAdapter_LR(items, getActivity().getApplicationContext());
                listView.setAdapter(listViewAdapter);
            }
        });
        return v;
    }

}