package com.alessio.luca.a321do;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Luca on 25/10/2016.
 */

public class EditNoteActivity2 extends Activity {
    private Note note;
    private NoteDBAdapter noteDBAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_menu);
        note = (Note) getIntent().getExtras().get("EditNotePayload");
        noteDBAdapter = new NoteDBAdapter(this);

        TextView textViewEditNoteTitle = (TextView) findViewById(R.id.textViewEditNoteTitle);
        textViewEditNoteTitle.setText(note.getTitle());
        ListView listView = (ListView) findViewById(R.id.listviewEditNoteOptions);
        Button buttonSave = (Button) findViewById(R.id.button_save);
        Button buttonDismiss = (Button) findViewById(R.id.button_annulla);

        String[] options = new String[] { getString(R.string.editOptionsDetails), getString(R.string.editOptionsAlarm), getString(R.string.editOptionsChecklist), getString(R.string.editOptionsMedia) };
        ArrayAdapter<String> arraydapter = new ArrayAdapter<>(EditNoteActivity2.this, android.R.layout.simple_list_item_1, android.R.id.text1, options);
        listView.setAdapter(arraydapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        DetailsDialog detailsDialog = new DetailsDialog(EditNoteActivity2.this,note);
                        detailsDialog.show();
                        break;
                    case 1:
                        DateTimeDialog dateTimeDialog = new DateTimeDialog(EditNoteActivity2.this,note);
                        dateTimeDialog.show();
                        break;
                    case 2:
                        CheckListDialog checkListDialog = new CheckListDialog(EditNoteActivity2.this,note);
                        checkListDialog.show();
                        break;
                    case 3:
                        Toast.makeText(EditNoteActivity2.this,"Media TODO",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteDBAdapter.updateNote(note);
                if (note.isAlarmOn() && note.getNoteState() == Note.NoteState.PLANNED)
                    planNotification(note);
                Toast.makeText(EditNoteActivity2.this, R.string.messageChangesApplied, Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(0,0);
            }
        });

        buttonDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EditNoteActivity2.this, R.string.messageChangesNotApplied,Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(0,0);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(EditNoteActivity2.this, R.string.messageChangesNotApplied,Toast.LENGTH_SHORT).show();
        overridePendingTransition(0,0);
    }

    public void planNotification(Note note) {
        long when = note.getDueDate().getTimeInMillis();
        //long when = System.currentTimeMillis()+3000; //for debug
        Intent intentAlarm = new Intent(EditNoteActivity2.this, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("NotePayload",note);
        intentAlarm.putExtras(bundle);
        AlarmManager alarmManager = (AlarmManager) EditNoteActivity2.this.getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, when, PendingIntent.getBroadcast(EditNoteActivity2.this, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }
}
