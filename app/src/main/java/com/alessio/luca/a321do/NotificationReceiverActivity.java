package com.alessio.luca.a321do;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Luca on 11/10/2016.
 */

public class NotificationReceiverActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switch (getIntent().getAction()){
            case AlarmReceiver.COMPLETE_NOTE:
                Toast.makeText(this,"TICK da implementare",Toast.LENGTH_LONG).show();
                Log.d("321NRA",AlarmReceiver.COMPLETE_NOTE);
                break;

            case AlarmReceiver.OPEN_NOTIFICATION:
                setContentView(R.layout.result);
                TextView t = (TextView) findViewById(R.id.textViewResult);
                Button b = (Button) findViewById(R.id.buttonResult);
                final Note note = (Note) getIntent().getExtras().get("NotePayload");
                t.setText(note.print());
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NoteDBAdapter noteDBAdapter = new NoteDBAdapter(NotificationReceiverActivity.this);
                        noteDBAdapter.tickNote(note);
                        Intent intent = new Intent(NotificationReceiverActivity.this, NoteActivity.class);
                        startActivity(intent);
                    }
                });
                Log.d("321NRA",AlarmReceiver.OPEN_NOTIFICATION);
                break;

            case AlarmReceiver.CLOSE_NOTIFICATION:
                Toast.makeText(this,"dismiss non va per il momento",Toast.LENGTH_LONG).show();
                Log.d("321NRA",AlarmReceiver.COMPLETE_NOTE);
                break;

            case AlarmReceiver.SNOOZE_NOTE:
                Toast.makeText(this,"Snooze TODO",Toast.LENGTH_LONG).show();
                Log.d("321NRA",AlarmReceiver.SNOOZE_NOTE);
                break;

            default:
                Log.w("321NRA","default should never verify");
                break;
        }

    }
}
