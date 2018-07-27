package com.tenet.timertest;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.tenet.timertest.api.ApiTimer;
import com.tenet.timertest.api.IApiTimerSettings;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        IApiTimerSettings settings = new IApiTimerSettings() {
            @Override
            public Context getAppContext() {
                return App.this;
            }

            @Override
            public String getTimerLeftPrefix() {
                return getString(R.string.timer_left_prefix);
            }

            @Override
            public String getTimerEndMessage() {
                return getString(R.string.timer_ended);
            }

            @Override
            public Intent getNotificationRunIntent() {
                return new Intent(App.this,ActivityStart.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        };
        ApiTimer.init(this,settings);
    }
}
