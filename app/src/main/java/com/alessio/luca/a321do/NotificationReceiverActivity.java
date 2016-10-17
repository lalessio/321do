package com.alessio.luca.a321do;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Luca on 11/10/2016.
 */

public class NotificationReceiverActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        TextView t = (TextView) findViewById(R.id.textViewResult);
        Note note = (Note) getIntent().getExtras().get("NotePayload");
        t.setText(note.print());
    }
}
