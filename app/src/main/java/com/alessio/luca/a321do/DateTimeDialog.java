package com.alessio.luca.a321do;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Luca on 19/10/2016.
 */
//TODO metodi deprecati
public class DateTimeDialog extends Dialog{
    private Context context;
    private Note note;
    private DatePicker datePicker;
    private TimePicker timePicker;

    public DateTimeDialog(Context context, Note note) {
        super(context);
        this.context = context;
        this.note = note;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.editNoteDateTimeEditTitle);
        setContentView(R.layout.date_time_layout);

        datePicker = (DatePicker) findViewById(R.id.datePicker);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        Button confirmButton = (Button) findViewById(R.id.buttonDateTime);
        Button cancelButton = (Button) findViewById(R.id.buttonDismissDateTime);
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
                {
                    switch (note.getNoteState()){
                        case COMPLETED:
                            Toast.makeText(context, R.string.errorNoNeedForAlarm, Toast.LENGTH_SHORT).show();
                            break;
                        case EXPIRED:
                            Toast.makeText(context, R.string.errorDateTimePassed, Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(context, R.string.messageAlarmOn, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                else
                {
                    Toast.makeText(context, R.string.messageAlarmOff, Toast.LENGTH_SHORT).show();
                }
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c=note.getDueDate();
                c.set(Calendar.YEAR,datePicker.getYear());
                c.set(Calendar.MONTH,datePicker.getMonth());
                c.set(Calendar.DAY_OF_MONTH,datePicker.getDayOfMonth());
                c.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                c.set(Calendar.MINUTE,timePicker.getCurrentMinute());
                note.setDueDate(c);
                if(!note.isAlarmOn())
                {
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                    Intent intentAlarm = new Intent(context, AlarmReceiver.class);
                    alarmManager.cancel(PendingIntent.getBroadcast(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
                }
                dismiss();
                datePicker.init(note.getDueDate().get(Calendar.YEAR),note.getDueDate().get(Calendar.MONTH),note.getDueDate().get(Calendar.DAY_OF_MONTH),null);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                datePicker.init(note.getDueDate().get(Calendar.YEAR),note.getDueDate().get(Calendar.MONTH),note.getDueDate().get(Calendar.DAY_OF_MONTH),null);
                timePicker.setCurrentHour(note.getDueDate().get(Calendar.HOUR_OF_DAY));
                timePicker.setCurrentMinute(note.getDueDate().get(Calendar.MINUTE));
            }
        });
    }
}
