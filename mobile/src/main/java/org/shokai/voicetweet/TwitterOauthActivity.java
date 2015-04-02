package org.shokai.voicetweet;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TwitterOauthActivity extends Activity implements View.OnClickListener {

    private Button buttonLogin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_oauth);

        buttonLogin = (Button)findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() != R.id.button_login) return;
        
    }
}
