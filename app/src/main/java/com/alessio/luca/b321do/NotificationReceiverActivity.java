package com.alessio.luca.b321do;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Created by Luca on 11/10/2016.
 */

public class NotificationReceiverActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Note note = (Note) getIntent().getExtras().get(Utilities.NOTIFICATION_PAYLOAD_CODE);
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
                TextView textViewNotificationLength = (TextView) findViewById(R.id.alla);
                ImageView imageViewNotification = (ImageView) findViewById(R.id.imageViewNotification);
                final Button buttonAudioAttachment = (Button) findViewById(R.id.buttonAudioAttachment);
                Button buttonNotificationDismiss = (Button) findViewById(R.id.buttonNotificationDismiss);
                Button buttonNotificationSnooze = (Button) findViewById(R.id.buttonNotificationSnooze);
                Button buttonNotificationTick = (Button) findViewById(R.id.buttonNotificationTick);

                //there is always a title
                textViewNotificationTitle.setText(note.getTitle());

                //is description set?
                if(note.getDescription().length()>0)
                    textViewNotificationDescription.setText(note.getDescription());
                else
                    textViewNotificationDescription.setVisibility(View.GONE);

                //importance is set by default
                textViewNotificationImportance.setText(note.getImportance().toString());

                //is a tag assigned?
                if(note.getTag().length()>0)
                    textViewNotificationTag.setText(note.getTag());
                else
                {
                    LinearLayout tagLayout = (LinearLayout) findViewById(R.id.tagLayout);
                    tagLayout.setVisibility(View.GONE);
                }

                //do we have an audio attachment to play?
                if(!new File(note.getAudioPath()).canRead())
                    buttonAudioAttachment.setVisibility(View.GONE);

                //is length set?
                if(note.getLength()!=0)
                    textViewNotificationLength.setText(" " + note.getLength() + " ");
                else
                {
                    LinearLayout lengthLayout = (LinearLayout) findViewById(R.id.lengthLayout);
                    lengthLayout.setVisibility(View.GONE);
                }

                //do we have an image too?
                if(note.getImgBytes()!=null)
                    imageViewNotification.setImageBitmap(Utilities.resizeImage(BitmapFactory.decodeByteArray(note.getImgBytes(),0,note.getImgBytes().length),0.4f));
                else
                    imageViewNotification.setVisibility(View.GONE);

                final boolean[] startPlaying = {true};
                buttonAudioAttachment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playAudioAttachment(startPlaying[0],note);
                        if (startPlaying[0]) {
                            buttonAudioAttachment.setText(R.string.playButtonStop);
                        } else {
                            buttonAudioAttachment.setText(R.string.playButtonStart);
                        }
                        startPlaying[0] = !startPlaying[0];
                    }
                });
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
                        showSnoozeDialog(note);
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
                showSnoozeDialog(note);
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
        bundle.putSerializable(Utilities.NOTIFICATION_PAYLOAD_CODE,note);
        intentAlarm.putExtras(bundle);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
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
    private void showSnoozeDialog(final Note note) {
        final Dialog dialog = new Dialog(NotificationReceiverActivity.this);
        dialog.setContentView(R.layout.dialog_snooze);
        dialog.show();

        final NumberPicker npHours = (NumberPicker) dialog.findViewById(R.id.snoozeNumberPickerHours);
        npHours.setMaxValue(8);
        npHours.setMinValue(0);
        npHours.setValue(0);

        final NumberPicker npMinutes = (NumberPicker) dialog.findViewById(R.id.snoozeNumberPickerMinutes);
        npMinutes.setMaxValue(7);
        npMinutes.setMinValue(0);
        npMinutes.setDisplayedValues(new String[] {"0", "2", "5", "10", "15", "20", "30", "45"});

        Button snoozeConfirmButton = (Button) dialog.findViewById(R.id.snoozeConfirmButton);
        snoozeConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snoozeNotification(note,npHours.getValue()*3600000+npMinutes.getValue()*60000);
                Toast.makeText(NotificationReceiverActivity.this, R.string.toastNoteSnoozed,Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            }
        });
    }
    private void playAudioAttachment(boolean start, Note note) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        if (start) {
            try {
                mediaPlayer.setDataSource(note.getAudioPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                Log.e("321error", "prepare() failed");
            }
        } else
            mediaPlayer.release();
    }
}
