package com.alessio.luca.a321do;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.health.SystemHealthManager;
import android.util.Log;

/**
 * Created by Luca on 14/10/2016.
 */

public class AlarmReceiver extends BroadcastReceiver {

    public static final String OPEN_NOTIFICATION = "OPEN_NOTIFICATION";
    public static final String COMPLETE_NOTE = "COMPLETE_NOTE";
    public static final String SNOOZE_NOTE = "SNOOZE_NOTE";
    public static final String CLOSE_NOTIFICATION = "CLOSE_NOTIFICATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        Note note = (Note) intent.getExtras().get("NotePayload");
        Bundle bundle = new Bundle();
        bundle.putSerializable("NotePayload",note);
        intent = new Intent(context, NotificationReceiverActivity.class);
        intent.putExtras(bundle);
        intent.setAction(OPEN_NOTIFICATION);
        PendingIntent pIntent = PendingIntent.getActivity(context, note.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentComplete = new Intent(context, NotificationReceiverActivity.class);
        intentComplete.setAction(COMPLETE_NOTE);
        PendingIntent pendingIntentComplete = PendingIntent.getActivity(context, note.getId(), intentComplete, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentDismiss = new Intent(context, NotificationReceiverActivity.class);
        intentDismiss.setAction(CLOSE_NOTIFICATION);
        PendingIntent pendingIntentDismiss = PendingIntent.getActivity(context, note.getId(), intentDismiss, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentSnooze = new Intent(context, NotificationReceiverActivity.class);
        intentDismiss.setAction(SNOOZE_NOTE);
        PendingIntent pendingIntentSnooze = PendingIntent.getActivity(context, note.getId(), intentSnooze, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(context)
                .setContentTitle(note.getTitle())
                .setContentText(note.getDescription())
                .setDefaults(Notification.DEFAULT_ALL)
                .setTicker(note.getTitle())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_launcher,"Dismiss",pendingIntentDismiss)
                .addAction(R.mipmap.ic_launcher,"Snooze",pendingIntentSnooze)
                .addAction(R.mipmap.ic_launcher,"Tick",pendingIntentComplete)
                .setContentIntent(pIntent).build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(note.getId(), notification);
    }
}
