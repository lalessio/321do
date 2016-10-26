package com.alessio.luca.a321do;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by Luca on 26/10/2016.
 */

public class EditCheckListActivity extends Activity {
    private Note note;
    private NoteDBAdapter noteDBAdapter;
    private EditText editTextCheckList;
    private ListView listViewCheckList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        note = (Note) getIntent().getExtras().get("EditNotePayload");
        noteDBAdapter = new NoteDBAdapter(this);

        setTitle(R.string.checkListTitle);
        setContentView(R.layout.dialog_checklist);

        editTextCheckList = (EditText) findViewById(R.id.editTextCheckList);
        Button buttonAddCheckListItem = (Button) findViewById(R.id.buttonCheckListAdd);
        listViewCheckList = (ListView) findViewById(R.id.checklist_list_view);
        updateCheckListView();

        buttonAddCheckListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextCheckList.getText().toString().length()>=1)
                {
                    note.addToCheckList(editTextCheckList.getText().toString());
                    editTextCheckList.setText("");
                    updateCheckListView();
                }
                else
                    Toast.makeText(EditCheckListActivity.this,R.string.errorEmptyField,Toast.LENGTH_SHORT).show();
            }
        });

        listViewCheckList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int masterListPosition, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(EditCheckListActivity.this);
                ListView modeListView = new ListView(EditCheckListActivity.this);
                String[] modes = new String[] {"Delete"};
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(EditCheckListActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, modes);
                modeListView.setAdapter(modeAdapter);
                builder.setView(modeListView);
                final Dialog dialog = builder.create();
                dialog.show();

                //gestico ordini
                modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        note.removeFromCheckList(masterListPosition);
                        updateCheckListView();
                        dialog.dismiss();
                    }
                });
            }
        });
    }
    @Override
    protected void onPause() {
        noteDBAdapter.updateNote(note);
        super.onPause();
    }
    private void updateCheckListView(){
        ArrayAdapter<String> checkListAdapter = new ArrayAdapter<String>(EditCheckListActivity.this,android.R.layout.simple_list_item_1,note.getCheckList());
        listViewCheckList.setAdapter(checkListAdapter);
    }
}
