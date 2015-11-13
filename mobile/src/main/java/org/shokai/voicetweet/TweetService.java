package org.shokai.voicetweet;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.UnsupportedEncodingException;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetService extends WearableListenerService implements
        ResultCallback<MessageApi.SendMessageResult> {

    private final static String TAG = "TweetService";

    private TwitterUtil mTwitterUtil;
    private GoogleApiClient mGoogleApiClient;

    public TweetService() {
        mTwitterUtil = new TwitterUtil(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.v(TAG, "onMessageReceived");
        switch (messageEvent.getPath()) {
            case MessagePath.TWEET:
                String msg;
                try {
                    msg = new String(messageEvent.getData(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return;
                }
                Log.i(TAG, "receive from wear: " + msg);
                updateTweet(msg);
                break;
            case MessagePath.LAUNCH_APP:
                Log.i(TAG, "launch phone app");
                Intent intent = new Intent(this, MainActivity_.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                sendMessageToWear(MessagePath.LAUNCH_APP_SUCCESS, "Launch App Success", this);
        }
    }

    private Status updateTweet(String tweet){
        if(!mTwitterUtil.hasToken()){
            sendMessageToWear(MessagePath.TWEET_FAILED, "Please Login", this);
            return null;
        }
        Twitter client = mTwitterUtil.getTwitterInstance();
        Status status = null;
        try {
            status = client.updateStatus(tweet);
        } catch (TwitterException e) {
            e.printStackTrace();
            sendMessageToWear(MessagePath.TWEET_FAILED, e.getErrorMessage(), this);
        }

        if(status != null && status.getId() > 0){
            sendMessageToWear(MessagePath.TWEET_SUCCESS, tweet, this);
            return status;
        }
        return null;
    }

    private void sendMessageToWear(String path, String msg, ResultCallback<MessageApi.SendMessageResult> callback){
        Log.v(TAG, "sendMessageToWear: "+path+" "+msg);
        byte[] bytes;
        try{
            bytes = msg.getBytes("UTF-8");
        }
        catch (Exception ex){
            ex.printStackTrace();
            return;
        }
        for (Node node : Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes()){
            Log.v(TAG, "sending to node:" + node.getId());
            PendingResult<MessageApi.SendMessageResult> res = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, bytes);
            if(callback != null){
                res.setResultCallback(callback);
            }
        }
    }

    @Override
    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
        com.google.android.gms.common.api.Status stat = sendMessageResult.getStatus();
        if(!stat.isSuccess()){
            Log.e(TAG, "sendMessageToWear failed: " + stat.getStatusMessage());
        }
        else {
            Log.i(TAG, "sendMessageToWear success");
        }
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }
}
