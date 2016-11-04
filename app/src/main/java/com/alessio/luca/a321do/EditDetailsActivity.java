package com.alessio.luca.a321do;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Created by Luca on 26/10/2016.
 */

public class EditDetailsActivity extends Activity {
    private Note note;
    private NoteDBAdapter noteDBAdapter;
    private EditText editTextTitle, editTextDesc, editTextLength;
    private AutoCompleteTextView autoCompleteTag;
    private int[] priority;
    private char[] urgency;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        note = (Note) getIntent().getExtras().get(Utilities.EDIT_NOTE_PAYLOAD_CODE);
        noteDBAdapter = new NoteDBAdapter(this);

        setTitle(note.getTitle());
        setContentView(R.layout.details_layout);

        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextTitle.setText(note.getTitle());
        editTextDesc = (EditText) findViewById(R.id.editTextDescription);
        editTextDesc.setText(note.getDescription());
        editTextLength = (EditText) findViewById(R.id.editTextLength);
        if(note.getLength()!=0)
            editTextLength.setText(String.valueOf(note.getLength()));

        autoCompleteTag = (AutoCompleteTextView) findViewById(R.id.autoCompleteTag);
        autoCompleteTag.setText(note.getTag());
        autoCompleteTag.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, NoteActivity.getExistingTags()));

        Spinner prioritySpinner = (Spinner) findViewById(R.id.spinner_priority);
        Spinner urgencySpinner = (Spinner) findViewById(R.id.spinner_urgency);

        priority = new int[]{Character.getNumericValue(note.getImportance().toString().charAt(0))};
        urgency = new char[]{note.getImportance().toString().charAt(1)};

        new Importance();
        ArrayAdapter<String> priorities = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Importance.getAllPriorities());
        prioritySpinner.setAdapter(priorities);
        prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapter, View view, int pos, long id) {
                String selected = adapter.getItemAtPosition(pos).toString();
                priority[0] = java.lang.Character.getNumericValue(selected.charAt(0));
            }
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
        prioritySpinner.setSelection(priority[0]-1);

        ArrayAdapter<String> urgencies = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Importance.getAllUrgencies());
        urgencySpinner.setAdapter(urgencies);
        urgencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapter, View view,int pos, long id) {
                String selected = (String)adapter.getItemAtPosition(pos);
                urgency[0] = selected.charAt(0);
            }
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
        urgencySpinner.setSelection(java.lang.Character.getNumericValue(urgency[0])-10); //A = 12 in ASCII, la selezione va da 0 a 2 quindi converto la lettera in un valore accettabile dallo spinner
    }
    @Override
    protected void onPause() {
        note.setTitle(editTextTitle.getText().toString());
        note.setImportance(priority[0], urgency[0]);
        note.setDescription(editTextDesc.getText().toString());
        if(editTextLength.getText().toString().length()>0)
            note.setLength(Integer.parseInt(editTextLength.getText().toString()));
        else
            note.setLength(0);
        note.setTag(autoCompleteTag.getText().toString());
        noteDBAdapter.updateNote(note);
        super.onPause();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }
}
