package com.alessio.luca.a321do;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

//TODO per venerdì 14: note con tutti i tipi di dato interni pensati, CRUD ecc.

public class NoteActivity extends AppCompatActivity {
    private ListView listView;
    private NoteDBAdapter noteDBAdapter;
    private ArrayList<Integer> arrayIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //TODO

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);

        listView = (ListView) findViewById(R.id.note_list_view);
        listView.setDivider(null);

        noteDBAdapter = new NoteDBAdapter(this);
        noteDBAdapter.open();
        listView = (ListView)findViewById(R.id.note_list_view);
        arrayIds = new ArrayList<Integer>();
        updateListView();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int masterListPosition, long id) {
                //creo finestra
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                ListView modeListView = new ListView(NoteActivity.this);
                String[] modes = new String[] { "Edit Note", "Delete Note" };
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(NoteActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, modes);
                modeListView.setAdapter(modeAdapter);
                builder.setView(modeListView);
                final Dialog dialog = builder.create();
                dialog.show();

                //gestico ordini
                modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            Note note = noteDBAdapter.retrieveNoteById(arrayIds.get(masterListPosition));
                            Log.d("D/Note","edit nota"+note.print());
                            showEditNoteMenu(note);
                        } else {
                            noteDBAdapter.deleteNoteById(arrayIds.get(masterListPosition));
                            updateListView();
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

//        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
//        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
//            @Override
//            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean
//                    checked) { }
//            @Override
//            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//                MenuInflater inflater = mode.getMenuInflater();
//                inflater.inflate(R.menu.cam_menu, menu);
//                return true;
//            }
//            @Override
//            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//                return false;
//            }
//            @Override
//            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.menu_item_delete_reminder:
//                        for (int nC = mCursorAdapter.getCount() - 1; nC >= 0; nC--) {
//                            if (listView.isItemChecked(nC)) {
//                                noteDBAdapter.deleteNoteById(getIdFromPosition(nC));
//                            }
//                        }
//                        mode.finish();
//                        mCursorAdapter.changeCursor(noteDBAdapter.fetchAllNotes());
//                        return true;
//                }
//                return false;
//            }
//            @Override
//            public void onDestroyActionMode(ActionMode mode) { }
//        });
//
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
//                Toast.makeText(NoteActivity.this, "nuova nota? non ancora mi spiace!", Toast.LENGTH_SHORT).show();
                showNewNoteMenu();
                return true;
            case R.id.action_exit:
                finish();
                return true;
            default:
                return false;
        }
    }

    private void showNewNoteMenu(){
        //creo la finestrella e la popolo
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("Add a new Note");
        dialog.setContentView(R.layout.dialog_new_note);
        final EditText editText = (EditText) dialog.findViewById(R.id.editText_title);
        LinearLayout linearLayout = (LinearLayout) dialog.findViewById(R.id.new_note_layout);
        final Button confirmButton = (Button) dialog.findViewById(R.id.button_confirm);
        final Button cancelButton = (Button) dialog.findViewById(R.id.button_cancel);
        dialog.show();

        //collego comando ai pulsanti
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteDBAdapter.createNote(new Note(editText.getText().toString()));
                updateListView();
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
        dialog.setTitle("Edit Note");
        dialog.setContentView(R.layout.dialog_edit_note);
        final int[] priority = {java.lang.Character.getNumericValue(note.getImportance().translate().charAt(0))};
        final char[] urgency = {note.getImportance().translate().charAt(1)};

        final EditText editText1 = (EditText) dialog.findViewById(R.id.editText_title);
        editText1.setText(note.getTitle());
        final EditText editText2 = (EditText) dialog.findViewById(R.id.editText_description);
        editText2.setText(note.getDescription());
        final EditText editTextDate = (EditText) dialog.findViewById(R.id.editText_date);
        editTextDate.setText(note.readDueDate());

        LinearLayout linearLayout = (LinearLayout) dialog.findViewById(R.id.edit_note_layout);
        final Button dateButton = (Button) dialog.findViewById(R.id.button_date);
        final Button confirmButton = (Button) dialog.findViewById(R.id.button_confirm);
        final Button cancelButton = (Button) dialog.findViewById(R.id.button_cancel);
        final Spinner prioritySpinner = (Spinner) dialog.findViewById(R.id.spinner_priority);
        Spinner urgencySpinner = (Spinner) dialog.findViewById(R.id.spinner_urgency);

        dialog.show();

        //collego comando ai pulsanti
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(),"choose date");
                note.setDueDate(newFragment.getDateSelected());
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note newNote = new Note(note);
                newNote.setTitle(editText1.getText().toString());
                newNote.setImportance(priority[0],urgency[0]);
                newNote.setDescription(editText2.getText().toString());
                noteDBAdapter.updateNote(newNote);
                updateListView();
                dialog.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        private Calendar dateSelected = new GregorianCalendar();
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, 2080, month, day);
        }
        public void onDateSet(DatePicker view, int year, int month, int day) {
            dateSelected.set(view.getYear(),view.getMonth(),view.getDayOfMonth());
        }
        public Calendar getDateSelected() {//da chiamare solo dopo che onDateSet è avvenuto altrimenti esplode tutto
            return dateSelected;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_note, menu);
        return true;
    }

    public void updateListView() //TODO implementare riordino (cosi non funziona se cambio l'ordine)
    {
        Cursor cursor = noteDBAdapter.retrieveAllNotes();
        ArrayList<String> arrayAllTitles = new ArrayList<String>();
        arrayIds.clear();
        while (cursor.isAfterLast()==false)
        {
            //TODO eliminare istruzioni debug
            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(cursor.getLong(cursor.getColumnIndex("dueDate")));
            String data = new String (cal.get(cal.YEAR)+" "+cal.get(cal.MONTH)+" "+cal.get(cal.DAY_OF_MONTH)+"");
            arrayAllTitles.add(cursor.getString(cursor.getColumnIndex("title"))+data);
            arrayIds.add(cursor.getInt(cursor.getColumnIndex("id")));
            cursor.moveToNext();
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,arrayAllTitles);
        listView.setAdapter(arrayAdapter);
    }
}