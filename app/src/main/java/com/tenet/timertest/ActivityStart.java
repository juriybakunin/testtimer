package com.tenet.timertest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.tenet.timertest.api.ApiTimer;
import com.tenet.timertest.api.ITimerCallback;

public class ActivityStart extends AppCompatActivity implements ITimerCallback {
    private TextView mTimerValue;
    private View mPlus;
    private View mMinus;
    private View mReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mTimerValue = findViewById(R.id.timerValue);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewClick(view);
            }
        };
        mPlus = findViewById(R.id.timerPlus);
        mMinus = findViewById(R.id.timerMinus);
        mReset = findViewById(R.id.timerReset);
        mPlus.setOnClickListener(listener);
        mMinus.setOnClickListener(listener);
        mReset.setOnClickListener(listener);
        mTimerValue.setOnClickListener(listener);
        ApiTimer api = ApiTimer.get();
        onTimerChanged(api.getCounter(),api.getTextOut());
        ApiTimer.get().addCallback(this);
    }
    @Override
    public void onTimerChanged(int timerValue, CharSequence textOut) {
        mTimerValue.setText(textOut);
    }

    @Override
    public void onTimerEnded() {
        ActivitySecond.run(this);
    }

    @Override
    public void onTimerReset() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ApiTimer.get().removeCallback(this);
    }

    public void onViewClick(View view) {
        ApiTimer api = ApiTimer.get();
        if(view == mTimerValue){
            api.startTimer();
        } else if(view == mReset) {
            api.resetTimer();
        } else if(view == mPlus) {
            api.increaseCounterByMinute();
        } else if(view == mMinus){
            api.decreaseCounterByMinute();
        }
    }
}
