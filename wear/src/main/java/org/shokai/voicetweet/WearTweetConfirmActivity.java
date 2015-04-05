package org.shokai.voicetweet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class WearTweetConfirmActivity extends Activity implements DelayedConfirmationView.DelayedConfirmationListener {

    private final String TAG = "WearTweetConfirmActivity";
    private DelayedConfirmationView mDelayedView;
    private TextView mTextView;
    private String mTweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_confirm);

        Intent intent = getIntent();
        if(intent != null){
            mTweet = intent.getExtras().getString("tweet");
        }

        mTextView = (TextView) findViewById(R.id.text);
        if(mTweet != null) {
            mTextView.setText(mTweet);
        }
        mDelayedView = (DelayedConfirmationView) findViewById(R.id.delayed_confirm);
        mDelayedView.setListener(WearTweetConfirmActivity.this);
        mDelayedView.setTotalTimeMs(6000);
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
