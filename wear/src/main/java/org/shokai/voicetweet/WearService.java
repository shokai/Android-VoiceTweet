package org.shokai.voicetweet;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;

public class WearService extends GoogleApiClientService {

    public final static String TAG = "WearService";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.v(TAG, "onMessageReceived");
        switch (messageEvent.getPath()){
            case MessagePath.LAUNCH_APP:
                Log.i(TAG, "launch wear app");
                Intent intent = new Intent(this, TweetActivity_.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
        }
    }

}
