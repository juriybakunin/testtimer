package com.tenet.timertest.api;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class TimerService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        ApiTimer apiTimer = ApiTimer.get();
        TimerNotification notification = apiTimer.getNotification();
        startForeground(notification.getNotificationId(),notification.getNotification());
        ApiTimer.get().executeThreadTimer();
        return Service.START_STICKY;
    }

    public static void run(Context c){
        Intent in = new Intent(c, TimerService.class);
        if(Build.VERSION.SDK_INT >= 26) {
            c.startForegroundService(in);
        } else {
            c.startService(in);
        }

    }
    public static void stop(Context c){
        c.stopService(new Intent(c,TimerService.class));
    }
}
