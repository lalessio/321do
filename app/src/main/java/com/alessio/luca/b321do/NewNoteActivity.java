package com.alessio.luca.b321do;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Luca on 31/10/2016.
 */

public class NewNoteActivity extends Activity {
    private NoteDBAdapter noteDBAdapter;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        noteDBAdapter = new NoteDBAdapter(this);
        setTitle(R.string.newNoteTitle);
        setContentView(R.layout.new_note_layout);

        editText = (EditText) findViewById(R.id.editText_title);
        editText.requestFocus();
        //Utilities.openKeyboard(this);

        Button confirmButton = (Button) findViewById(R.id.button_confirm);
        Button cancelButton = (Button) findViewById(R.id.button_cancel);
        Button createWithDetailsButton = (Button) findViewById(R.id.button_create_with_details);
        //ImageButton quickAttachmentButton = (ImageButton) findViewById(R.id.quickButtonAttachment);
        Button quickAttachmentButton = (Button) findViewById(R.id.quickButtonAttachment);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    performCreation();
                return true;
            }
        });

        //collego comando ai pulsanti
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               performCreation();
            }
        });
        createWithDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.closeKeyboard(NewNoteActivity.this, editText);
                Bundle bundle = new Bundle();
                bundle.putSerializable(Utilities.EDIT_NOTE_PAYLOAD_CODE,noteDBAdapter.createNote(editText.getText().toString()));
                Intent intent = new Intent(NewNoteActivity.this, EditNoteActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(0,0);
                finish();
            }
        });
        quickAttachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.closeKeyboard(NewNoteActivity.this, editText);
                Bundle bundle = new Bundle();
                bundle.putSerializable(Utilities.EDIT_NOTE_PAYLOAD_CODE,noteDBAdapter.createNote(editText.getText().toString()));
                Intent intent = new Intent(NewNoteActivity.this, EditMediaActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(0,0);
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.closeKeyboard(NewNoteActivity.this, editText);
                finish();
            }
        });
    }
    @Override
    public void onBackPressed() {
        finish();
    }
    private void performCreation() {
        if(editText.getText().toString().length()!=0)
        {
            noteDBAdapter.createNote(editText.getText().toString());
            Utilities.closeKeyboard(NewNoteActivity.this, editText);
            Toast.makeText(NewNoteActivity.this, R.string.noteCreatedMessage, Toast.LENGTH_SHORT).show();
            finish();
        }
        else
            Toast.makeText(NewNoteActivity.this, R.string.errorEmptyField, Toast.LENGTH_SHORT).show();
    }

//    private class CustomEditText extends EditText {
//
//        Context context;
//
//        public CustomEditText(Context context, AttributeSet attrs) {
//            super(context, attrs);
//            this.context = context;
//        }
//
//        @Override
//        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
//            if (keyCode == KeyEvent.KEYCODE_BACK) {
//                // User has pressed Back key. So hide the keyboard
//                InputMethodManager mgr = (InputMethodManager)
//                        context.getSystemService(Context.INPUT_METHOD_SERVICE);
//                mgr.hideSoftInputFromWindow(this.getWindowToken(), 0);
//                finish();
//            }
//            return false;
//        }}
}
