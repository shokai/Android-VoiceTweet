package org.shokai.voicetweet;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;


public class TweetTestActivity extends Activity {

    private Twitter mTwitter;
    private TwitterUtil mTwitterUtil;
    private Button mButton;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_test);

        mTwitterUtil = new TwitterUtil(this);
        mTwitter = mTwitterUtil.getTwitterInstance();
        mButton = (Button) findViewById(R.id.button);
        mEditText = (EditText) findViewById(R.id.editText);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tweet = mEditText.getText().toString();
                new AsyncTask<String, Void, Status>() {
                    @Override
                    protected twitter4j.Status doInBackground(String... params) {
                        String tweet = params[0];
                        try {
                            return mTwitter.updateStatus(tweet);
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(twitter4j.Status status) {
                        if (status != null && status.getId() > 0) {
                            Toast.makeText(TweetTestActivity.this, "success!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TweetTestActivity.this, "failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute(tweet);
            }

        });
    }
}
