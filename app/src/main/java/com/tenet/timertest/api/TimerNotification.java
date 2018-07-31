package com.tenet.timertest.api;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class TimerNotification implements ITimerCallback {
    private static final String NOTIFICATION_CHANNEL_ID = "default";
    private static final String NOTIFICATION_CHANNEL_NAME = "timerTest";
    private static final int NOTIFICATION_ID = 1;

    private NotificationCompat.Builder mNotification;
    private NotificationManager mNotificationManager;
    PendingIntent mNotificationStartedIntent;
    PendingIntent mNotificationStoppedIntent;
    TimerNotification(Context context){
        mNotificationManager =  (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        initNotificationChannelIfNeed();
        IApiTimerSettings settings = ApiTimer.get().getSettings();
        mNotificationStartedIntent = PendingIntent.getActivity(
                context, 0, settings.getNotificationTimerStartedIntent(), PendingIntent.FLAG_UPDATE_CURRENT
        );
        mNotificationStoppedIntent = PendingIntent.getActivity(
                context, 0, settings.getNotificationTimerStopedIntent(), PendingIntent.FLAG_UPDATE_CURRENT
        );;
        mNotification = createNotification(context);

    }
    private NotificationCompat.Builder createNotification(Context context){
        return new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_NAME)
                .setSound(null)
                .setSmallIcon(android.R.drawable.ic_dialog_info);
    }
    private void initNotificationChannelIfNeed() {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        if(mNotificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) != null){
            return;
        }
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_NAME,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW);
        channel.setSound(null,null);
        mNotificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onTimerChanged(int timerValue, CharSequence textOut) {
        if(ApiTimer.get().isStarted()) {
            String text = ApiTimer.get().getSettings().getTimerLeftPrefix()+textOut.toString();
            mNotification.setContentIntent(mNotificationStartedIntent);
            mNotification.setContentTitle(text);
            mNotificationManager.notify(NOTIFICATION_ID,mNotification.build());
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }

    }

    @Override
    public void onTimerEnded() {
        mNotification.setContentTitle(ApiTimer.get().getSettings().getTimerEndMessage());
        mNotification.setContentIntent(mNotificationStoppedIntent);
        mNotificationManager.notify(NOTIFICATION_ID,mNotification.build());
    }

    @Override
    public void onTimerReset() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

}
