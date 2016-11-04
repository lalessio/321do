package com.alessio.luca.a321do;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.alessio.luca.a321do.NoteDBAdapter.COL_ALARM;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_AUDIO;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_CHECKLIST;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_DESCRIPTION;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_DONE;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_DUEDATE;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_ID;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_IMAGE;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_IMPORTANCE;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_LENGTH;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_TAG;
import static com.alessio.luca.a321do.NoteDBAdapter.COL_TITLE;
import static com.alessio.luca.a321do.NoteDBAdapter.TABLE_NAME;

/**
 * Created by Luca on 17/10/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    //in caso di nuove colonne: cambiare queste due stringhe, aggiungerla nel mezzo, controllare virgole
    public static final String DATABASE_NAME = "db321do006";
    public static final int DATABASE_VERSION = 8;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //Log.d(NoteDBAdapter.DEBUG_TAG,"costruttore dbhelper");
        getWritableDatabase();
        getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String DATABASE_CREATE =
                "CREATE TABLE " + TABLE_NAME + " ( " +
                        COL_ID + " INTEGER PRIMARY KEY autoincrement, " + //semplice id
                        COL_TITLE + " TEXT, " + //semplice stringa no problem
                        COL_DESCRIPTION + " TEXT, " + //semplice stringa no problem
                        COL_TAG + " TEXT, " + //semplice stringa no problem (per ora)
                        COL_CHECKLIST + " TEXT, " + //come salvo arraylist?
                        COL_DUEDATE + " INTEGER, " + //converto a long al momento del salvataggio (INTEGER non ha problemi a memorizzare long quindi non perdo cifre)
                        COL_IMPORTANCE + " TEXT, " +
                        COL_IMAGE + " BLOB, " +
                        COL_AUDIO + " TEXT, " +
                        COL_LENGTH + " INTEGER, " +
                        COL_DONE + " INTEGER, " + //booleano visto come 0 o 1
                        COL_ALARM + " INTEGER );";
        //Log.d(NoteDBAdapter.DEBUG_TAG, "evoco onCreate di DatabaseHelper");
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(NoteDBAdapter.DEBUG_TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
