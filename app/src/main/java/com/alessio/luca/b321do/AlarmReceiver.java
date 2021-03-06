package com.alessio.luca.b321do;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

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
        Note note = (Note) intent.getExtras().get(Utilities.NOTIFICATION_PAYLOAD_CODE);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Utilities.NOTIFICATION_PAYLOAD_CODE,note);
        intent = new Intent(context, NotificationReceiverActivity.class);
        intent.putExtras(bundle);
        intent.setAction(OPEN_NOTIFICATION);
        PendingIntent pIntent = PendingIntent.getActivity(context, note.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentComplete = new Intent(context, NotificationReceiverActivity.class);
        intentComplete.putExtras(bundle);
        intentComplete.setAction(COMPLETE_NOTE);
        PendingIntent pendingIntentComplete = PendingIntent.getActivity(context, note.getId(), intentComplete, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentDismiss = new Intent(context, NotificationReceiverActivity.class);
        intentDismiss.putExtras(bundle);
        intentDismiss.setAction(CLOSE_NOTIFICATION);
        PendingIntent pendingIntentDismiss = PendingIntent.getActivity(context, note.getId(), intentDismiss, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentSnooze = new Intent(context, NotificationReceiverActivity.class);
        intentSnooze.putExtras(bundle);
        intentSnooze.setAction(SNOOZE_NOTE);
        PendingIntent pendingIntentSnooze = PendingIntent.getActivity(context, note.getId(), intentSnooze, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = null;

        if (Build.VERSION.SDK_INT > 19)
        {
            Notification.Action action1 = new Notification.Action.Builder(R.mipmap.nope,"",pendingIntentDismiss).build();
            Notification.Action action2 = new Notification.Action.Builder(R.mipmap.snooze,"",pendingIntentSnooze).build();
            Notification.Action action3 = new Notification.Action.Builder(R.mipmap.yep,"",pendingIntentComplete).build();
            notification = new Notification.Builder(context)
                    .setContentTitle(note.getTitle())
                    .setContentText(note.getDescription())
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setTicker(note.getTitle())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .addAction(action1)
                    .addAction(action2)
                    .addAction(action3)
                    .setAutoCancel(true)
                    .setContentIntent(pIntent).build();

        }
        else
        {
            notification = new Notification.Builder(context)
                    .setContentTitle(note.getTitle())
                    .setContentText(note.getDescription())
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setTicker(note.getTitle())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .addAction(R.mipmap.nope,"",pendingIntentDismiss)
                    .addAction(R.mipmap.snooze,"",pendingIntentSnooze)
                    .addAction(R.mipmap.yep,"",pendingIntentComplete)
                    .setContentIntent(pIntent).build();
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(note.getId(), notification);
    }
}