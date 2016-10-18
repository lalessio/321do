package com.alessio.luca.a321do;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Luca on 11/10/2016.
 */

public class NotificationReceiverActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Note note = (Note) getIntent().getExtras().get("NotePayload");
        switch (getIntent().getAction()){
            case AlarmReceiver.COMPLETE_NOTE:
                tickAction(note);
                closeNotification(note.getId());
                finish();
                break;

            case AlarmReceiver.OPEN_NOTIFICATION:
                //TODO layout result
                setContentView(R.layout.result);
                final TextView t = (TextView) findViewById(R.id.textViewResult);
                Button b = (Button) findViewById(R.id.buttonResult);
                t.setText(note.print());
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tickAction(note);
                        Intent intent = new Intent(NotificationReceiverActivity.this, NoteActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                break;

            case AlarmReceiver.CLOSE_NOTIFICATION:
                closeNotification(note.getId());
                finish();
                break;

            case AlarmReceiver.SNOOZE_NOTE:
                long amount = 5000;
                snoozeNotification(note,amount);
                finish();
                break;

            default:
                Log.w("321NRA","default should never verify");
                closeNotification(note.getId());
                finish();
                break;
        }
        return;
    }
    private void snoozeNotification(Note note, long amount) {
        //prima chiudo la notifica che esiste già perchè andrò a riusarne l'id
        closeNotification(note.getId());
        Intent intentAlarm = new Intent(NotificationReceiverActivity.this, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("NotePayload",note);
        intentAlarm.putExtras(bundle);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        //per ora snooze DAL MOMENTO IN CUI PREMO SNOOZE
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+amount, PendingIntent.getBroadcast(this, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }
    private void tickAction(Note note) {
        NoteDBAdapter noteDBAdapter = new NoteDBAdapter(NotificationReceiverActivity.this);
        noteDBAdapter.tickNote(note);
    }
    private void closeNotification(int id){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }
}
