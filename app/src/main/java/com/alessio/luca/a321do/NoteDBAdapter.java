package com.alessio.luca.a321do;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * methods are not static because whoever uses this class should always instantiate it otherwise the dbhelper is not initialized
 * Created by Luca on 27/09/2016.
 */
            // TODO 7 MEDIA + PLACE

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
        Note newNote = new Note(noteName);

        //prima salvo tutti i valori
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, newNote.getTitle());
        values.put(COL_DUEDATE,newNote.getDueDate().getTimeInMillis());
        values.put(COL_IMPORTANCE,newNote.getImportance().toString());
        values.put(COL_DESCRIPTION, newNote.getDescription());
        values.put(COL_TAG, newNote.getTag());
        values.put(COL_CHECKLIST, newNote.checkListToString(newNote.getCheckList()));
        values.put(COL_DONE,newNote.isDone()?1:0);
        values.put(COL_ALARM,newNote.isAlarmOn()?1:0);
        values.put(COL_IMAGE,newNote.getImgBytes());

        db.insert(TABLE_NAME, null, values);

        //poi restituisco un nuovo oggetto identico a quello passato ma con id aggiornato
        Cursor cursor = db.rawQuery("SELECT MAX(id) FROM "+TABLE_NAME,null);
        cursor.moveToFirst();
        db.close();
        newNote.setId(cursor.getInt(0)); //è la colonna che contiene l'id
        Log.d(DEBUG_TAG,"created new note: "+newNote.print());
        return newNote;
    }
    public void cloneNote(Note note) {
        Note clone = createNote(note.getTitle());
        //clone.setTitle(note.getTitle());
        clone.setDescription(note.getDescription());
        clone.setTag(note.getTag());
        clone.setImportance(note.getImportance());
        clone.setCheckList(note.getCheckList());
        //dueDate, done, alarm e tutto il contenuto non testuale non viene copiato da requisiti
        updateNote(clone);
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
                        COL_ALARM,
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
            ArrayList<String> nCheckList = new ArrayList<>(Note.stringToCheckList(cursor.getString(cursor.getColumnIndex(COL_CHECKLIST))));
            Calendar nDueDate = new GregorianCalendar();
            nDueDate.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COL_DUEDATE)));
            Importance nImportance = new Importance(cursor.getString(cursor.getColumnIndex(COL_IMPORTANCE)));
            byte [] nImgBytes = cursor.getBlob(cursor.getColumnIndex(COL_IMAGE));
            note = new Note(nId, nTitle, nDescription, nTag, nCheckList, nDueDate, nImportance, nImgBytes);
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

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long midnight = calendar.getTimeInMillis();

        boolean whereClause = false;

        String sorting;
        switch (sortBy.getFilter()) {
            case WITH_ATTACHMENT:
                sorting = " where " + COL_IMAGE + " is not null";
                whereClause = true;
                break;
            case ONLY_PLANNED:
                sorting = " where " + COL_DUEDATE + " > " + System.currentTimeMillis() + " and " + COL_DONE + " = 0";
                whereClause = true;
                break;
            case ONLY_EXPIRED:
                sorting = " where " + COL_DUEDATE + " < " + System.currentTimeMillis() + " and " + COL_DONE + " = 0";
                whereClause = true;
                break;
            case ONLY_COMPLETED:
                sorting = " where " + COL_DONE + " = 1";
                whereClause = true;
                break;
            case TODAY:
                //TODO risolvere today tomorrow risultati scorretti
                Calendar cal = Calendar.getInstance();
                long now = cal.getTimeInMillis();
                sorting = " where " + COL_DUEDATE + " between " + now + " and " + midnight;
                whereClause = true;
                break;
            case TOMORROW:
                sorting = " where " + COL_DUEDATE + " between " + midnight + " and " + (midnight+86400000);
                whereClause = true;
                break;
            case NEXT7DAYS:
                sorting = " where " + COL_DUEDATE + " between " + midnight + " and " + (midnight+7*86400000);
                whereClause = true;
                break;
            default:
                sorting = new String();
                break;
        }
        Cursor c = null;
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
                sorting = sorting+", "+COL_TAG+", "+COL_ID;
                break;
            default: //che sarebbe il case NONE e quindi CREATIONDATE
                sorting = sorting + ", "+COL_ID;
                break;
        }
        if(sortBy.isSearchParameterSet())
        {
            if(whereClause)
            {
                sorting = sorting + " and " + COL_TITLE + " like '%" + sortBy.getSearchParameter() + "%' ";
                c = db.rawQuery("select * from " + TABLE_NAME + sorting, null);
            }
            else
                c = db.rawQuery("select * from " + TABLE_NAME + " where " + COL_TITLE + " like '%" + sortBy.getSearchParameter() + "%' " + sorting, null);
        }
        else
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
        values.put(COL_CHECKLIST, note.checkListToString(note.getCheckList())); //TODO correggere
        values.put(COL_IMPORTANCE, note.getImportance().toString());
        values.put(COL_DUEDATE,note.getDueDate().getTimeInMillis());
        values.put(COL_ALARM,note.isAlarmOn());
        values.put(COL_IMAGE,note.getImgBytes());
//        if(note.getImg()!=null)
//        {
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            note.getImg().compress(Bitmap.CompressFormat.PNG,0,out);
//            values.put(COL_IMAGE,out.toByteArray());
//        }
//        else
//        {
//            byte[] emptyBlob = new byte[0];
//            values.put(COL_IMAGE,emptyBlob);
//        }
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
