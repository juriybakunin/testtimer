package com.tenet.timertest.api;

import android.content.Context;
import android.content.SharedPreferences;

public class TimerSaver implements ITimerCallback {
    private static final String PREF_COUNTER = "counter";
    private static final String PREF_STARTED = "started";
    private SharedPreferences mPrefs;
    TimerSaver(Context context) {
        mPrefs = context.getSharedPreferences("timerTest",0);
    }
    int getPrefCounter(int defaultCounter){
        return mPrefs.getInt(PREF_COUNTER,defaultCounter);
    }
    boolean getPrefStarted(boolean defaultStarted){
        return mPrefs.getBoolean(PREF_STARTED,defaultStarted);
    }
    private void save(){
        ApiTimer apiTimer = ApiTimer.get();
        mPrefs.edit()
                .putInt(PREF_COUNTER,apiTimer.getCounter())
                .putBoolean(PREF_STARTED,apiTimer.isStarted())
                .apply();

    }
    @Override
    public void onTimerChanged(int timerValue, CharSequence textOut) {
        save();
    }

    @Override
    public void onTimerEnded() {
        save();
    }

    @Override
    public void onTimerReset() {
        save();
    }
}
