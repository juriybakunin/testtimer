package com.tenet.timertest.api;

import android.content.Context;
import android.media.MediaPlayer;

import com.tenet.timertest.R;

public class TimerSounds implements ITimerCallback {

    private MediaPlayer mShortSound;
    private MediaPlayer  mLongSound;
    TimerSounds(Context context){
        mShortSound = MediaPlayer.create(context, R.raw.short_sound);
        mLongSound = MediaPlayer.create(context, R.raw.long_sound);
    }
    private void playShortSound(){
        mShortSound.start();
    }
    private void playLongSound(){
        mLongSound.start();
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
