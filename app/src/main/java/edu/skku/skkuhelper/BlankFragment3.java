package edu.skku.skkuhelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
    //tag example
    String[] tagList = {"전체", "장학", "진로", "취업", "지원"};
    String tag = "전체";
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
        Spinner spinner = v.findViewById(R.id.spinner);
        ToggleButton btn = v.findViewById(R.id.alarmToggle2);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, tagList);
        spinner.setAdapter(adapter);


        //example
        String sum = "This is summary!";
        String link = "https://www.skku.edu/skku/index.do";
        items = new ArrayList<Notice>();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tag = tagList[i];

                items.add(new Notice("공지제목1", "글쓴이1", "2020-01-01", link, true, sum));
                items.add(new Notice("공지제목2", "글쓴이2", "2021-01-01", link, true, sum));
                items.add(new Notice("공지제목3", "글쓴이3", "2021-01-01", link, true, sum));
                listViewAdapter = new ListViewAdapter_Notification(items, getActivity().getApplicationContext());
                listView.setAdapter(listViewAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        btn.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        //toggle 버튼이 on된 경우
                        if(isChecked){
                        }
                        else{
                        }

                    }
                }
        );

       /* listView.setOnItemClickListener (new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(items.get(position).link));
                getActivity().startActivity(intent);
            }
        });*/



        return v;
    }
}