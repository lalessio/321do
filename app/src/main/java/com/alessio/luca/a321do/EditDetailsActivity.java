package com.alessio.luca.a321do;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Created by Luca on 26/10/2016.
 */

public class EditDetailsActivity extends Activity {
    private Note note;
    private NoteDBAdapter noteDBAdapter;
    private EditText editTextTitle, editTextDesc, editTextTag;
    private int[] priority;
    private char[] urgency;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        note = (Note) getIntent().getExtras().get("EditNotePayload");
        noteDBAdapter = new NoteDBAdapter(this);

        setTitle(note.getTitle());
        setContentView(R.layout.dialog_details);

        editTextTitle = (EditText) findViewById(R.id.editText_title);
        editTextTitle.setText(note.getTitle());
        editTextDesc = (EditText) findViewById(R.id.editText_description);
        editTextDesc.setText(note.getDescription());
        editTextTag = (EditText) findViewById(R.id.editText_tag);
        editTextTag.setText(note.getTag());


        Spinner prioritySpinner = (Spinner) findViewById(R.id.spinner_priority);
        Spinner urgencySpinner = (Spinner) findViewById(R.id.spinner_urgency);

        //TODO migliorare salvataggio importance

        priority = new int[]{Character.getNumericValue(note.getImportance().toString().charAt(0))};
        urgency = new char[]{note.getImportance().toString().charAt(1)};

        ArrayAdapter<String> priorities = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new Importance().getAllPriorities());
        prioritySpinner.setAdapter(priorities);
        prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapter, View view, int pos, long id) {
                String selected = adapter.getItemAtPosition(pos).toString();
                priority[0] = java.lang.Character.getNumericValue(selected.charAt(0));
            }
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
        prioritySpinner.setSelection(priority[0]-1);

        ArrayAdapter<String> urgencies = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new Importance().getAllUrgencies());
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
        note.setTag(editTextTag.getText().toString());
        noteDBAdapter.updateNote(note);
        super.onPause();
    }
}
