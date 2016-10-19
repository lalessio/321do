package com.alessio.luca.a321do;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
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
    private Button dateButton, confirmButton, cancelButton, buttonDateTime;
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
                            Toast.makeText(context, "Note completed, no need for reminder", Toast.LENGTH_SHORT).show();
                            break;
                        case PLANNED:
                            Log.d("321notifica","notifica creata");
                            break;
                        case EXPIRED:
                            Toast.makeText(context, "Date/Time already passed", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                else
                {
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    Intent intentAlarm = new Intent(context, AlarmReceiver.class);
                    PendingIntent pendingUpdateIntent = PendingIntent.getBroadcast(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.cancel(pendingUpdateIntent);
                    Log.d("321notifica","notifica eliminata");
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


        //inizializzazione sotto dialog date time
//        final Dialog dateTimeDialog = new Dialog(this);
//        dateTimeDialog.setTitle(R.string.editNoteDateTimeEditTitle);
//        dateTimeDialog.setContentView(R.layout.date_time_layout);
//
//        final DatePicker datePicker = (DatePicker) dateTimeDialog.findViewById(R.id.datePicker);
//        final TimePicker timePicker = (TimePicker) dateTimeDialog.findViewById(R.id.timePicker);
//        Button buttonDateTime = (Button) dateTimeDialog.findViewById(R.id.buttonDateTime);
//        Button buttonDismissDateTime = (Button) dateTimeDialog.findViewById(R.id.buttonDismissDateTime);
//
//        datePicker.init(note.getDueDate().get(Calendar.YEAR),note.getDueDate().get(Calendar.MONTH),note.getDueDate().get(Calendar.DAY_OF_MONTH),null);
//        timePicker.setIs24HourView(true);
//        timePicker.setCurrentHour(note.getDueDate().get(Calendar.HOUR_OF_DAY));
//        timePicker.setCurrentMinute(note.getDueDate().get(Calendar.MINUTE));

        //collego comando ai pulsanti
//        dateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dateTimeDialog.show();
//            }
//        });

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
                    //l'orario è gestito altrove
                    noteDBAdapter.updateNote(newNote);
                    //updateListView(currentOrder); TODO spostare in ondimisslistener
                    if (newNote.isAlarmOn() && newNote.getNoteState() == Note.NoteState.PLANNED)
                        planNotification(newNote);
                    dismiss();
                    Toast.makeText(context, "Changes applied", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(context, "Note MUST have a title!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Changes NOT SAVED",Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        //gestisco lo switch del promemoria


        //collego comandi agli spinner e imposto valori di default uguali a quelli già presenti nel db

//        buttonDateTime.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Calendar c=note.getDueDate();
//                c.set(Calendar.YEAR,datePicker.getYear());
//                c.set(Calendar.MONTH,datePicker.getMonth());
//                c.set(Calendar.DAY_OF_MONTH,datePicker.getDayOfMonth());
//                c.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
//                c.set(Calendar.MINUTE,timePicker.getCurrentMinute());
//                note.setDueDate(c);
//                editTextDate.setText(note.printDueDate());
//                dateTimeDialog.dismiss();
//                datePicker.init(note.getDueDate().get(Calendar.YEAR),note.getDueDate().get(Calendar.MONTH),note.getDueDate().get(Calendar.DAY_OF_MONTH),null);
//            }
//        });
//
//        buttonDismissDateTime.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dateTimeDialog.dismiss();
//                datePicker.init(note.getDueDate().get(Calendar.YEAR),note.getDueDate().get(Calendar.MONTH),note.getDueDate().get(Calendar.DAY_OF_MONTH),null);
//                timePicker.setCurrentHour(note.getDueDate().get(Calendar.HOUR_OF_DAY));
//                timePicker.setCurrentMinute(note.getDueDate().get(Calendar.MINUTE));
//            }
//        });


        //inizializzazione sotto dialog checklist
//        final Dialog checkListDialog = new Dialog(this);
//        checkListDialog.setContentView(R.layout.dialog_checklist);
//
//        final EditText editTextCheckList = (EditText) checkListDialog.findViewById(R.id.editTextCheckList);
//        final Button buttonAddCheckListItem = (Button) checkListDialog.findViewById(R.id.buttonCheckListAdd);
//        final ListView listViewCheckList = (ListView) checkListDialog.findViewById(R.id.checklist_list_view);
//        note.addToCheckList("test");
//        //TODO ArrayAdapter<String> checkListAdapter = new ArrayAdapter<String>(context,R.layout.checklist_row,note.getCheckList());
//        //listViewCheckList.setAdapter(checkListAdapter);
//        editTextCheckList.setText(note.getCheckList().toString());
//
//        buttonAddCheckListItem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(editTextCheckList.getText().toString().length()>=1)
//                    note.addToCheckList(editTextCheckList.getText().toString());
//                else
//                    Toast.makeText(context,"Empty field",Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        final Button buttonCheckList = (Button) findViewById(R.id.button_checklist);
//        buttonCheckList.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                checkListDialog.show();
//            }
//        });
    }

    public void planNotification(Note note) {
        long when = note.getDueDate().getTimeInMillis(); //TODO ripristinare
        //long when = System.currentTimeMillis()+3000;
        Intent intentAlarm = new Intent(context, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("NotePayload",note);
        intentAlarm.putExtras(bundle);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, when, PendingIntent.getBroadcast(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }
}
