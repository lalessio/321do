package com.alessio.luca.a321do;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Luca on 19/10/2016.
 */

public class NewNoteDialog extends Dialog {

    private Context context;
    private EditText editText;
    private NoteDBAdapter noteDBAdapter;


    public NewNoteDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        noteDBAdapter = new NoteDBAdapter(context);
        setTitle(R.string.newNoteTitle);
        setContentView(R.layout.dialog_new_note);

        editText = (EditText) findViewById(R.id.editText_title);
        editText.requestFocus(); //TODO non prende il focus
        Button confirmButton = (Button) findViewById(R.id.button_confirm);
        Button cancelButton = (Button) findViewById(R.id.button_cancel);

        //collego comando ai pulsanti
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.getText().toString().length()!=0)
                {
                    noteDBAdapter.createNote(new Note(editText.getText().toString()));
                    dismiss();
                }
                else
                {
                    Toast.makeText(context, R.string.errorEmptyField, Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}

