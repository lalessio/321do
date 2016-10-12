package com.alessio.luca.a321do;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Luca on 27/09/2016.
 */

// TODO 4 CHECKLIST

        // TODO 6 NOTIFICHE
            // TODO 7 MEDIA + PLACE

public class NoteDBAdapter {
    //these are the column names
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION="description";
    public static final String COL_TAG="tag";
//    public static final String COL_CHECKLIST="checkList";
    public static final String COL_DUEDATE="dueDate";
    public static final String COL_IMPORTANCE="importance";
    public static final String COL_DONE="done";

    //these are the corresponding indices
    public static final int INDEX_ID = 0;
    public static final int INDEX_TITLE = INDEX_ID + 1;
    public static final int INDEX_DESCRIPTION = INDEX_ID + 2;
    public static final int INDEX_TAG = INDEX_ID + 3;
    public static final int INDEX_CHECKLIST = INDEX_ID + 4;
    public static final int INDEX_DUEDATE = INDEX_ID + 5;
    public static final int INDEX_IMPORTANCE = INDEX_ID + 6;
    public static final int INDEX_DONE = INDEX_ID + 7;

    //enumerazione dedicata all'ordinamento della view
    public enum SortingOrder {NONE,DUEDATE,IMPORTANCE,CATEGORY};

    //used for logging
    private static final String DEBUG_TAG = "NoteDBAdapter";
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private static final String DATABASE_NAME = "321do_db_test_4";
    private static final String TABLE_NAME = "table_notes";
    private static final int DATABASE_VERSION = 1;
    private final Context context;
    //SQL statement used to create the database
    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + TABLE_NAME + " ( " +
                    COL_ID + " INTEGER PRIMARY KEY autoincrement, " + //semplice id
                    COL_TITLE + " TEXT, " + //semplice stringa no problem
                    COL_DESCRIPTION + " TEXT, " + //semplice stringa no problem
                    COL_TAG + " TEXT, " + //semplice stringa no problem (per ora)
//                    COL_CHECKLIST + " TEXT, " + //come salvo arraylist?
                    COL_DUEDATE + " INTEGER, " + //converto a long al momento del salvataggio (INTEGER non ha problemi a memorizzare long quindi non perdo cifre)
                    COL_IMPORTANCE + " TEXT, " +
                    COL_DONE + " INTEGER);"; //booleano visto come 0 o 1

    public NoteDBAdapter(Context ctx) {
        this.context = ctx;
    }

    //open
    public void open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    //close
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    //CREATE
    public Note createNote(Note note) {
        //prima salvo tutti i valori
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, note.getTitle());
        values.put(COL_DESCRIPTION, note.getDescription());
        values.put(COL_TAG, note.getTag());
//        values.put(COL_CHECKLIST, note.getCheckList().toString()); //TODO correggere
        values.put(COL_IMPORTANCE,note.getImportance().translate());
        values.put(COL_DUEDATE,note.getDueDate().getTimeInMillis());
        values.put(COL_DONE,note.isDone()?1:0);
        db.insert(TABLE_NAME, null, values);
        //poi restituisco un nuovo oggetto identico a quello passato ma con id aggiornato
        Note newNote = new Note(note);
        Cursor cursor = db.rawQuery("SELECT MAX(id) FROM "+TABLE_NAME,null);
        cursor.moveToFirst();
        newNote.setId(cursor.getInt(INDEX_ID));
        Log.d(DEBUG_TAG,"ho salvato nota: "+newNote.print());
        return newNote;
    }

    //READ
    public Note retrieveNoteById(int id) {
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COL_ID,
                        COL_TITLE,
                        COL_DESCRIPTION,
                        COL_TAG,
                        /*COL_CHECKLIST,*/
                        COL_IMPORTANCE,
                        COL_DUEDATE,
                        COL_DONE},
                COL_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null
        );

        if(cursor != null)
            cursor.moveToFirst();

        int nId = cursor.getInt(INDEX_ID);
        String nTitle = cursor.getString(cursor.getColumnIndex(COL_TITLE));
        String nDescription = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION));
        String nTag = cursor.getString(cursor.getColumnIndex(COL_TAG));
        Calendar nDueDate = new GregorianCalendar();
        nDueDate.setTimeInMillis(cursor.getLong(INDEX_DUEDATE));
        Importance nImportance = new Importance(cursor.getString(cursor.getColumnIndex(COL_IMPORTANCE)));
        Note note = new Note(nId,nTitle,nDescription,nTag,nDueDate,nImportance);
        Log.d(DEBUG_TAG,"retrieved note: "+note.print());
        return note;
    }
    public Cursor retrieveAllNotes(SortingOrder sortBy) {
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
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, note.getTitle());
        values.put(COL_DESCRIPTION, note.getDescription());
        values.put(COL_TAG, note.getTag());
//        values.put(COL_CHECKLIST, note.getCheckList().toString()); //TODO correggere
        values.put(COL_IMPORTANCE, note.getImportance().translate());
        values.put(COL_DUEDATE,note.getDueDate().getTimeInMillis());
        // l'aggiornamento del campo done è gestito da tickNote()
        db.update(TABLE_NAME, values, COL_ID + "=?", new String[]{String.valueOf(note.getId())});
    }
    public boolean tickNote(Note note){ //se una nota era da completare la completo e se era completata la "scompleto"
        ContentValues values = new ContentValues();
        values.put(COL_DONE,!note.isDone());
        db.update(TABLE_NAME, values, COL_ID + "=?", new String[]{String.valueOf(note.getId())});
        note.setDone(!note.isDone()); //forse questa istruzione non ha side effect ma per ora non importa
        return note.isDone();
    }

    //DELETE
    public void deleteNote(Note note) {
        db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(note.getId())});
    }
    public void deleteAllNotes() {
        db.delete(TABLE_NAME, null, null);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(DEBUG_TAG, DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(DEBUG_TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
