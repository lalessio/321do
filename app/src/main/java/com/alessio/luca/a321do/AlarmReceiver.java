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

    @Override
    public void onReceive(Context context, Intent intent) {
        Note note = (Note) intent.getExtras().get("NotePayload");
        Bundle bundle = new Bundle();
        bundle.putSerializable("NotePayload",note);
        intent = new Intent(context, NotificationReceiverActivity.class);
        intent.putExtras(bundle);
        PendingIntent pIntent = PendingIntent.getActivity(context, note.getId(), intent, 0);

        Notification notification = new Notification.Builder(context)
                .setContentTitle(note.getTitle())
                .setContentText(note.getDescription())
                .setDefaults(Notification.DEFAULT_ALL)
                .setTicker(note.getTitle())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(pIntent).build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(note.getId(), notification);
    }
}
