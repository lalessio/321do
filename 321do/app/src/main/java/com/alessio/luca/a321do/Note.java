package com.alessio.luca.a321do;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Luca on 03/10/2016.
 */
public class Note {

/////////////////////////////////////////CAMPI DATO////////////////////////////////////////////////

    private int id;
    private String title;
    private String description;
    private String tag;
    //private ArrayList<String> checkList; //TODO sistemare checkList
    private Calendar dueDate;
    private Importance importance;

//////////////////////////////////////////TODO/////////////////////////////////////////////////////

    //private ArrayList<Note> childs; //facciamo secondario per ora
    //private Place place;
    //private Alarm reminder; //faccio io ma come?
    //private MediaAttachment mediaAttachment;
    //private Length length; //durata appuntamento, requisito secondario

//////////////////////////////////////////ALTRI VALORI UTILI///////////////////////////////////////

    private static final String TAG = "Note";
    public enum NoteState {COMPLETED,PLANNED,EXPIRED};
    private boolean done; //true se la nota è stata completata, false altrimenti

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

//    public ArrayList<String> getCheckList() {
//        return checkList;
//    }
//    public void setCheckList(ArrayList<String> checkList){
//        this.checkList=checkList;
//    }

    public boolean isDone() {
        return done;
    }
    public void setDone(boolean done) {
        this.done = done;
    }

    ////////////////////////////////////////ALTRI METODI///////////////////////////////////////////////

//    public void addToCheckList(String n)
//    {
//        checkList.add(n);
//    }
//    public String removeFromCheckList(int i)
//    {
//        return checkList.remove(i);
//    }

    public boolean isDueOver(){
        Calendar now = new GregorianCalendar();
        if(this.dueDate.compareTo(now)>0)
            return false;
        else
            return true;
    }
    public NoteState getNoteState(){
        NoteState noteState;
        if(isDone())
            noteState=NoteState.COMPLETED;
        else
        {
            if(!isDueOver())
                noteState=NoteState.PLANNED;
            else
                noteState=NoteState.EXPIRED;
        }
        return noteState;
    }
    private void setStandardTime() {
        //l'ora standard è domani mattina alle 8:30
        this.dueDate=new GregorianCalendar();
        this.dueDate.add(Calendar.DAY_OF_MONTH,1);
        this.dueDate.set(Calendar.HOUR,8);
        this.dueDate.set(Calendar.MINUTE,30);
    }
    private void newNoteInitialization(){
        this.done = false;
        setStandardTime();
        this.tag="none";
        this.description = new String();
        this.importance = new Importance();
    }
    public String print() {
        return getId()+" / "+getTitle()+" / "+getDescription()+" / "+getTag()+" / "+readDueDate()+" / "+getImportance().translate();
    }
    public String readDueDate(){
        return new String(getDueDate().get(Calendar.YEAR)+"/"+(getDueDate().get(Calendar.MONTH)+1)+"/"+getDueDate().get(Calendar.DAY_OF_MONTH));
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

    //TODO costruttore con tutti i parametri completi dopo che sono stati letti dal DB
    public Note(int nId, String nTitle, String nDescription, String nTag, Calendar nDueDate, Importance nImportance){
        this.id=nId;
        this.title=nTitle;
        this.description=nDescription;
        this.tag=nTag;
        this.dueDate=nDueDate;
        this.importance=nImportance;
    }

    public Note(Note note){
        //primo abbozzo costruttore copia
        this.id = note.getId();
        this.title = note.getTitle();
        this.description = note.getDescription();
        this.tag = note.getTag();
//        this.checkList = note.getCheckList();
        this.dueDate = note.getDueDate();
        this.importance = note.getImportance();
        this.done=note.isDone();
    }

//    private class Place {
//        //TODO place
//    }
//
//    private class MediaAttachment {
//        //TODO mediaattachment
//    }
}
