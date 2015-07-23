package com.example.anottingham.highline;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HistoryActivity extends Activity {
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //get the listView
        expListView = (ExpandableListView)findViewById(R.id.history);

        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);

    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String> >();

        listDataHeader.add("1");
        listDataHeader.add("2");
        listDataHeader.add("3");

        List<String> first = new ArrayList<String>();
        first.add("1.1");
        first.add("1.2");
        first.add("1.3");

        List<String> second = new ArrayList<String>();
        second.add("2.1");
        second.add("2.2");
        second.add("2.3");

        List<String> third = new ArrayList<String>();
        third.add("3.1");
        third.add("3.2");
        third.add("3.3");

        listDataChild.put(listDataHeader.get(0), first);
        listDataChild.put(listDataHeader.get(1), second);
        listDataChild.put(listDataHeader.get(2), third);
    }
}
