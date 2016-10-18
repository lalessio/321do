package com.alessio.luca.a321do;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

//TODO per venerdì 14: note con tutti i tipi di dato interni pensati, CRUD ecc.

public class NoteActivity extends AppCompatActivity {
    private ListView listView;
    private NoteDBAdapter noteDBAdapter;
    private ArrayList<Note> retrievedNotes;
    private NoteDBAdapter.SortingOrder currentOrder;
    

    //alla creazione imposto la lista che visualizza le note richieste (al momento solo questo)
    @Override
    protected void onCreate(Bundle savedInstanceState) { //TODO in futuro implementare il discorso di più liste simultanee

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        //TODO capire se la action bar serve
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setHomeButtonEnabled(true);
        //actionBar.setDisplayShowHomeEnabled(true);
        //actionBar.setIcon(R.mipmap.ic_launcher);

        listView = (ListView) findViewById(R.id.note_list_view);
        listView.setDivider(null);

        noteDBAdapter = new NoteDBAdapter(this);
        //noteDBAdapter.open();
        listView = (ListView)findViewById(R.id.note_list_view);
        retrievedNotes = new ArrayList<>();
        currentOrder = NoteDBAdapter.SortingOrder.NONE;
        updateListView(currentOrder);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int masterListPosition, long id) {
                //creo finestra
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                ListView modeListView = new ListView(NoteActivity.this);
                String[] modes = new String[] { getString(R.string.noteOptionEdit), getString(R.string.noteOptionDelete), getString(R.string.noteOptionTick) };
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(NoteActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, modes);
                modeListView.setAdapter(modeAdapter);
                builder.setView(modeListView);
                final Dialog dialog = builder.create();
                dialog.show();

                //gestico ordini
                modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position){
                            case 0:
                                showEditNoteMenu(noteDBAdapter.retrieveNoteById(retrievedNotes.get(masterListPosition).getId()));
                                break;
                            case 1:
                                noteDBAdapter.deleteNote(retrievedNotes.get(masterListPosition));
                                updateListView(currentOrder);
                                break;
                            case 2:
                                noteDBAdapter.tickNote(retrievedNotes.get(masterListPosition));
                                updateListView(currentOrder);
                                break;
                        }
                        dialog.dismiss();
                    }
                });
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewNoteMenu();
            }
        });

        //TODO cancellazione multipla
//        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
//        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
//            @Override
//            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
//            }
//
//            @Override
//            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//                return false;
//            }
//
//            @Override
//            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//                return false;
//            }
//
//            @Override
//            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.multipleDelete:
//                        for (int nC = new NoteListAdapter(NoteActivity.this,R.layout.note_row,retrievedNotes.toArray(new Note[retrievedNotes.size()]),currentOrder).getCount() - 1; nC >= 0; nC--) {
//                            if (listView.isItemChecked(nC)) {
//                                noteDBAdapter.deleteNote(retrievedNotes.get(nC));
//                            }
//                        }
//                        mode.finish();
//                        updateListView(currentOrder);
//                        return true;
//                }
//                return false;
//            }
//
//            @Override
//            public void onDestroyActionMode(ActionMode mode) {
//
//            }
//        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateListView(currentOrder);
    }

    //gestisco il menù
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                showNewNoteMenu();
                return true;
            case R.id.action_exit:
                finish();
                return true;
            case R.id.action_settings:
                Toast.makeText(NoteActivity.this, "settings TODO", Toast.LENGTH_SHORT).show();
                //noteDBAdapter.selfDestruct();
                return true;
            case R.id.action_sort:
                showSortMenu();
                return true;
            default:
                return false;
        }
    }

    //funzioni di servizio
    private void showSortMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
        ListView modeListView = new ListView(NoteActivity.this);
        String[] modes = new String[] { getString(R.string.sortOptionCreation),
                                        getString(R.string.sortOptionDueDate),
                                        getString(R.string.sortOptionImportance),
                                        getString(R.string.sortOptionTag) };
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(NoteActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, modes);
        modeListView.setAdapter(modeAdapter);
        builder.setView(modeListView);
        final Dialog dialog = builder.create();
        dialog.setTitle(R.string.sortOptionTitle);
        dialog.show();
        modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        currentOrder = NoteDBAdapter.SortingOrder.NONE;
                        break;
                    case 1:
                        currentOrder = NoteDBAdapter.SortingOrder.DUEDATE;
                        break;
                    case 2:
                        currentOrder = NoteDBAdapter.SortingOrder.IMPORTANCE;
                        break;
                    case 3:
                        currentOrder = NoteDBAdapter.SortingOrder.CATEGORY;
                        break;
                }
                updateListView(currentOrder);
                dialog.dismiss();
            }
        });
    }

    private void showNewNoteMenu(){
        //creo la finestrella e la popolo
        final Dialog dialog = new Dialog(this);
        dialog.setTitle(R.string.newNoteTitle);
        dialog.setContentView(R.layout.dialog_new_note);
        final EditText editText = (EditText) dialog.findViewById(R.id.editText_title);
        //LinearLayout linearLayout = (LinearLayout) dialog.findViewById(R.id.new_note_layout);
        final Button confirmButton = (Button) dialog.findViewById(R.id.button_confirm);
        final Button cancelButton = (Button) dialog.findViewById(R.id.button_cancel);
        dialog.show();

        //collego comando ai pulsanti
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteDBAdapter.createNote(new Note(editText.getText().toString()));
                updateListView(currentOrder);
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void showEditNoteMenu(final Note note) {
        final Dialog dialog = new Dialog(this);
        dialog.setTitle(R.string.editNoteTitle);
        dialog.setContentView(R.layout.dialog_edit_note);

        final EditText editTextTitle = (EditText) dialog.findViewById(R.id.editText_title);
        editTextTitle.setText(note.getTitle());
        final EditText editTextDesc = (EditText) dialog.findViewById(R.id.editText_description);
        editTextDesc.setText(note.getDescription());
        final EditText editTextDate = (EditText) dialog.findViewById(R.id.editText_date);
        editTextDate.setText(note.printDueDate());
        final EditText editTextTag = (EditText) dialog.findViewById(R.id.editText_tag);
        editTextTag.setText(note.getTag());

        final Button dateButton = (Button) dialog.findViewById(R.id.button_date);
        final Button confirmButton = (Button) dialog.findViewById(R.id.button_confirm);
        final Button cancelButton = (Button) dialog.findViewById(R.id.button_cancel);
        Switch alarmSwitch = (Switch) dialog.findViewById(R.id.switch_alarm);
        final Spinner prioritySpinner = (Spinner) dialog.findViewById(R.id.spinner_priority);
        Spinner urgencySpinner = (Spinner) dialog.findViewById(R.id.spinner_urgency);
        final int[] priority = {java.lang.Character.getNumericValue(note.getImportance().translate().charAt(0))};
        final char[] urgency = {note.getImportance().translate().charAt(1)};

        dialog.show();

        //inizializzazione sotto dialog date time
        final Dialog dateTimeDialog = new Dialog(this);
        dateTimeDialog.setTitle(R.string.editNoteDateTimeEditTitle);
        dateTimeDialog.setContentView(R.layout.date_time_layout);

        final DatePicker datePicker = (DatePicker) dateTimeDialog.findViewById(R.id.datePicker);
        final TimePicker timePicker = (TimePicker) dateTimeDialog.findViewById(R.id.timePicker);
        Button buttonDateTime = (Button) dateTimeDialog.findViewById(R.id.buttonDateTime);
        Button buttonDismissDateTime = (Button) dateTimeDialog.findViewById(R.id.buttonDismissDateTime);

        datePicker.init(note.getDueDate().get(Calendar.YEAR),note.getDueDate().get(Calendar.MONTH),note.getDueDate().get(Calendar.DAY_OF_MONTH),null);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(note.getDueDate().get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(note.getDueDate().get(Calendar.MINUTE));

        //collego comando ai pulsanti
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateTimeDialog.show();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note newNote = new Note(note);
                newNote.setTitle(editTextTitle.getText().toString());
                newNote.setImportance(priority[0],urgency[0]);
                newNote.setDescription(editTextDesc.getText().toString());
                newNote.setTag(editTextTag.getText().toString());
                //l'orario è gestito altrove
                noteDBAdapter.updateNote(newNote);
                updateListView(currentOrder);
                if(newNote.isAlarmOn() && newNote.getNoteState() == Note.NoteState.PLANNED)
                    planNotification(newNote);
                dialog.dismiss();
                Toast.makeText(NoteActivity.this,"Changes applied",Toast.LENGTH_SHORT).show();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NoteActivity.this,"Changes NOT SAVED",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        //gestisco lo switch del promemoria
        alarmSwitch.setChecked(note.isAlarmOn());
        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                note.setAlarm(isChecked);
                if(isChecked)
                {
                    switch (note.getNoteState()){
                        case COMPLETED:
                            Toast.makeText(NoteActivity.this, "Note completed, no need for reminder", Toast.LENGTH_SHORT).show();
                            break;
                        case PLANNED:
                            Log.d("321notifica","notifica creata");
                            break;
                        case EXPIRED:
                            Toast.makeText(NoteActivity.this, "Date/Time already passed", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                else
                {
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent intentAlarm = new Intent(NoteActivity.this, AlarmReceiver.class);
                    PendingIntent pendingUpdateIntent = PendingIntent.getBroadcast(NoteActivity.this, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.cancel(pendingUpdateIntent);
                    Log.d("321notifica","notifica eliminata");
                }
            }
        });

        //collego comandi agli spinner e imposto valori di default uguali a quelli già presenti nel db
        final ArrayAdapter<String> priorities = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,new Importance().getAllPriorities());
        prioritySpinner.setAdapter(priorities);
        prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapter, View view, int pos, long id) {
                String selected = (String)adapter.getItemAtPosition(pos).toString();
                priority[0] = java.lang.Character.getNumericValue(selected.charAt(0));
            }
            public void onNothingSelected(AdapterView<?> arg0) {}
        });

        ArrayAdapter<String> urgencies = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,new Importance().getAllUrgencies());
        urgencySpinner.setAdapter(urgencies);
        urgencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapter, View view,int pos, long id) {
                String selected = (String)adapter.getItemAtPosition(pos);
                urgency[0] = selected.charAt(0);
            }
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
        prioritySpinner.setSelection(priority[0]-1);
        urgencySpinner.setSelection(java.lang.Character.getNumericValue(urgency[0])-10); //A = 12 in ASCII, la selezione va da 0 a 2 quindi converto la lettera in un valore accettabile dallo spinner

        buttonDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c=note.getDueDate();
                c.set(Calendar.YEAR,datePicker.getYear());
                c.set(Calendar.MONTH,datePicker.getMonth());
                c.set(Calendar.DAY_OF_MONTH,datePicker.getDayOfMonth());
                c.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                c.set(Calendar.MINUTE,timePicker.getCurrentMinute());
                note.setDueDate(c);
                editTextDate.setText(note.printDueDate());
                dateTimeDialog.dismiss();
                datePicker.init(note.getDueDate().get(Calendar.YEAR),note.getDueDate().get(Calendar.MONTH),note.getDueDate().get(Calendar.DAY_OF_MONTH),null);
                //planNotification(note);
            }
        });

        buttonDismissDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateTimeDialog.dismiss();
                datePicker.init(note.getDueDate().get(Calendar.YEAR),note.getDueDate().get(Calendar.MONTH),note.getDueDate().get(Calendar.DAY_OF_MONTH),null);
                timePicker.setCurrentHour(note.getDueDate().get(Calendar.HOUR_OF_DAY));
                timePicker.setCurrentMinute(note.getDueDate().get(Calendar.MINUTE));
            }
        });
    }

    public void updateListView(NoteDBAdapter.SortingOrder sortBy) {
        Cursor cursor = noteDBAdapter.retrieveAllNotes(sortBy);
        cursor.moveToFirst();
        retrievedNotes.clear();

        while (!cursor.isAfterLast())
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
            if(cursor.getInt(cursor.getColumnIndex(NoteDBAdapter.COL_DONE))==0)
                temp.setDone(false);
            else
                temp.setDone(true);
            retrievedNotes.add(temp);
            cursor.moveToNext();
        }

        Note[] notes = retrievedNotes.toArray(new Note[retrievedNotes.size()]);
        NoteListAdapter noteListAdapter = new NoteListAdapter(this,R.layout.note_row,notes,sortBy);
        listView.setAdapter(noteListAdapter);
    }

    public void planNotification(Note note) {
        //long when = note.getDueDate().getTimeInMillis(); //TODO ripristinare
        long when = System.currentTimeMillis()+3000;
        Intent intentAlarm = new Intent(this, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("NotePayload",note);
        intentAlarm.putExtras(bundle);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, when, PendingIntent.getBroadcast(this, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }
}