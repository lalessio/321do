package com.alessio.luca.a321do;

import android.app.Dialog;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class NoteActivity extends AppCompatActivity {
    private ListView listView;
    private NoteDBAdapter noteDBAdapter;

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
//edit reminder
                        //TODO come mi ricavo la position?
                        if (position == 0) {
                            //int nId = getIdFromPosition(masterListPosition);
                            //Note nota= noteDBAdapter.fetchNoteById(nId);
                            //showNewNoteMenu(nota);
                            Toast.makeText(NoteActivity.this, "edit " + position, Toast.LENGTH_SHORT).show();
                        } else {
                            //noteDBAdapter.deleteNoteById(getIdFromPosition(masterListPosition));
                            //mCursorAdapter.changeCursor(noteDBAdapter.fetchAllNotes());
                            Toast.makeText(NoteActivity.this, "delete " + position, Toast.LENGTH_SHORT).show();
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
                //Toast.makeText(NoteActivity.this, "pressed FAB", Toast.LENGTH_SHORT).show();
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
//
//    private int getIdFromPosition(int nC) {
//        return (int)mCursorAdapter.getItemId(nC);
//    }


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
        final EditText editCustom = (EditText) dialog.findViewById(R.id.editText_title);
        LinearLayout rootLayout = (LinearLayout) dialog.findViewById(R.id.new_note_layout);
        final Button confirmButton = (Button) dialog.findViewById(R.id.button_confirm);
        final Button buttonCancel = (Button) dialog.findViewById(R.id.button_cancel);
        dialog.show();

        //collego comando ai pulsanti
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteDBAdapter.createNote(new Note(editCustom.getText().toString()));
                updateListView();
                dialog.dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_note, menu);
        return true;
    }

    public void updateListView()
    {
        Cursor cursor = noteDBAdapter.retrieveAllNotes();
        ArrayList<String> arrayAllTitles = new ArrayList<String>();
        while (cursor.isAfterLast()==false)
        {
            arrayAllTitles.add(cursor.getString(cursor.getColumnIndex("title")));
            cursor.moveToNext();
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,arrayAllTitles);
        listView.setAdapter(arrayAdapter);
    }
}