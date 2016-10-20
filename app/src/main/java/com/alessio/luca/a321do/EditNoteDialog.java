package com.alessio.luca.a321do;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by Luca on 19/10/2016.
 */

public class EditNoteDialog extends Dialog {

    private Context context;
    private Note note;
    private NoteDBAdapter noteDBAdapter;
    private EditText editTextTitle, editTextDesc, editTextTag, editTextDate;
    private Button dateButton, confirmButton, cancelButton, buttonCheckList;
    private Switch alarmSwitch;
    private Spinner prioritySpinner, urgencySpinner;

    public EditNoteDialog(Context context, Note note) {
        super(context);
        this.context=context;
        this.note=note;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        noteDBAdapter = new NoteDBAdapter(context);
        setTitle(R.string.editNoteTitle);
        setContentView(R.layout.dialog_edit_note);

        editTextTitle = (EditText) findViewById(R.id.editText_title);
        editTextTitle.setText(note.getTitle());
        editTextDesc = (EditText) findViewById(R.id.editText_description);
        editTextDesc.setText(note.getDescription());
        editTextDate = (EditText) findViewById(R.id.editText_date);
        editTextDate.setText(note.printDueDate());
        editTextTag = (EditText) findViewById(R.id.editText_tag);
        editTextTag.setText(note.getTag());

        dateButton = (Button) findViewById(R.id.button_date);
        confirmButton = (Button) findViewById(R.id.button_confirm);
        cancelButton = (Button) findViewById(R.id.button_cancel);
        alarmSwitch = (Switch) findViewById(R.id.switch_alarm);
        prioritySpinner = (Spinner) findViewById(R.id.spinner_priority);
        urgencySpinner = (Spinner) findViewById(R.id.spinner_urgency);

        /////////////////////////////////DATE/TIME///////////////////////////////////////////

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTimeDialog dateTimeDialog = new DateTimeDialog(context,note);
                dateTimeDialog.show();
                dateTimeDialog.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        editTextDate.setText(note.printDueDate());
                    }
                });
            }
        });

        /////////////////////////////////CHECKLIST///////////////////////////////////////////

        buttonCheckList = (Button) findViewById(R.id.button_checklist);
        buttonCheckList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckListDialog checkListDialog = new CheckListDialog(context,note);
                checkListDialog.show();
            }
        });

        /////////////////////////////////SWITCH///////////////////////////////////////////

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
                            Log.d("321EditNodeDialog","case default or PLANNED note");
                            break;
                    }
                }
                else
                {
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    Intent intentAlarm = new Intent(context, AlarmReceiver.class);
                    alarmManager.cancel(PendingIntent.getBroadcast(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
                }
            }
        });

        /////////////////////////////////IMPORTANCE///////////////////////////////////////////

        //TODO migliorare salvataggio importance
        final int[] priority = {java.lang.Character.getNumericValue(note.getImportance().toString().charAt(0))};
        final char[] urgency = {note.getImportance().toString().charAt(1)};

        ArrayAdapter<String> priorities = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, new Importance().getAllPriorities());
        prioritySpinner.setAdapter(priorities);
        prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapter, View view, int pos, long id) {
                String selected = adapter.getItemAtPosition(pos).toString();
                priority[0] = java.lang.Character.getNumericValue(selected.charAt(0));
            }
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
        prioritySpinner.setSelection(priority[0]-1);

        ArrayAdapter<String> urgencies = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, new Importance().getAllUrgencies());
        urgencySpinner.setAdapter(urgencies);
        urgencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapter, View view,int pos, long id) {
                String selected = (String)adapter.getItemAtPosition(pos);
                urgency[0] = selected.charAt(0);
            }
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
        urgencySpinner.setSelection(java.lang.Character.getNumericValue(urgency[0])-10); //A = 12 in ASCII, la selezione va da 0 a 2 quindi converto la lettera in un valore accettabile dallo spinner

        /////////////////////////////////SAVE CHANGES LOGIC//////////////////////////////////////

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextTitle.getText().toString().length()>0)
                {
                    Note newNote = new Note(note);
                    newNote.setTitle(editTextTitle.getText().toString());
                    newNote.setImportance(priority[0], urgency[0]);
                    newNote.setDescription(editTextDesc.getText().toString());
                    newNote.setTag(editTextTag.getText().toString());
                    newNote.setCheckList(note.getCheckList());
                    //l'orario è gestito da DateTimeDialog
                    noteDBAdapter.updateNote(newNote);
                    if (newNote.isAlarmOn() && newNote.getNoteState() == Note.NoteState.PLANNED)
                        planNotification(newNote);
                    dismiss();
                    Toast.makeText(context, R.string.messageChangesApplied, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(context, R.string.errorEmptyTitle, Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, R.string.messageChangesNotApplied,Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }

    public void planNotification(Note note) {
        long when = note.getDueDate().getTimeInMillis();
        //long when = System.currentTimeMillis()+3000; //for debug
        Intent intentAlarm = new Intent(context, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("NotePayload",note);
        intentAlarm.putExtras(bundle);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, when, PendingIntent.getBroadcast(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }
}
