package org.shokai.voicetweet;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;

import java.util.List;

@EActivity(R.layout.activity_tweet)
public class TweetActivity extends GoogleApiClientActivity {

    public final static String TAG = "TweetActivity";

    private static final int CODE_RECOGNIZE_SPEECH = 10;
    private static final int CODE_CONFIRM_TWEET    = 11;

    private String mTweet;

    @Click(R.id.button)
    void onButtonClick(){
        startSpeechRecognition();
    }

    @Override
    protected void onResume(){
        super.onResume();
        final TweetActivity self = this;
        Wearable.DataApi.getDataItems(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DataItemBuffer>() {
                    @Override
                    public void onResult(DataItemBuffer dataItems) {
                        for (DataItem dataItem : dataItems) {
                            if (dataItem.getUri().getPath().equals(MessagePath.ROOT)) {
                                DataMap dataMap = DataMap.fromByteArray(dataItem.getData());
                                boolean login = dataMap.getBoolean(MessagePath.IS_LOGIN);
                                Log.v(TAG, "twitter login :" + login);
                                if (!login) {
                                    LaunchPhoneAppActivity_.intent(self).start();
                                }
                            }
                        }
                        dataItems.release();
                    }
                });
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
        TweetConfirmActivity_
                .intent(this)
                .extra("tweet", mTweet)
                .startForResult(CODE_CONFIRM_TWEET);
    }

    @OnActivityResult(CODE_CONFIRM_TWEET)
    void onConfirmTweet(int resultCode){
        if(resultCode != RESULT_OK) return;
        sendMessageAsync(MessagePath.TWEET, mTweet);
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

}
