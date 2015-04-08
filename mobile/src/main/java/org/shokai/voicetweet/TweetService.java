package org.shokai.voicetweet;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetService extends WearableListenerService implements
        ResultCallback<MessageApi.SendMessageResult> {

    private final static String TAG = "TweetService";
    public final static String MESSAGE_PATH_TWEET         = "/tweet/post";
    public final static String MESSAGE_PATH_TWEET_SUCCESS = "/tweet/post/success";
    public final static String MESSAGE_PATH_TWEET_FAILED  = "/tweet/post/failed";

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
        if (messageEvent.getPath().equals(MESSAGE_PATH_TWEET)) {
            String msg;
            try {
                msg = new String(messageEvent.getData(), "UTF-8");
            }
            catch (Exception ex){
                Log.e(TAG, ex.getMessage());
                return;
            }
            Log.i(TAG, "receive from wear: "+ msg);
            updateTweet(msg);
        }
    }

    private Status updateTweet(String tweet){
        if(!mTwitterUtil.hasToken()) return null;
        Twitter client = mTwitterUtil.getTwitterInstance();
        Status status = null;
        try {
            status = client.updateStatus(tweet);
        } catch (TwitterException e) {
            e.printStackTrace();
            sendMessageToWear(MESSAGE_PATH_TWEET_FAILED, e.getErrorMessage(), this);
        }

        if(status != null && status.getId() > 0){
            sendMessageToWear(MESSAGE_PATH_TWEET_SUCCESS, tweet, this);
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
