package org.shokai.voicetweet;

import android.app.Activity;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_tweet_confirm)
public class TweetConfirmActivity extends Activity implements DelayedConfirmationView.DelayedConfirmationListener {

    private final String TAG = "TweetConfirmActivity";

    @ViewById(R.id.delayed_confirm)
    DelayedConfirmationView mDelayedView;

    @ViewById(R.id.text)
    TextView mTextView;

    @Extra("tweet")
    String mTweet;

    @AfterViews
    void afterViews(){
        if(mTweet == null){
            Log.e(TAG, "tweet is empty");
            finish();
        }
        mTextView.setText(mTweet);
        mDelayedView.setListener(TweetConfirmActivity.this);
        mDelayedView.setTotalTimeMs(3000 + mTweet.length()*100);
        mDelayedView.start();
    }

    @Override
    public void onTimerFinished(View view) {
        Log.v(TAG, "finished");
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onTimerSelected(View view) {
        Log.v(TAG, "canceled");
        setResult(RESULT_CANCELED);
        finish();
    }

}
