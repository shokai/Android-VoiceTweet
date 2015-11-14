package org.shokai.voicetweet;

import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_launch_phone_app)
public class LaunchPhoneAppActivity extends GoogleApiClientActivity {

    public final static String TAG = "LaunchPhoneAppActivity";

    @AfterViews
    void showLoginMessage(){
        Toast.makeText(this, "Login Twitter", Toast.LENGTH_LONG).show();
    }

    @Click(R.id.button)
    void onButtonClick(){
        sendMessageAsync(MessagePath.LAUNCH_APP, "launch app");
    }

}
