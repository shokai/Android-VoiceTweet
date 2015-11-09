package org.shokai.voicetweet;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import twitter4j.Twitter;
import twitter4j.TwitterException;


@EActivity(R.layout.activity_tweet_test)
public class TweetTestActivity extends Activity {

    private Twitter mTwitter;
    private TwitterUtil mTwitterUtil;

    @ViewById(R.id.button)
    Button mButton;

    @ViewById(R.id.editText)
    EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTwitterUtil = new TwitterUtil(this);
        mTwitter = mTwitterUtil.getTwitterInstance();
    }

    @Click
    void button() {
        updateTweetAsync(mEditText.getText().toString());
    }

    @Background
    void updateTweetAsync(String tweet) {
        try {
            twitter4j.Status res = mTwitter.updateStatus(tweet);
            if (res == null || !(res.getId() > 0)) {
                displayTweetResult("failed");
                return;
            }
        } catch (TwitterException e) {
            e.printStackTrace();
            displayTweetResult(e.getMessage());
            return;
        }
        displayTweetResult("success");
    }

    @UiThread
    void displayTweetResult(String message) {
        Toast.makeText(TweetTestActivity.this, message, Toast.LENGTH_SHORT).show();
    }

}
