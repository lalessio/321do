package com.alessio.luca.b321do;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

        note = (Note) getIntent().getExtras().get(Utilities.EDIT_NOTE_PAYLOAD_CODE);
        noteDBAdapter = new NoteDBAdapter(this);

        setTitle(R.string.checkListTitle);
        setContentView(R.layout.checklist_layout);

        editTextCheckList = (EditText) findViewById(R.id.editTextCheckList);
        listViewCheckList = (ListView) findViewById(R.id.checklist_list_view);
        updateCheckListView();

        editTextCheckList.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                    if(editTextCheckList.getText().toString().length()>=1)
                    {
                        note.addToCheckList(editTextCheckList.getText().toString());
                        editTextCheckList.setText("");
                        updateCheckListView();
                        editTextCheckList.requestFocus();
                    }
                    else
                        Toast.makeText(EditCheckListActivity.this,R.string.errorEmptyField,Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        listViewCheckList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int masterListPosition, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(EditCheckListActivity.this);
                ListView modeListView = new ListView(EditCheckListActivity.this);
                String[] modes = new String[] {getString(R.string.deleteOptionCheckListText)};
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
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }
    private void updateCheckListView(){
        ArrayAdapter<String> checkListAdapter = new ArrayAdapter<String>(EditCheckListActivity.this,android.R.layout.simple_list_item_1,note.getCheckList());
        listViewCheckList.setAdapter(checkListAdapter);
        TextView textView = (TextView) findViewById(R.id.emptyCheckList);
        if(checkListAdapter.isEmpty())
            textView.setText(R.string.emptyCheckListMessage);
        else
            textView.setText("");
    }
}
