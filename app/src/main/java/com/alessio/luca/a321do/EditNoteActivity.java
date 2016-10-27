package com.alessio.luca.a321do;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Luca on 25/10/2016.
 */

public class EditNoteActivity extends Activity {
    private Note note;
    private NoteDBAdapter noteDBAdapter;
    private TextView textViewEditNoteTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_menu_layout);
        note = (Note) getIntent().getExtras().get("EditNotePayload");
        noteDBAdapter = new NoteDBAdapter(this);

        textViewEditNoteTitle = (TextView) findViewById(R.id.textViewEditNoteTitle);
        textViewEditNoteTitle.setText(note.getTitle());
        ListView listView = (ListView) findViewById(R.id.listviewEditNoteOptions);

        String[] options = new String[] { getString(R.string.editOptionsDetails), getString(R.string.editOptionsAlarm), getString(R.string.editOptionsChecklist), getString(R.string.editOptionsMedia) };
        ArrayAdapter<String> arraydapter = new ArrayAdapter<>(EditNoteActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, options);
        listView.setAdapter(arraydapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("EditNotePayload",note);
                Intent intent = null;
                switch (position){
                    case 0:
                        //bundle.putSerializable("TagsPayload",(String []) getIntent().getExtras().get("TagsPayload"));
                        intent = new Intent(EditNoteActivity.this, EditDetailsActivity.class);
                        break;
                    case 1:
                        intent = new Intent(EditNoteActivity.this, EditDateTimeActivity.class);
                        break;
                    case 2:
                        intent = new Intent(EditNoteActivity.this, EditCheckListActivity.class);
                        break;
                    case 3:
                        intent = new Intent(EditNoteActivity.this, EditMediaActivity.class);
                        break;
                    default:
                        break;
                }
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(EditNoteActivity.this, R.string.messageChangesApplied,Toast.LENGTH_SHORT).show();
        overridePendingTransition(0,0);
    }
    public void planNotification(Note note) {
        long when = note.getDueDate().getTimeInMillis();
        //long when = System.currentTimeMillis()+3000; //for debug
        Intent intentAlarm = new Intent(EditNoteActivity.this, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("NotePayload",note);
        intentAlarm.putExtras(bundle);
        AlarmManager alarmManager = (AlarmManager) EditNoteActivity.this.getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, when, PendingIntent.getBroadcast(EditNoteActivity.this, note.getId(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    @Override
    protected void onResume() {
        note = noteDBAdapter.retrieveNoteById(note.getId());
        textViewEditNoteTitle.setText(note.getTitle());
        super.onResume();
    }
}
