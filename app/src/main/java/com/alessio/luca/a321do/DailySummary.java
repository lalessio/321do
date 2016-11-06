package com.alessio.luca.a321do;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Luca on 03/11/2016.
 */

public class DailySummary extends Activity{
    private NoteDBAdapter noteDBAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.daily_summary_layout);
        noteDBAdapter = new NoteDBAdapter(this);
        ArrayList<String> todayNotes = new ArrayList<>();
        Cursor cursor = noteDBAdapter.retrieveAllNotes(new SortingOrder(SortingOrder.Order.NONE, SortingOrder.Filter.TODAY));
        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            todayNotes.add(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_TITLE)));
            cursor.moveToNext();
        }

        if(!todayNotes.isEmpty())
        {
            ListView listView = (ListView) findViewById(R.id.summaryListView);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,android.R.id.text1,todayNotes);
            listView.setAdapter(adapter);

            Button okButton = (Button) findViewById(R.id.summaryOkButton);
            Button openAppButton = (Button) findViewById(R.id.summaryOpenAppButton);

            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            openAppButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DailySummary.this, NoteActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

            //TODO launch intent
        }
    }
}
