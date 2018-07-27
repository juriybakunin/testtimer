package com.tenet.timertest.api;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

public class ApiTimer  {

    private static ApiTimer INSTANCE;


    private static final int MINUTE = 60;
    private static final int INIT_TIMER_START = 5*MINUTE;
    private static final int MIN_TIMER_START = MINUTE;
    private static final int MAX_TIMER_START = 10*MINUTE;

    private List<ITimerCallback> mUiThreadCallbacks = new ArrayList<>();
    private final AtomicInteger mCounter = new AtomicInteger(INIT_TIMER_START);
    private final StringBuilder mStringBuilder = new StringBuilder();
    private volatile boolean mStarted = false;
    private IApiTimerSettings mSettings;
    private Disposable mDispose;
    private Observable<Long> mObservableTimer = Observable.interval(1,1, TimeUnit.SECONDS)
            .takeWhile(new Predicate<Long>() {
                @Override
                public boolean test(Long aLong) throws Exception {
                    return decrementCounterIfCan();
                }
            }).observeOn(AndroidSchedulers.mainThread());
    private Observable<Long> mObservableUpdateUi = Observable.just((long)1)
            .observeOn(AndroidSchedulers.mainThread());
    private Consumer<Long> mConsumerUpdateUi = new Consumer<Long>() {
        @Override
        public void accept(Long integer) throws Exception {
            notifyUIThreadChange();
        }
    };


    public static void init(Context context,IApiTimerSettings settings){
        INSTANCE = new ApiTimer(settings);
        INSTANCE.addCallback(new TimerNotification(context));
        INSTANCE.addCallback(new TimerSounds());
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
    @SuppressLint("CheckResult")
    private void notifyChangeWorkerThread(){
        mObservableUpdateUi.subscribe(mConsumerUpdateUi);
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
    private boolean decrementCounterIfCan() {
        if(getCounter() == 0) {
            return false;
        }
        mCounter.decrementAndGet();
        return true;
    }
    void executeThreadTimer(){
        mDispose = mObservableTimer.subscribe(mConsumerUpdateUi);
    }
    private void stopThreadTimer(){
        if(mDispose != null){
            mDispose.dispose();
            mDispose = null;
        }
        TimerService.stop(getSettings().getAppContext());
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
}
