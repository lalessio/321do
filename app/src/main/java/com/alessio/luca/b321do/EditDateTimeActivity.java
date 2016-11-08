package com.alessio.luca.b321do;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by Luca on 26/10/2016.
 */
public class EditDateTimeActivity extends Activity {
    private Note note;
    private NoteDBAdapter noteDBAdapter;
    private DatePicker datePicker;
    private TimePicker timePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        note = (Note) getIntent().getExtras().get(Utilities.EDIT_NOTE_PAYLOAD_CODE);
        noteDBAdapter = new NoteDBAdapter(this);

        setTitle(R.string.editNoteDateTimeEditTitle);
        setContentView(R.layout.date_time_layout);

        datePicker = (DatePicker) findViewById(R.id.datePicker);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        Switch alarmSwitch = (Switch) findViewById(R.id.switch_alarm);

        datePicker.init(note.getDueDate().get(Calendar.YEAR),note.getDueDate().get(Calendar.MONTH),note.getDueDate().get(Calendar.DAY_OF_MONTH),null);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(note.getDueDate().get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(note.getDueDate().get(Calendar.MINUTE));

        alarmSwitch.setChecked(note.isAlarmOn());
        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                note.setAlarm(isChecked);
                if(isChecked)
                    Toast.makeText(EditDateTimeActivity.this, R.string.messageAlarmOn, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(EditDateTimeActivity.this, R.string.messageAlarmOff, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onPause() {
        Calendar c=note.getDueDate();
        c.set(Calendar.YEAR,datePicker.getYear());
        c.set(Calendar.MONTH,datePicker.getMonth());
        c.set(Calendar.DAY_OF_MONTH,datePicker.getDayOfMonth());
        c.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
        c.set(Calendar.MINUTE,timePicker.getCurrentMinute());
        note.setDueDate(c);
        if(note.isAlarmOn())
        {
            switch (note.getNoteState()) {
                case COMPLETED:
                    Toast.makeText(EditDateTimeActivity.this, R.string.errorNoNeedForAlarm, Toast.LENGTH_SHORT).show();
                    break;
                case EXPIRED:
                    Toast.makeText(EditDateTimeActivity.this, R.string.errorDateTimePassed, Toast.LENGTH_SHORT).show();
                    break;
                case PLANNED:
                    long when = note.getDueDate().getTimeInMillis();
                    //long when = System.currentTimeMillis()+5000; //for debug //TODO rimettere a posto
                    Intent intentAlarm = new Intent(EditDateTimeActivity.this, AlarmReceiver.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Utilities.NOTIFICATION_PAYLOAD_CODE, note);
                    intentAlarm.putExtras(bundle);
                    AlarmManager alarmManager = (AlarmManager) EditDateTimeActivity.this.getSystemService(ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, when, PendingIntent.getBroadcast(EditDateTimeActivity.this, note.getId(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
                    break;
            }
        }
        else
        {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intentAlarm = new Intent(EditDateTimeActivity.this, AlarmReceiver.class);
            alarmManager.cancel(PendingIntent.getBroadcast(EditDateTimeActivity.this, note.getId(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
        }
        noteDBAdapter.updateNote(note);
        super.onPause();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }
}
