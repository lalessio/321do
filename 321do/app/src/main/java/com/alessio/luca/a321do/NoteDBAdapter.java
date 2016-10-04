package com.alessio.luca.a321do;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Date;

/**
 * Created by Luca on 27/09/2016.
 */

public class NoteDBAdapter {
    //these are the column names
    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION="description";
    public static final String COL_TAG="tag";
    public static final String COL_CHECKLIST="checkList";
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

    //used for logging
    private static final String TAG = "NoteDBAdapter";
    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDB;
    private static final String DATABASE_NAME = "db_notes";
    private static final String TABLE_NAME = "table_notes";
    private static final int DATABASE_VERSION = 1;
    private final Context mCtx;
    //SQL statement used to create the database
    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + TABLE_NAME + " ( " +
                    COL_ID + " INTEGER PRIMARY KEY autoincrement, " + //semplice id
                    COL_TITLE + " TEXT, " + //semplice stringa no problem
                    COL_DESCRIPTION + " TEXT, " + //semplice stringa no problem
                    COL_TAG + " TEXT, " + //semplice stringa no problem (per ora)
                    COL_CHECKLIST + " TEXT, " + //come salvo arraylist?
                    COL_DUEDATE + " INTEGER, " + //converto a long al momento del salvataggio (INTEGER non ha problemi a memorizzare long quindi non perdo cifre)
                    COL_IMPORTANCE + " INTEGER, " + //TODO pensare a come salvare
                    COL_DONE + " INTEGER);"; //booleano visto come 0 o 1
//TODO leggere e rivedere
    public NoteDBAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    //open
    public void open() throws SQLException {
        mDBHelper = new DatabaseHelper(mCtx);
        mDB = mDBHelper.getWritableDatabase();
    }
    //close
    public void close() {
        if (mDBHelper != null) {
            mDBHelper.close();
        }
    }

    //CREATE
//note that the id will be created for you automatically


    public long createNote(Note note) {
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, note.getTitle());
        values.put(COL_DESCRIPTION, note.getDescription());
        values.put(COL_TAG, note.getTag());
//TODO finire
        values.put(COL_DUEDATE,note.getDueDate().getTimeInMillis());
// Inserting Row
        return mDB.insert(TABLE_NAME, null, values);
    }

    //READ
    public Note fetchNoteById(int id) {
        Cursor cursor = mDB.query(TABLE_NAME, new String[]{COL_ID,
                        COL_TITLE}, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null
        );
        if (cursor != null)
            cursor.moveToFirst();
        return new Note(
                cursor.getString(INDEX_TITLE)
        );
    }
    public Cursor fetchAllNotes() {
        Cursor mCursor = mDB.query(TABLE_NAME, new String[]{COL_ID,
                        COL_TITLE},
                null, null, null, null, null
        );
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    //UPDATE
    public void updateNote(Note nota) {
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, nota.getTitle());
        mDB.update(TABLE_NAME, values,
                COL_ID + "=?", new String[]{String.valueOf(nota.getId())});
    }
    //DELETE
    public void deleteNoteById(int nId) {
        mDB.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(nId)});
    }
    public void deleteAllNotes() {
        mDB.delete(TABLE_NAME, null, null);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG, DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
