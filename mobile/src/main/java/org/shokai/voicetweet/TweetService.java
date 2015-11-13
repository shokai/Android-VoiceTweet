package org.shokai.voicetweet;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;

import java.io.UnsupportedEncodingException;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetService extends GoogleApiClientService {

    private final static String TAG = "TweetService";

    private TwitterUtil mTwitterUtil;

    public TweetService() {
        mTwitterUtil = new TwitterUtil(this);
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
                sendMessage(MessagePath.LAUNCH_APP_SUCCESS, "done", this);
        }
    }

    private Status updateTweet(String tweet){
        if(!mTwitterUtil.hasToken()){
            sendMessage(MessagePath.TWEET_FAILED, "Please Login", this);
            return null;
        }
        Twitter client = mTwitterUtil.getTwitterInstance();
        Status status = null;
        try {
            status = client.updateStatus(tweet);
        } catch (TwitterException e) {
            e.printStackTrace();
            sendMessage(MessagePath.TWEET_FAILED, e.getErrorMessage(), this);
        }

        if(status != null && status.getId() > 0){
            sendMessage(MessagePath.TWEET_SUCCESS, tweet, this);
            return status;
        }
        return null;
    }
}
