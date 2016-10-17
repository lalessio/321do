package com.alessio.luca.a321do;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.alessio.luca.a321do.NoteDBAdapter.COL_ALARM;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_CHECKLIST;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_DESCRIPTION;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_DONE;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_DUEDATE;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_ID;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_IMPORTANCE;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_TAG;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_TITLE;
import static com.alessio.luca.a321do.NoteDBAdapter.TABLE_NAME;

/**
 * Created by Luca on 17/10/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    DatabaseHelper(Context context) {
        super(context, NoteDBAdapter.DATABASE_NAME, null, NoteDBAdapter.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String DATABASE_CREATE =
                "CREATE TABLE if not exists " + TABLE_NAME + " ( " +
                        COL_ID + " INTEGER PRIMARY KEY autoincrement, " + //semplice id
                        COL_TITLE + " TEXT, " + //semplice stringa no problem
                        COL_DESCRIPTION + " TEXT, " + //semplice stringa no problem
                        COL_TAG + " TEXT, " + //semplice stringa no problem (per ora)
                        COL_CHECKLIST + " TEXT, " + //come salvo arraylist?
                        COL_DUEDATE + " INTEGER, " + //converto a long al momento del salvataggio (INTEGER non ha problemi a memorizzare long quindi non perdo cifre)
                        COL_IMPORTANCE + " TEXT, " +
                        COL_DONE + " INTEGER, " + //booleano visto come 0 o 1
                        COL_ALARM + " INTEGER );"; //booleano visto come 0 o 1
        Log.w(NoteDBAdapter.DEBUG_TAG, "evoco onCreate di DatabaseHelper");
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(NoteDBAdapter.DEBUG_TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
