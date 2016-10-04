package com.alessio.luca.a321do;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NoteActivity extends AppCompatActivity {
    private ListView mListView;
    private NoteDBAdapter mDBAdapter;
    private NoteSimpleCursorAdapter mCursorAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) { //TODO
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);
        mListView = (ListView) findViewById(R.id.reminders_list_view);
        mListView.setDivider(null);
        mDBAdapter = new NoteDBAdapter(this);
        mDBAdapter.open();
       // if (savedInstanceState == null) {
            //Clear all data
            //mDBAdapter.deleteAllNotes();
            //Add some data
            //insertSomeNote();
       // }
        Cursor cursor = mDBAdapter.fetchAllNotes();
//from columns defined in the db
        String[] from = new String[]{
                NoteDBAdapter.COL_TITLE
        };
//to the ids of views in the layout
        int[] to = new int[]{
                R.id.row_text
        };
        mCursorAdapter = new NoteSimpleCursorAdapter(NoteActivity.this, R.layout.note_row, cursor, from, to, 0);
// the cursorAdapter (controller) is now updating the listView (view)
//with data from the db (model)
        mListView.setAdapter(mCursorAdapter);
        //when we click an individual item in the listview
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int masterListPosition, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                ListView modeListView = new ListView(NoteActivity.this);
                String[] modes = new String[] { "Edit Note", "Delete Note" };
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(NoteActivity.this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, modes);
                modeListView.setAdapter(modeAdapter);
                builder.setView(modeListView);
                final Dialog dialog = builder.create();
                dialog.show();
                modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//edit reminder
                        if (position == 0) {
                            int nId = getIdFromPosition(masterListPosition);
                            Note nota= mDBAdapter.fetchNoteById(nId);
                            fireCustomDialog(nota);
//delete reminder
                        } else {
                            mDBAdapter.deleteNoteById(getIdFromPosition(masterListPosition));
                            mCursorAdapter.changeCursor(mDBAdapter.fetchAllNotes());
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
                fireCustomDialog(null);
            }
        });

        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean
                    checked) { }
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.cam_menu, menu);
                return true;
            }
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item_delete_reminder:
                        for (int nC = mCursorAdapter.getCount() - 1; nC >= 0; nC--) {
                            if (mListView.isItemChecked(nC)) {
                                mDBAdapter.deleteNoteById(getIdFromPosition(nC));
                            }
                        }
                        mode.finish();
                        mCursorAdapter.changeCursor(mDBAdapter.fetchAllNotes());
                        return true;
                }
                return false;
            }
            @Override
            public void onDestroyActionMode(ActionMode mode) { }
        });

    }

    private int getIdFromPosition(int nC) {
        return (int)mCursorAdapter.getItemId(nC);
    }

    private void insertSomeNote() {
        mDBAdapter.createNote(new Note("Impegno di prova"));
        mDBAdapter.createNote(new Note("Se vedi questi impegni"));
        mDBAdapter.createNote(new Note("Vuol dire che il db era vuoto"));
        mDBAdapter.createNote(new Note("Queste sono note di default"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
//create new Note
                fireCustomDialog(null);
                return true;
            case R.id.action_exit:
                finish();
                return true;
            default:
                return false;
        }
    }

    private void fireCustomDialog(final Note reminder){//TODO
// custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_costum);
        TextView titleView = (TextView) dialog.findViewById(R.id.textView);
        final EditText editCustom = (EditText) dialog.findViewById(R.id.editText);
        Button commitButton = (Button) dialog.findViewById(R.id.button_commit);
        LinearLayout rootLayout = (LinearLayout) dialog.findViewById(R.id.custom_root_layout);
        final boolean isEditOperation = (reminder != null);
        rootLayout.setBackgroundColor(getResources().getColor(R.color.green));
//this is for an edit
        if (isEditOperation){
            titleView.setText("Edit Note");
            editCustom.setText(reminder.getTitle());
            rootLayout.setBackgroundColor(getResources().getColor(R.color.blue));
        }
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reminderText = editCustom.getText().toString();
                if (isEditOperation) {
                    Note reminderEdited = new Note(reminderText);
                    mDBAdapter.updateNote(reminderEdited);
//this is for new reminder
                } else {

                    mDBAdapter.createNote(new Note(reminderText));
                }
                mCursorAdapter.changeCursor(mDBAdapter.fetchAllNotes());
                dialog.dismiss();
            }
        });
        Button buttonCancel = (Button) dialog.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDBAdapter.close();
    }
}