package com.alessio.luca.a321do;

/**
 * Created by Luca on 04/10/2016.
 */
public class Importance {

    //definizione enum valori importanza (nomi provvisori)
    public enum Priority {IMPORTANT,NORMAL,SECONDARY};
    public enum Urgency {HIGH,NORMAL,LOW};

    //campi dato
    private Priority priority;
    private Urgency urgency;

    //costruttori
    public Importance() {
        this.priority = Priority.NORMAL;
        this.urgency = Urgency.NORMAL;
    }
    public Importance(Importance importance) {
        this.priority = importance.getPriority();
        this.urgency = importance.getUrgency();
    }
    public Importance(Priority p, Urgency u) {
        this.priority = p;
        this.urgency = u;
    }
    public Importance(int i, char c){
        this.priority = mapToPriority(i);
        this.urgency = mapToUrgency(c);
    }

    //set & get
    public Priority getPriority() {
        return priority;
    }
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    public Urgency getUrgency() {
        return urgency;
    }
    public void setUrgency(Urgency urgency) {
        this.urgency = urgency;
    }

    //altri metodi
    public String translate() {
        String temp = new String();
        switch (getPriority()) {
            case IMPORTANT:
                temp = "1";
                break;
            case NORMAL:
                temp = "2";
                break;
            case SECONDARY:
                temp = "3";
                break;
            default:
                temp = "?";
                break;
        }
        switch (getUrgency()) {
            case HIGH:
                temp = temp + "A";
                break;
            case NORMAL:
                temp = temp + "B";
                break;
            case LOW:
                temp = temp + "C";
                break;
            default:
                temp = temp + "?";
                break;
        }
        return temp;
    }
    public Priority mapToPriority(int i) {
        Priority p;
        switch (i){
            case 1: p=Priority.IMPORTANT; break;
            case 2: p=Priority.NORMAL; break;
            case 3: p=Priority.SECONDARY; break;
            default: p=Priority.NORMAL; break;
        }
        return p;
    }
    public Urgency mapToUrgency(char c) {
        Urgency u;
        switch (c){
            case 'A': u=Urgency.HIGH; break;
            case 'B': u=Urgency.NORMAL; break;
            case 'C': u=Urgency.LOW; break;
            default: u=Urgency.NORMAL; break;
        }
        return u;
    }
}
