package org.shokai.voicetweet;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import org.shokai.voicetweet.lib.TwitterUtil;

import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class WearMainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String mTweet;
    private ImageButton mButton;
    private GoogleApiClient mGoogleApiClient;
    private TwitterUtil mTwitterUtil;
    private Twitter mTwitter;

    public final String TAG = "WearMainActivity";
    private final int CODE_RECOGNIZE_SPEECH = 2;
    private final int CODE_CONFIRM_TWEET = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (ImageButton) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechRecognition();
            }
        });

        mTwitterUtil = new TwitterUtil(this);

        //startSpeechRecognition();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        if (!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    private void startSpeechRecognition(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, CODE_RECOGNIZE_SPEECH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CODE_RECOGNIZE_SPEECH &&
           resultCode == RESULT_OK){
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mTweet = results.get(0);
            Intent confirmIntent = new Intent(this, WearTweetConfirmActivity.class);
            confirmIntent.putExtra("tweet", mTweet);
            startActivityForResult(confirmIntent, CODE_CONFIRM_TWEET);
        }
        if(requestCode == CODE_CONFIRM_TWEET
                && resultCode == RESULT_OK){
            sendTweetAsync(mTweet);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleApiClient Connected");
        if(mTwitter != null) return;
        mTwitterUtil.getTwitterInstance(mGoogleApiClient, new TwitterUtil.TwitterInstanceListener() {
            @Override
            public void onCreate(Twitter twitter) {
                mTwitter = twitter;
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, error.getMessage());
                Toast.makeText(WearMainActivity.this, "login error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void sendTweetAsync(String tweet){
        if(tweet == null || mTwitter == null) return;
        Log.i(TAG, "tweet \"" + tweet + "\"");

        new AsyncTask<String, Void, Status>(){
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
                if(status != null && status.getId() > 0){
                    Toast.makeText(WearMainActivity.this, "success!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(WearMainActivity.this, "failed", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(tweet);
    }

    @Override
    public void onConnectionSuspended(int i) {
        String msg = "GoogleApiClient connection suspended";
        Log.i(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        String msg = "GoogleApiClient connection failed" + result.toString();
        Log.i(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();
    }

}
