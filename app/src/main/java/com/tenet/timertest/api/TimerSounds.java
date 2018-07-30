package com.tenet.timertest.api;

import android.media.AudioManager;
import android.media.ToneGenerator;

class TimerSounds implements ITimerCallback {

    private ToneGenerator mToneGenerator;
    TimerSounds(){
        mToneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION,100);
    }
    private void playShortSound(){
        mToneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP,50);
    }
    private void playLongSound(){
        mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ANSWER,250);
    }

    @Override
    public void onTimerChanged(int timerValue, CharSequence textOut) {
        if(timerValue == 0 || !ApiTimer.get().isStarted()) {
            return;
        }
        if(timerValue < 11 || (timerValue%ApiTimer.MINUTE) == 0) {
            playShortSound();
        }
    }

    @Override
    public void onTimerEnded() {
        playLongSound();
    }

    @Override
    public void onTimerReset() {

    }
}
