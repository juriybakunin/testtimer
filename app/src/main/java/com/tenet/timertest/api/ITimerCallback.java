package com.tenet.timertest.api;

public interface ITimerCallback {
    void onTimerChanged(int timerValue,CharSequence textOut);
    void onTimerEnded();
    void onTimerReset();
}
