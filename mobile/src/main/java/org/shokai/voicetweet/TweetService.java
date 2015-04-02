package org.shokai.voicetweet;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetService extends WearableListenerService {

    private final String TAG = "TweetService";
    public final String MESSAGE_PATH_TWEET = "/tweet/post";
    private TwitterUtil mTwitterUtil;

    public TweetService(){
        mTwitterUtil = new TwitterUtil(this);
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
            Log.i(TAG, "receive: "+ msg);
            Status status = updateTweet(msg);
            if(status != null && status.getId() > 0){
                sendNotification("tweet success ("+msg+")");
            }
            else {
                sendNotification("tweet failed ("+msg+")");
            }
        }
    }

    private void sendNotification(String msg){
        Log.v(TAG, "sendNotification: "+msg);

        Notification notif = new NotificationCompat.Builder(this)
                .setContentTitle(TAG)
                .setContentText(msg)
                .setSmallIcon(android.R.drawable.btn_default_small)
                .build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(0, notif);

    }

    private Status updateTweet(String tweet){
        if(!mTwitterUtil.hasToken()) return null;
        Twitter client = mTwitterUtil.getTwitterInstance();
        try {
            return client.updateStatus(tweet);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return null;
    }
}
