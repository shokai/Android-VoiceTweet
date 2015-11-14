package org.shokai.voicetweet;

import android.content.Intent;
import android.support.wearable.activity.ConfirmationActivity;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;

public class WearService extends GoogleApiClientService {

    public final static String TAG = "WearService";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.v(TAG, "onMessageReceived");
        String msg = new String(messageEvent.getData());
        Intent intent;
        switch (messageEvent.getPath()){
            case MessagePath.LAUNCH_APP:
                Log.i(TAG, "launch wear app");
                intent = new Intent(this, TweetActivity_.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                break;
            case MessagePath.TWITTER_NOT_LOGIN:
                Log.i(TAG, msg);
                intent = new Intent(this, LaunchPhoneAppActivity_.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                break;
            case MessagePath.TWEET_SUCCESS:
                Log.i(TAG, "Tweet Success: " + msg);
                intent = new Intent(this, ConfirmationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, msg);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
                startActivity(intent);
                break;
            case MessagePath.TWEET_FAILED:
                Log.i(TAG, "Tweet Failed: " + msg);
                intent = new Intent(this, ConfirmationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, msg);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
                startActivity(intent);
                break;
        }
    }

}
