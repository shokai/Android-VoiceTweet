package org.shokai.voicetweet;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.activity.ConfirmationActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;

import java.util.List;

@EActivity(R.layout.activity_tweet)
public class TweetActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        MessageApi.MessageListener {

    public final String TAG = "MainActivity";

    private static final int CODE_RECOGNIZE_SPEECH = 10;
    private static final int CODE_CONFIRM_TWEET    = 11;

    public final static String MESSAGE_PATH_TWEET         = "/tweet/post";
    public final static String MESSAGE_PATH_TWEET_SUCCESS = "/tweet/post/success";
    public final static String MESSAGE_PATH_TWEET_FAILED  = "/tweet/post/failed";

    private String mTweet;
    private GoogleApiClient mGoogleApiClient;

    @Click(R.id.button)
    void onButtonClick(){
        startSpeechRecognition();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void startSpeechRecognition(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, CODE_RECOGNIZE_SPEECH);
    }

    @OnActivityResult(CODE_RECOGNIZE_SPEECH)
    void onRecognizeSpeech(int resultCode, Intent data){
        if(resultCode != RESULT_OK) return;
        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if(results.isEmpty()) return;
        mTweet = results.get(0);
        if(mTweet == null || mTweet.length() < 1) return;
        Intent confirmIntent = new Intent(this, TweetConfirmActivity_.class);
        confirmIntent.putExtra("tweet", mTweet);
        startActivityForResult(confirmIntent, CODE_CONFIRM_TWEET);
    }

    @OnActivityResult(CODE_CONFIRM_TWEET)
    void onConfirmTweet(int resultCode){
        if(resultCode != RESULT_OK) return;
        sendMessageToHandheldAsync(MESSAGE_PATH_TWEET, mTweet);
    }

    public void sendMessageToHandheldAsync(String path, String msg){
        if(msg == null) return;
        Log.i(TAG, "sendMessageToHandheld: " + path + " " + msg);
        new AsyncTask<String, Void, PendingResult<MessageApi.SendMessageResult>>() {
            @Override
            protected PendingResult<MessageApi.SendMessageResult> doInBackground(String... params) {
                String path = params[0];
                String tweet = params[1];
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
                    return Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, bytes);
                }
                return null;
            }

            @Override
            protected void onPostExecute(PendingResult<MessageApi.SendMessageResult> pendingResult) {
                if(pendingResult == null) return;
                pendingResult.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        com.google.android.gms.common.api.Status stat = sendMessageResult.getStatus();
                        if(!stat.isSuccess()){
                            Log.e(TAG, "sendMessageToWear failed: "+stat.getStatusMessage());
                        }
                        else {
                            Log.i(TAG, "sendMessageToWear success");
                        }
                    }
                });
            }
        }.execute(path, msg);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleApiClient Connected");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
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
     * Message from Handheld
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String res = new String(messageEvent.getData());
        Intent intent = new Intent(this, ConfirmationActivity.class);

        switch (messageEvent.getPath()) {
            case MESSAGE_PATH_TWEET_SUCCESS:
                Log.i(TAG, "Tweet Success: " + res);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, res);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
                break;
            case MESSAGE_PATH_TWEET_FAILED:
                Log.i(TAG, "Tweet Failed: " + res);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, res);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
                break;
        }
        startActivity(intent);
    }
}
