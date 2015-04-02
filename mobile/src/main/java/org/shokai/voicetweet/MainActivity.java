package org.shokai.voicetweet;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;


public class MainActivity extends Activity {

    private TwitterUtil mTwitterUtil;
    private Twitter mTwitter;
    private Button mButton;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTwitterUtil = new TwitterUtil(this);
        mButton = (Button) findViewById(R.id.button);
        mEditText = (EditText) findViewById(R.id.editText);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tweet = mEditText.getText().toString();
                new AsyncTask<String, Void, Status>(){
                    @Override
                    protected twitter4j.Status doInBackground(String... params) {
                        String tweet = params[0];
                        try {
                            return mTwitter.updateStatus(tweet);
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(twitter4j.Status status) {
                        if(status != null && status.getId() > 0){
                            Toast.makeText(MainActivity.this, "success!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute(tweet);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mTwitterUtil.hasToken()){
            mTwitter = mTwitterUtil.getTwitterInstance();
            mButton.setVisibility(View.VISIBLE);
            mEditText.setVisibility(View.VISIBLE);
        }
        else{
            mButton.setVisibility(View.INVISIBLE);
            mEditText.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if(!mTwitterUtil.hasToken()) {
            MenuItem itemSettings = menu.findItem(R.id.action_settings);
            itemSettings.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_login:
                Intent intent = new Intent(this, TwitterOauthActivity.class);
                startActivityForResult(intent, 0);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
