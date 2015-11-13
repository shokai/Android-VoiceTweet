package org.shokai.voicetweet;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_launch_phone_app)
public class LaunchPhoneAppActivity extends GoogleApiClientActivity implements
        MessageApi.MessageListener {

    public final static String TAG = "LaunchPhoneAppActivity";

    @AfterViews
    void showLoginMessage(){
        Toast.makeText(this, "Login Twitter", Toast.LENGTH_LONG).show();
    }

    @Click(R.id.button)
    void onButtonClick(){
        sendMessageAsync(MessagePath.LAUNCH_APP, "launch app");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleApiClient Connected");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    protected void onStop() {
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        super.onStop();
    }

    /*
     * Message from Handheld
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.v(TAG, "onMessageReceived");
        String res = new String(messageEvent.getData());
        Intent intent = new Intent(this, ConfirmationActivity.class);

        switch (messageEvent.getPath()) {
            case MessagePath.LAUNCH_APP_SUCCESS:
                Log.i(TAG, "Launch App Success: " + res);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, res);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
                break;
            case MessagePath.LAUNCH_APP_FAILED:
                Log.i(TAG, "Launch App Failed: " + res);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, res);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
                break;
        }
        startActivity(intent);
    }
}
