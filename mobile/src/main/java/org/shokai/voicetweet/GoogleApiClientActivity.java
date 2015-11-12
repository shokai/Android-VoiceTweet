package org.shokai.voicetweet;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

public class GoogleApiClientActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public final static String TAG = "GoogleApiClientActivity";

    protected GoogleApiClient mGoogleApiClient;

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

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "connection failed");
    }

    public void sendMessageAsync(String path, String msg){
        if(msg == null) return;
        Log.i(TAG, "sendMessage: " + path + " " + msg);
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
                            Log.e(TAG, "sendMessage failed: "+stat.getStatusMessage());
                        }
                        else {
                            Log.i(TAG, "sendMessage success");
                        }
                    }
                });
            }
        }.execute(path, msg);
    }

}
