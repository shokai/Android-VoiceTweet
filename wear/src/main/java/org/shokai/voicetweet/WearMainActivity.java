package org.shokai.voicetweet;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.activity.ConfirmationActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class WearMainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<MessageApi.SendMessageResult> {

    public final String TAG = "MainActivity";

    private static final int CODE_RECOGNIZE_SPEECH = 10;
    private static final int CODE_CONFIRM_TWEET    = 11;
    public static final String MESSAGE_PATH_TWEET  = "/tweet/post";

    private String mTweet;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton button = (ImageButton) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechRecognition();
            }
        });
        startSpeechRecognition();
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
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void startSpeechRecognition(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, CODE_RECOGNIZE_SPEECH);
    }

    /*
     * receive speech recognition result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CODE_RECOGNIZE_SPEECH &&
           resultCode == RESULT_OK){
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mTweet = results.get(0);
            if(mTweet == null || mTweet.length() < 1) return;
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

    public void sendTweetAsync(String tweet){
        if(tweet == null) return;
        Log.i(TAG, "send \"" + tweet + "\" to handheld");
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String tweet = params[0];
                byte[] bytes;
                try {
                    bytes = tweet.getBytes("UTF-8");
                }
                catch(Exception ex){
                    Log.e(TAG, ex.getMessage());
                    return null;
                }
                for (Node node : Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes()){
                    Log.v(TAG, "sending to node:" + node.getId());
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), MESSAGE_PATH_TWEET, bytes)
                            .setResultCallback(WearMainActivity.this);
                }
                return null;
            }
        }.execute(tweet);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleApiClient Connected");
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

    /*
     Result from Handheld
     */
    @Override
    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
        Intent intent = new Intent(WearMainActivity.this, ConfirmationActivity.class);
        if (sendMessageResult.getStatus().isSuccess()) {
            Log.v(TAG, "success");
            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "success");
            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
        }
        else{
            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "failed");
            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
        }
        startActivity(intent);
    }
}
