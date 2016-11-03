package com.alessio.luca.a321do;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;

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

        noteDBAdapter = new NoteDBAdapter(this);
        ArrayList<Note> retrievedNotes = new ArrayList<>();
        Cursor cursor = noteDBAdapter.retrieveAllNotes(new SortingOrder(SortingOrder.Order.NONE, SortingOrder.Filter.TODAY));
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) //forse il contenuto di questo for può diventare una funzione da qualche altra parte
        {
            Note temp = new Note();
            temp.setId(cursor.getInt(cursor.getColumnIndex(NoteDBAdapter.COL_ID)));
            temp.setTitle(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_TITLE)));
            temp.setDescription(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_DESCRIPTION)));
            temp.setTag(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_TAG)));
            temp.setLength(cursor.getInt(cursor.getColumnIndex(NoteDBAdapter.COL_LENGTH)));
            Calendar t = new GregorianCalendar();
            t.setTimeInMillis(Long.valueOf(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_DUEDATE))));
            temp.setDueDate(t);
            temp.setImportance(new Importance(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_IMPORTANCE))));
            temp.setImgBytes(cursor.getBlob(cursor.getColumnIndex(NoteDBAdapter.COL_IMAGE)));
            ArrayList<String> nCheckList = new ArrayList<>(Utilities.stringToCheckList(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_CHECKLIST))));
            temp.setCheckList(nCheckList);
            if(cursor.getInt(cursor.getColumnIndex(NoteDBAdapter.COL_DONE))==0)
                temp.setDone(false);
            else
                temp.setDone(true);
            if(cursor.getInt(cursor.getColumnIndex(NoteDBAdapter.COL_ALARM))==0)
                temp.setAlarm(false);
            else
                temp.setAlarm(true);
            retrievedNotes.add(temp);
            cursor.moveToNext();
        }

//        Note[] notes = retrievedNotes.toArray(new Note[retrievedNotes.size()]);
//        NoteListAdapter noteListAdapter = new NoteListAdapter(this,R.layout.note_row,notes,sortBy);
//        listView.setAdapter(noteListAdapter);
//        TextView emptyText = (TextView)  findViewById(R.id.emptyList);
//        if(!noteListAdapter.isEmpty())
//            emptyText.setText("");
//        else
//            emptyText.setText(R.string.errorEmptyListView);
    }
}
