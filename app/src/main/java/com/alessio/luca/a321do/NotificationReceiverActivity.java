package com.alessio.luca.a321do;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
                setContentView(R.layout.notification_layout);

                TextView textViewNotificationTitle = (TextView) findViewById(R.id.textViewNotificationTitle);
                TextView textViewNotificationDescription = (TextView) findViewById(R.id.textViewNotificationDescription);
                TextView textViewNotificationTag = (TextView) findViewById(R.id.textViewNotificationTag);
                TextView textViewNotificationImportance = (TextView) findViewById(R.id.textViewNotificationImportance);
                ImageView imageViewNotification = (ImageView) findViewById(R.id.imageViewNotification);
                Button buttonNotificationDismiss = (Button) findViewById(R.id.buttonNotificationDismiss);
                Button buttonNotificationSnooze = (Button) findViewById(R.id.buttonNotificationSnooze);
                Button buttonNotificationTick = (Button) findViewById(R.id.buttonNotificationTick);

                textViewNotificationTitle.setText(note.getTitle());
                textViewNotificationDescription.setText(note.getDescription());
                textViewNotificationTag.setText(note.getTag());
                textViewNotificationImportance.setText(note.getImportance().toString());

                if(note.getImgBytes()!=null)
                    imageViewNotification.setImageBitmap(BitmapFactory.decodeByteArray(note.getImgBytes(),0,note.getImgBytes().length));

                buttonNotificationDismiss.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(NotificationReceiverActivity.this, R.string.toastNoteDismissed,Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                buttonNotificationSnooze.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long amount = 10000;
                        snoozeNotification(note,amount);
                        Toast.makeText(NotificationReceiverActivity.this, R.string.toastNoteSnoozed,Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                buttonNotificationTick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tickAction(note);
                        Toast.makeText(NotificationReceiverActivity.this, R.string.toastNoteCompleted,Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                break;

            case AlarmReceiver.CLOSE_NOTIFICATION:
                closeNotification(note.getId());
                finish();
                break;

            case AlarmReceiver.SNOOZE_NOTE:
                long amount = 10000;
                snoozeNotification(note,amount);
                finish();
                break;

            default:
                Log.w("321NRA","default should never verify");
                closeNotification(note.getId());
                finish();
                break;
        }
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
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+amount, PendingIntent.getBroadcast(this, note.getId(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
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
