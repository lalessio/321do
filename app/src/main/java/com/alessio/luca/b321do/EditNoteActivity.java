package com.alessio.luca.b321do;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
        note = (Note) getIntent().getExtras().get(Utilities.EDIT_NOTE_PAYLOAD_CODE);
        noteDBAdapter = new NoteDBAdapter(this);

        textViewEditNoteTitle = (TextView) findViewById(R.id.textViewEditNoteTitle);
        textViewEditNoteTitle.setText(note.getTitle());
        ListView listView = (ListView) findViewById(R.id.listviewEditNoteOptions);

        String[] options = new String[] { getString(R.string.editOptionsDetails), getString(R.string.editOptionsAlarm), getString(R.string.editOptionsChecklist), getString(R.string.editOptionsMedia) };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(EditNoteActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Utilities.EDIT_NOTE_PAYLOAD_CODE,note);
                Intent intent = null;
                switch (position){
                    case 0:
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
                Bundle bundleAnimation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slide_in_left,R.anim.slide_out_left).toBundle();
                startActivity(intent,bundleAnimation);
            }
        });
    }
    @Override
    protected void onResume() {
        note = noteDBAdapter.retrieveNoteById(note.getId());
        textViewEditNoteTitle.setText(note.getTitle());
        super.onResume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }
}
