package org.shokai.voicetweet;

import android.app.Activity;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_launch_phone_app)
public class LaunchPhoneAppActivity extends Activity {

    public final static String TAG = "LaunchPhoneAppActivity";

    @Click(R.id.button)
    void onButtonClick(){

    }

}
