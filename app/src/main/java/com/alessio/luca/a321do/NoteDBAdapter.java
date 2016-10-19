package com.alessio.luca.a321do;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Luca on 27/09/2016.
 */

// TODO 4 CHECKLIST
    //TODO barra ricerca
        // TODO 6 NOTIFICHE
            // TODO 7 MEDIA + PLACE
                //TODO suddivisione tripla menu iniziale fatte|tra  poco|futuro personalizzabile

public class NoteDBAdapter {
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION="description";
    public static final String COL_TAG="tag";
    public static final String COL_CHECKLIST="checkList";
    public static final String COL_DUEDATE="dueDate";
    public static final String COL_IMPORTANCE="importance";
    public static final String COL_DONE="done";
    public static final String COL_ALARM="alarm";

    //enumerazione dedicata all'ordinamento della view
    public enum SortingOrder {NONE,DUEDATE,IMPORTANCE,CATEGORY};

    public static final String DEBUG_TAG = "321NoteDBAdapter";
    public static final String DATABASE_NAME = "321dodbtest_3.db";
    public static final String TABLE_NAME = "notes";
    public static final int DATABASE_VERSION = 1;

    private DatabaseHelper dbHelper;

    public NoteDBAdapter(Context ctx) {
        //this.context = ctx;
        dbHelper = new DatabaseHelper(ctx);
    }

    //CREATE
    public Note createNote(Note note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //prima salvo tutti i valori
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, note.getTitle());
        values.put(COL_DESCRIPTION, note.getDescription());
        values.put(COL_TAG, note.getTag());
//        values.put(COL_CHECKLIST, note.convertListToString(note.getCheckList())); //TODO correggere
        values.put(COL_IMPORTANCE,note.getImportance().toString());
        values.put(COL_DUEDATE,note.getDueDate().getTimeInMillis());
        values.put(COL_DONE,note.isDone()?1:0);
        values.put(COL_ALARM,note.isAlarmOn()?1:0);
        db.insert(TABLE_NAME, null, values);

        //poi restituisco un nuovo oggetto identico a quello passato ma con id aggiornato
        Note newNote = new Note(note);
        Cursor cursor = db.rawQuery("SELECT MAX(id) FROM "+TABLE_NAME,null);
        cursor.moveToFirst();
        db.close();
        newNote.setId(cursor.getInt(0)); //è la colonna che contiene l'id
        Log.d(DEBUG_TAG,"created new note: "+newNote.print());
        return newNote;
    }

    //READ
    public Note retrieveNoteById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COL_ID,
                        COL_TITLE,
                        COL_DESCRIPTION,
                        COL_TAG,
                        COL_CHECKLIST,
                        COL_IMPORTANCE,
                        COL_DUEDATE,
                        COL_DONE,
                        COL_ALARM},
                COL_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null
        );

        Note note = new Note();

        if(cursor.moveToFirst())
        {
            int nId = cursor.getInt(cursor.getColumnIndex(COL_ID));
            String nTitle = cursor.getString(cursor.getColumnIndex(COL_TITLE));
            String nDescription = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION));
            String nTag = cursor.getString(cursor.getColumnIndex(COL_TAG));
            ArrayList<String> nCheckList = new ArrayList<String>();/*Note.convertStringToList(cursor.getString(cursor.getColumnIndex(COL_CHECKLIST)))*/
            Calendar nDueDate = new GregorianCalendar();
            nDueDate.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COL_DUEDATE)));
            Importance nImportance = new Importance(cursor.getString(cursor.getColumnIndex(COL_IMPORTANCE)));
            note = new Note(nId, nTitle, nDescription, nTag, nCheckList, nDueDate, nImportance);
            note.setDone(cursor.getInt(cursor.getColumnIndex(COL_DONE)) != 0);
            note.setAlarm(cursor.getInt(cursor.getColumnIndex(COL_ALARM)) != 0);
            Log.d(DEBUG_TAG, "retrieved note: " + note.print());
        }
        else
        {
            Log.d(DEBUG_TAG,"nessuna nota recuperata :(");
        }

        cursor.close();
        db.close();
        return note;
    }
    public Cursor retrieveAllNotes(SortingOrder sortBy) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sorting = " order by "+COL_DONE;
        switch (sortBy) {
            case DUEDATE:
                sorting = sorting+", "+COL_DUEDATE;
                break;
            case IMPORTANCE:
                sorting = sorting+", "+COL_IMPORTANCE;
                break;
            case CATEGORY:
                sorting = sorting+", "+COL_TAG+", "+COL_ID;
                break;
            default: //che sarebbe il case NONE e quindi CREATIONDATE
                sorting = sorting+", "+COL_ID;
                break;
        }

        Cursor c = db.rawQuery("select * from " + TABLE_NAME + sorting, null);
        c.moveToFirst();
        Log.d(DEBUG_TAG,"all notes retrieved from db correctly");
        return c;
    }

    //UPDATE
    public void updateNote(Note note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_TITLE, note.getTitle());
        values.put(COL_DESCRIPTION, note.getDescription());
        values.put(COL_TAG, note.getTag());
        values.put(COL_CHECKLIST, note.convertListToString(note.getCheckList())); //TODO correggere
        values.put(COL_IMPORTANCE, note.getImportance().toString());
        values.put(COL_DUEDATE,note.getDueDate().getTimeInMillis());
        values.put(COL_ALARM,note.isAlarmOn());
        // l'aggiornamento del campo done è gestito da tickNote()
        db.update(TABLE_NAME, values, COL_ID + "=?", new String[]{String.valueOf(note.getId())});
        Log.d(DEBUG_TAG,"updated note to values: "+note.print());

        db.close();
    }
    public boolean tickNote(Note note){ //se una nota era da completare la completo e se era completata la "scompleto"
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_DONE,!note.isDone());
        db.update(TABLE_NAME, values, COL_ID + "=?", new String[]{String.valueOf(note.getId())});
        note.setDone(!note.isDone()); //forse questa istruzione non ha side effect ma per ora non importa
        db.close();

        return note.isDone();
    }

    //DELETE
    public void deleteNote(Note note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(note.getId())});
        db.close();
    }
    public void deleteAllNotes() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }
}
