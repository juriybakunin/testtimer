package com.tenet.timertest.api;

import android.content.Context;
import android.content.Intent;

public interface IApiTimerSettings {
    Context getAppContext();
    String getTimerLeftPrefix();
    String getTimerEndMessage();
    Intent getNotificationTimerStartedIntent();
    Intent getNotificationTimerStopedIntent();
}
