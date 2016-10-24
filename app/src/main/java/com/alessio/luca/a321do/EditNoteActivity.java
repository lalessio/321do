package com.alessio.luca.a321do;

import android.app.Activity;
import android.app.AlarmManager;
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

/**
 * Created by Luca on 24/10/2016.
 */

public class EditNoteActivity extends Activity {
    private Note note;
    private NoteDBAdapter noteDBAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        note = (Note) getIntent().getExtras().get("EditNotePayload");

        noteDBAdapter = new NoteDBAdapter(EditNoteActivity.this);
        setTitle(R.string.editNoteTitle);
        setContentView(R.layout.dialog_edit_note);

        final EditText editTextTitle = (EditText) findViewById(R.id.editText_title);
        editTextTitle.setText(note.getTitle());
        final EditText editTextDesc = (EditText) findViewById(R.id.editText_description);
        editTextDesc.setText(note.getDescription());
        final EditText editTextDate = (EditText) findViewById(R.id.editText_date);
        editTextDate.setText(note.printDueDate());
        final EditText editTextTag = (EditText) findViewById(R.id.editText_tag);
        editTextTag.setText(note.getTag());

        Button dateButton = (Button) findViewById(R.id.button_date);
        Button confirmButton = (Button) findViewById(R.id.button_confirm);
        Button cancelButton = (Button) findViewById(R.id.button_cancel);
        Switch alarmSwitch = (Switch) findViewById(R.id.switch_alarm);
        Spinner prioritySpinner = (Spinner) findViewById(R.id.spinner_priority);
        Spinner urgencySpinner = (Spinner) findViewById(R.id.spinner_urgency);

        /////////////////////////////////DATE/TIME///////////////////////////////////////////

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTimeDialog dateTimeDialog = new DateTimeDialog(EditNoteActivity.this,note);
                dateTimeDialog.show();
                dateTimeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        editTextDate.setText(note.printDueDate());
                    }
                });
            }
        });

        /////////////////////////////////CHECKLIST///////////////////////////////////////////

        Button buttonCheckList = (Button) findViewById(R.id.button_checklist);
        buttonCheckList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckListDialog checkListDialog = new CheckListDialog(EditNoteActivity.this,note);
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
                            Toast.makeText(EditNoteActivity.this, R.string.errorNoNeedForAlarm, Toast.LENGTH_SHORT).show();
                            break;
                        case EXPIRED:
                            Toast.makeText(EditNoteActivity.this, R.string.errorDateTimePassed, Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Log.d("321EditNodeDialog","case default or PLANNED note");
                            break;
                    }
                }
                else
                {
                    AlarmManager alarmManager = (AlarmManager) EditNoteActivity.this.getSystemService(ALARM_SERVICE);
                    Intent intentAlarm = new Intent(EditNoteActivity.this, AlarmReceiver.class);
                    alarmManager.cancel(PendingIntent.getBroadcast(EditNoteActivity.this, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
                }
            }
        });

        /////////////////////////////////IMPORTANCE///////////////////////////////////////////

        //TODO migliorare salvataggio importance
        final int[] priority = {java.lang.Character.getNumericValue(note.getImportance().toString().charAt(0))};
        final char[] urgency = {note.getImportance().toString().charAt(1)};

        ArrayAdapter<String> priorities = new ArrayAdapter<>(EditNoteActivity.this, android.R.layout.simple_spinner_item, new Importance().getAllPriorities());
        prioritySpinner.setAdapter(priorities);
        prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapter, View view, int pos, long id) {
                String selected = adapter.getItemAtPosition(pos).toString();
                priority[0] = java.lang.Character.getNumericValue(selected.charAt(0));
            }
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
        prioritySpinner.setSelection(priority[0]-1);

        ArrayAdapter<String> urgencies = new ArrayAdapter<>(EditNoteActivity.this, android.R.layout.simple_spinner_item, new Importance().getAllUrgencies());
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
                    Toast.makeText(EditNoteActivity.this, R.string.messageChangesApplied, Toast.LENGTH_SHORT).show();
                    finish();
                    overridePendingTransition(0,0);
                }
                else
                {
                    Toast.makeText(EditNoteActivity.this, R.string.errorEmptyTitle, Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EditNoteActivity.this, R.string.messageChangesNotApplied,Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(0,0);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(EditNoteActivity.this, R.string.messageChangesNotApplied,Toast.LENGTH_SHORT).show();
        overridePendingTransition(0,0);
    }

    public void planNotification(Note note) {
        long when = note.getDueDate().getTimeInMillis();
        //long when = System.currentTimeMillis()+3000; //for debug
        Intent intentAlarm = new Intent(EditNoteActivity.this, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("NotePayload",note);
        intentAlarm.putExtras(bundle);
        AlarmManager alarmManager = (AlarmManager) EditNoteActivity.this.getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, when, PendingIntent.getBroadcast(EditNoteActivity.this, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }
}
