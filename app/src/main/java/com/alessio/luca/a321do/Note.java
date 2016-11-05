package com.alessio.luca.a321do;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Luca on 03/10/2016.
 */
public class Note implements Serializable {

/////////////////////////////////////////CAMPI DATO////////////////////////////////////////////////

    private int id;
    private String title;
    private String description;
    private String tag;
    private List<String> checkList;
    private Calendar dueDate;
    private Importance importance;
    private byte[] imgBytes;
    private String audioPath;
    private int length;

//////////////////////////////////////////ALTRI VALORI UTILI///////////////////////////////////////

    public enum NoteState {COMPLETED,PLANNED,EXPIRED}

    private boolean done; //true se la nota è stata completata, false altrimenti
    private boolean alarm; //true se è stato impostata la notifica, false altrimenti

//////////////////////////////////////////GET&SET//////////////////////////////////////////////////

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        if(title.length()>140)
            this.title=title.substring(0,140);
        else
            this.title = title;
    }

    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Calendar getDueDate() {
        return dueDate;
    }
    public void setDueDate(Calendar dueDate) {
        this.dueDate = dueDate;
    }

    public Importance getImportance() {
        return importance;
    }
    public void setImportance(Importance importance) {
        this.importance = importance;
    }
    public void setImportance(int priority, char urgency) {
        this.importance = new Importance(this.importance.mapToPriority(priority), this.importance.mapToUrgency(urgency));
    }

    public List<String> getCheckList() {
        checkList.remove("");
        return checkList;
    }
    public void setCheckList(List<String> checkList) {
        checkList.remove("");
        this.checkList = checkList;
    }
    public void addToCheckList(String n)
    {
        checkList.add(n);
    }
    public String removeFromCheckList(int i)
    {
        return checkList.remove(i);
    }

    public boolean isDone() {
        return done;
    }
    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isAlarmOn() {
        return alarm;
    }
    public void setAlarm(boolean alarm) {
        this.alarm = alarm;
    }

    public byte[] getImgBytes() {
        return imgBytes;
    }
    public void setImgBytes(byte[] imgBytes) {
        this.imgBytes = imgBytes;
    }

    public String getAudioPath() {
        return audioPath;
    }
    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }

////////////////////////////////////////ALTRI METODI///////////////////////////////////////////////

    public boolean isDueOver(){
    Calendar now = Calendar.getInstance();
        return getDueDate().getTimeInMillis() <= now.getTimeInMillis();
}
    public NoteState getNoteState(){
        NoteState noteState;
        if(isDone())
            noteState=NoteState.COMPLETED;
        else
        {
            if(isDueOver())
                noteState=NoteState.EXPIRED;
            else
                noteState=NoteState.PLANNED;
        }
        return noteState;
    }
    private void setStandardTime() {
        //l'ora standard è domani mattina alle 8:30
        this.dueDate=new GregorianCalendar();
        this.dueDate.add(Calendar.DAY_OF_MONTH,1);
        this.dueDate.set(Calendar.HOUR_OF_DAY,8);
        this.dueDate.set(Calendar.MINUTE,30);
        this.dueDate.set(Calendar.SECOND,0);
        this.dueDate.set(Calendar.MILLISECOND,0);
    }
    private void newNoteInitialization(){
        this.done = false;
        setStandardTime();
        this.description = new String();
        this.length = 0;
        this.tag = new String();
        this.importance = new Importance();
        this.checkList = new ArrayList<String>();
        this.audioPath = new String();
        this.alarm = false;
    }
    public String printTime(){
        String dueDate = getDueDate().get(Calendar.DAY_OF_MONTH) + " "
                + (getDueDate().getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())) + " "
                + getDueDate().get(Calendar.YEAR) + "   "
                + getDueDate().get(Calendar.HOUR_OF_DAY) + ":";
        if(getDueDate().get(Calendar.MINUTE)<10)
            dueDate = dueDate + "0";
        dueDate = dueDate + getDueDate().get(Calendar.MINUTE);
        if(length!=0)
            dueDate = dueDate+"   ~ "+length+" minutes";
        return dueDate;
    }

///////////////////////////////////////METODI DEBUG////////////////////////////////////////////////

    public String print() {
        return getId() + " / "
                + getTitle() + " / "
                + getDescription() + " / "
                + getTag() + " / "
                + printTime() + " / "
                + getLength() + " minutes / "
                + getImportance().toString() + " / "
                + Utilities.checkListToString(checkList) + " / "
                + " / done = " + isDone()
                + " / alarm = " + isAlarmOn();
    }

///////////////////////////////////////////////////////////////////////////////////////////////////

    public Note(String title) {
        this.title = title;
        newNoteInitialization();
    }
    public Note(int id, String title) {
        this.id = id;
        this.title = title;
        newNoteInitialization();
    }
    public Note(){
        Calendar now = new GregorianCalendar();
        this.title = now.getTime().toString(); //titolo default momento creazione
        newNoteInitialization();
    }
    public Note(int nId, String nTitle, String nDescription, String nTag, ArrayList<String> nCheckList, Calendar nDueDate, Importance nImportance, byte[] nImgBytes, int nLength, String nAudioPath){
        this.id=nId;
        this.title=nTitle;
        this.description=nDescription;
        this.tag=nTag;
        this.checkList=nCheckList;
        this.dueDate=nDueDate;
        this.importance=nImportance;
        this.imgBytes=nImgBytes;
        this.length=nLength;
        this.audioPath=nAudioPath;
    }
    public Note(Note note){
        //abbozzo costruttore copia
        this.id = note.getId();
        this.title = note.getTitle();
        this.description = note.getDescription();
        this.tag = note.getTag();
        this.checkList = note.getCheckList();
        this.dueDate = note.getDueDate();
        this.importance = note.getImportance();
        this.done=note.isDone();
        this.alarm=note.isAlarmOn();
        this.imgBytes=note.getImgBytes();
        this.length=note.getLength();
        this.audioPath=note.getAudioPath();
    }
}
