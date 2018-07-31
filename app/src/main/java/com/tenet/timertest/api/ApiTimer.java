package com.tenet.timertest.api;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiTimer extends Handler {

    private static ApiTimer INSTANCE;
    private static final int MSG_CHANGED = 1;
    private static final int MSG_RESET = 2;

    static final int MINUTE = 60;
    private static final int INIT_TIMER_START = 5*MINUTE;
    private static final int MIN_TIMER_START = MINUTE;
    private static final int MAX_TIMER_START = 10*MINUTE;

    private List<ITimerCallback> mUiThreadCallbacks = new ArrayList<>();
    private final AtomicInteger mCounter = new AtomicInteger(INIT_TIMER_START);
    private final StringBuilder mStringBuilder = new StringBuilder();
    private volatile boolean mStarted = false;
    private IApiTimerSettings mSettings;
    private TimerTask mTimerTask;

    public static void init(Context context,IApiTimerSettings settings){
        TimerSaver ts = new TimerSaver(context);
        INSTANCE = new ApiTimer(settings);
        INSTANCE.addCallback(new TimerNotification(context));
        INSTANCE.addCallback(new TimerSounds(context));
        INSTANCE.addCallback(ts);
        INSTANCE.mCounter.set(ts.getPrefCounter(INIT_TIMER_START));
        boolean start = ts.getPrefStarted(false);
        if(start) {
            INSTANCE.startTimer();
        }
    }
    private ApiTimer(IApiTimerSettings settings){
        mSettings  = settings;
    }
    public final IApiTimerSettings getSettings(){
        return mSettings;
    }
    public static ApiTimer get(){
        return INSTANCE;
    }
    private void notifyChangeWorkerThread(){
        sendEmptyMessage(MSG_CHANGED);
    }
    public void resetTimer(){
        stopThreadTimer();
        mStarted = false;
        mCounter.set(INIT_TIMER_START);
        notifyChangeWorkerThread();
        notifyTimerStop(true);
    }
    public void startTimer(){
        if(isStarted()) {
            return;
        }
        mStarted = true;
        TimerService.run(getSettings().getAppContext());
    }
    public boolean isStarted() {
        return mStarted;
    }
    private void decrementCounterIfCan() {
        if(getCounter() == 0) {
            return;
        }
        mCounter.decrementAndGet();
    }
    void executeThreadTimer(){
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                decrementCounterIfCan();
                notifyChangeWorkerThread();
            }
        };
        new Timer().schedule(mTimerTask,1000,1000);
    }
    private void stopThreadTimer(){
        if(mTimerTask != null){
            mTimerTask.cancel();
            mTimerTask = null;
        }
        TimerService.stop(getSettings().getAppContext());
        mStarted = false;
    }
    public int getCounter() {
        return mCounter.get();
    }

    public synchronized void addCallback(ITimerCallback callback){
        mUiThreadCallbacks.add(callback);
    }
    public synchronized void removeCallback(ITimerCallback callback){
        mUiThreadCallbacks.remove(callback);
    }
    public synchronized CharSequence getTextOut() {
        int counter = getCounter();
        int min = counter/MINUTE;
        int sec = counter%MINUTE;
        mStringBuilder.setLength(0);
        if(min<10) {
            mStringBuilder.append('0');
        }
        mStringBuilder.append(min);
        mStringBuilder.append(':');
        if(sec<10) {
            mStringBuilder.append('0');
        }
        mStringBuilder.append(sec);
        return mStringBuilder;
    }

    private void notifyUIThreadChange(){
        int counter = getCounter();
        Log.d("TimerTest","Counter:"+counter);
        notifyTimerChange();
        if(counter == 0) {
            stopThreadTimer();
            notifyTimerStop(false);
        }
    }
    private void notifyTimerStop(boolean reset) {
        for (ITimerCallback cb: mUiThreadCallbacks) {
            if(reset) {
                cb.onTimerReset();
            } else {
                cb.onTimerEnded();
            }
        }
    }
    private void notifyTimerChange(){
        if(mUiThreadCallbacks.isEmpty()) {
            return;
        }
        CharSequence out = getTextOut();
        for (ITimerCallback cb: mUiThreadCallbacks) {
            cb.onTimerChanged(getCounter(),out);
        }
    }
    public void increaseCounterByMinute(){
        if(!isStarted() && getCounter()<MAX_TIMER_START){
            mCounter.addAndGet(MINUTE);
            notifyChangeWorkerThread();
        }
    }
    public void decreaseCounterByMinute(){
        if(!isStarted() && getCounter()>MIN_TIMER_START){
            mCounter.addAndGet(0-MINUTE);
            notifyChangeWorkerThread();
        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        notifyUIThreadChange();
        if(msg.what == MSG_RESET) {
            notifyTimerStop(true);
        }
    }
}
