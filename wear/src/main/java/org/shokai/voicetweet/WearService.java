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

public class WearService extends WearableListenerService implements
        ResultCallback<MessageApi.SendMessageResult> {

    public final static String TAG = "WearService";

    private GoogleApiClient mGoogleApiClient;


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
        switch (messageEvent.getPath()){
            case MessagePath.LAUNCH_APP:
                Log.i(TAG, "launch wear app");
                Intent intent = new Intent(this, TweetActivity_.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
        }
    }

    private void sendMessage(String path, String msg, ResultCallback<MessageApi.SendMessageResult> callback){
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
            Log.e(TAG, "sendMessageToHandheld failed: " + stat.getStatusMessage());
        }
        else {
            Log.i(TAG, "sendMessageToHandheld success");
        }
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }
}
