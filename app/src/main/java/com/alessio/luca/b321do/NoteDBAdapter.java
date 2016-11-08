package com.alessio.luca.b321do;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * methods are not static because whoever uses this class should always instantiate it otherwise the dbhelper is not initialized
 * Created by Luca on 27/09/2016.
 */

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
    public static final String COL_IMAGE="imgAttachment";
    public static final String COL_AUDIO="audioAttachment";
    public static final String COL_LENGTH="length";

    public static final String DEBUG_TAG = "321NoteDBAdapter";
    public static final String TABLE_NAME = "notes_table";

    private DatabaseHelper dbHelper;

    public NoteDBAdapter(Context ctx) {
        dbHelper = new DatabaseHelper(ctx);
        dbHelper.getWritableDatabase();
        dbHelper.getReadableDatabase();
    }

    //CREATE
    public Note createNote(String noteName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Note newNote = new Note();
        if(noteName!=null && !noteName.isEmpty())
            newNote.setTitle(noteName);

        //prima salvo tutti i valori
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, newNote.getTitle());
        values.put(COL_DUEDATE,newNote.getDueDate().getTimeInMillis());
        values.put(COL_IMPORTANCE,newNote.getImportance().toString());
        values.put(COL_DESCRIPTION, newNote.getDescription());
        values.put(COL_TAG, newNote.getTag());
        values.put(COL_LENGTH, newNote.getLength());
        values.put(COL_CHECKLIST, Utilities.checkListToString(newNote.getCheckList()));
        values.put(COL_DONE,newNote.isDone());
        values.put(COL_ALARM,newNote.isAlarmOn());
        values.put(COL_IMAGE,newNote.getImgBytes());
        values.put(COL_AUDIO,newNote.getAudioPath());

        db.insert(TABLE_NAME, null, values);

        //poi restituisco un nuovo oggetto identico a quello passato ma con id aggiornato
        Cursor cursor = db.rawQuery("SELECT MAX(id) FROM "+TABLE_NAME,null);
        cursor.moveToFirst();
        db.close();
        newNote.setId(cursor.getInt(0)); //è la colonna che contiene l'id
        Log.d(DEBUG_TAG,"created new note: "+newNote.print());
        return newNote;
    }
    public Note cloneNote(Note note) {
        Note clone = createNote(note.getTitle());
        clone.setDescription(note.getDescription());
        clone.setTag(note.getTag());
        clone.setLength(note.getLength());
        clone.setImportance(note.getImportance());
        clone.setCheckList(note.getCheckList());
        //dueDate, done, alarm e tutto il contenuto non testuale non viene copiato da requisiti
        updateNote(clone);
        return clone;
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
                        COL_LENGTH,
                        COL_DONE,
                        COL_ALARM,
                        COL_AUDIO,
                        COL_IMAGE},
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
            ArrayList<String> nCheckList = new ArrayList<>(Utilities.stringToCheckList(cursor.getString(cursor.getColumnIndex(COL_CHECKLIST))));
            Calendar nDueDate = new GregorianCalendar();
            nDueDate.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COL_DUEDATE)));
            int nLength = cursor.getInt(cursor.getColumnIndex(COL_LENGTH));
            Importance nImportance = new Importance(cursor.getString(cursor.getColumnIndex(COL_IMPORTANCE)));
            byte [] nImgBytes = cursor.getBlob(cursor.getColumnIndex(COL_IMAGE));
            String nAudioPath = cursor.getString(cursor.getColumnIndex(COL_AUDIO));
            note = new Note(nId, nTitle, nDescription, nTag, nCheckList, nDueDate, nImportance, nImgBytes, nLength, nAudioPath);
            note.setDone(cursor.getInt(cursor.getColumnIndex(COL_DONE)) != 0);
            note.setAlarm(cursor.getInt(cursor.getColumnIndex(COL_ALARM)) != 0);
            Log.d(DEBUG_TAG, "retrieved note: " + note.print());
        }
        else
            Log.d(DEBUG_TAG,"nessuna nota recuperata :(");

        cursor.close();
        db.close();
        return note;
    }
    public Cursor retrieveAllNotes(SortingOrder sortBy) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long midnight = calendar.getTimeInMillis();

        boolean whereClause = true;
        Log.d(DEBUG_TAG,"retrieving notes sorted by order = "+sortBy.getOrder().name()+" filter = "+sortBy.getFilter().name()+" searchparameter = "+sortBy.getSearchParameter());

        String sorting;
        switch (sortBy.getFilter()) {
            case WITH_ATTACHMENT:
                sorting = " where " + COL_IMAGE + " is not null or " + COL_AUDIO + " like '%/%' ";
                break;
            case ONLY_PLANNED:
                sorting = " where " + COL_DUEDATE + " > " + System.currentTimeMillis() + " and " + COL_DONE + " = 0 ";
                break;
            case ONLY_EXPIRED:
                sorting = " where " + COL_DUEDATE + " < " + System.currentTimeMillis() + " and " + COL_DONE + " = 0 ";
                break;
            case ONLY_COMPLETED:
                sorting = " where " + COL_DONE + " = 1";
                break;
            case TODAY:
                //in caso di risultati scorretti mettere un default in noteactivity
                sorting = " where " + COL_DUEDATE + " between " + (midnight-86400000) + " and " + midnight;
                break;
            case TOMORROW:
                sorting = " where " + COL_DUEDATE + " between " + midnight + " and " + (midnight+86400000);
                break;
            case NEXT7DAYS:
                sorting = " where " + COL_DUEDATE + " between " + midnight + " and " + (midnight+7*86400000);
                break;
            case WITH_SUB_ACTIVITIES:
                sorting = " where "+ COL_CHECKLIST + " like '%" + Utilities.LIST_SEPARATOR + "%' ";
                break;
            default:
                sorting = new String();
                whereClause = false;
                break;
        }

        Cursor c = null;

        if(sortBy.isSearchParameterSet())
        {
            if(whereClause)
                sorting = sorting + " and " + COL_TITLE + " like '%" + sortBy.getSearchParameter() + "%' " + " or " + COL_TAG + " ='" + sortBy.getSearchParameter() + "'";
            else
                sorting = " where " + COL_TITLE + " like '%" + sortBy.getSearchParameter() + "%' "
                        + " or " + COL_TAG + " ='" + sortBy.getSearchParameter() + "'";
        }

        sorting = sorting + " order by "+COL_DONE;

        switch (sortBy.getOrder())
        {
            case DUEDATE:
                sorting = sorting+", "+COL_DUEDATE;
                break;
            case IMPORTANCE:
                sorting = sorting+", "+COL_IMPORTANCE;
                break;
            case CATEGORY:
                sorting = sorting+", case when " + COL_TAG +" ='' then 2 else 1 end, "+COL_TAG+", "+COL_ID;
                break;
            default: //che sarebbe il case NONE e quindi CREATIONDATE
                sorting = sorting + ", "+COL_ID + " desc";
                break;
        }

        if(sortBy.isTagCase()) //brutal example of code'n'fix but the deadline is today so take it or leave it
            sorting = " where " + COL_TAG + " ='" + sortBy.getSearchParameter() + "' order by " + COL_DONE + ", "+COL_ID;

        c = db.rawQuery("select * from " + TABLE_NAME + sorting, null);
        c.moveToFirst();
        return c;
    }

    //UPDATE
    public void updateNote(Note note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_TITLE, note.getTitle());
        values.put(COL_DESCRIPTION, note.getDescription());
        values.put(COL_TAG, note.getTag());
        values.put(COL_CHECKLIST, Utilities.checkListToString(note.getCheckList()));
        values.put(COL_IMPORTANCE, note.getImportance().toString());
        values.put(COL_DUEDATE,note.getDueDate().getTimeInMillis());
        values.put(COL_LENGTH,note.getLength());
        values.put(COL_ALARM,note.isAlarmOn());
        values.put(COL_IMAGE,note.getImgBytes());
        values.put(COL_AUDIO,note.getAudioPath());
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
        Log.d(DEBUG_TAG,"ticked note "+note.getId()+" "+note.getTitle()+" now done = "+note.isDone());

        return note.isDone();
    }

    //DELETE
    public Note deleteNote(Note note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(note.getId())});
        db.close();
        Log.d(DEBUG_TAG,"deleted note "+note.print());
        return note;
    }
    public void deleteAllNotes() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
        Log.d(DEBUG_TAG,"ALL NOTES DELETED FROM DB");
    }
}
