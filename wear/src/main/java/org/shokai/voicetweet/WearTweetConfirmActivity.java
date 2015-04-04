package org.shokai.voicetweet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.TextView;

public class WearTweetConfirmActivity extends Activity implements
        WatchViewStub.OnLayoutInflatedListener,
        DelayedConfirmationView.DelayedConfirmationListener {

    private DelayedConfirmationView mDelayedView;
    private TextView mTextView;
    private String mTweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_confirm);
        WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(this);

        Intent intent = getIntent();
        if(intent != null){
            mTweet = intent.getExtras().getString("tweet");
        }

    }

    @Override
    public void onLayoutInflated(WatchViewStub stub) {
        mTextView = (TextView) stub.findViewById(R.id.text);
        if(mTweet != null) {
            mTextView.setText(mTweet);
        }
        mDelayedView = (DelayedConfirmationView) stub.findViewById(R.id.delayed_confirm);
        mDelayedView.setListener(WearTweetConfirmActivity.this);
        mDelayedView.setTotalTimeMs(6000);
        mDelayedView.start();
    }

    @Override
    public void onTimerFinished(View view) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onTimerSelected(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

}
