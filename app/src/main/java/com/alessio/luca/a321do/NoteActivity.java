package com.alessio.luca.a321do;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.List;

//TODO spezzare activities e fragments

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
                                EditNoteDialog editNoteDialog = new EditNoteDialog(NoteActivity.this,noteDBAdapter.retrieveNoteById(retrievedNotes.get(masterListPosition).getId()));
                                editNoteDialog.show();
                                editNoteDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        updateListView(currentOrder);
                                    }
                                });
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
                NewNoteDialog dialog = new NewNoteDialog(NoteActivity.this);
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        updateListView(currentOrder);
                    }
                });
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
                NewNoteDialog dialog = new NewNoteDialog(this);
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        updateListView(currentOrder);
                    }
                });
                return true;
            case R.id.action_exit:
                finish();
                return true;
            case R.id.action_settings:
                Toast.makeText(NoteActivity.this, "settings TODO", Toast.LENGTH_SHORT).show(); //TODO
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
    private void updateListView(NoteDBAdapter.SortingOrder sortBy) {
        Cursor cursor = noteDBAdapter.retrieveAllNotes(sortBy);
        cursor.moveToFirst();
        retrievedNotes.clear();
//TODO casini con la checklist ancora da risolvere
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
            ArrayList<String> nCheckList = Note.stringToCheckList(cursor.getString(cursor.getColumnIndex(NoteDBAdapter.COL_CHECKLIST)));
            temp.setCheckList(nCheckList);
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
}