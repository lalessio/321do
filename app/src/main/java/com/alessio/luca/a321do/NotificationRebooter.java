package com.alessio.luca.a321do;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Luca on 31/10/2016.
 */

public class NotificationRebooter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NoteDBAdapter noteDBAdapter = new NoteDBAdapter(context);
        Cursor cursor = noteDBAdapter.retrieveAllNotes(new SortingOrder());
        cursor.moveToFirst();
        while(!cursor.isAfterLast())
        {
            Note temp = new Note();
            temp.setId(cursor.getInt(cursor.getColumnIndex(NoteDBAdapter.COL_ID)));
            temp.setTitle(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_TITLE)));
            temp.setDescription(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_DESCRIPTION)));
            temp.setTag(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_TAG)));
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

            if(temp.isAlarmOn() && temp.getNoteState() == Note.NoteState.PLANNED)
            {
                long when = temp.getDueDate().getTimeInMillis();
                Intent intentAlarm = new Intent(context, AlarmReceiver.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(Utilities.NOTIFICATION_PAYLOAD_CODE,temp);
                intentAlarm.putExtras(bundle);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, when, PendingIntent.getBroadcast(context, temp.getId(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
            }

            cursor.moveToNext();
        }
    }
}
