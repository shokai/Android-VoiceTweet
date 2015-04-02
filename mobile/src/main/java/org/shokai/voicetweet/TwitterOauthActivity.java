package org.shokai.voicetweet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterOauthActivity extends Activity implements View.OnClickListener {

    private final String TAG = "TwitterOauthActivity";
    private Button mButtonLogin;
    private TwitterUtil mTwitterUtil;
    private Twitter mTwitter;
    private String mCallbackUrl;
    private RequestToken mRequestToken;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_oauth);

        mTwitterUtil = new TwitterUtil(this);
        mTwitter = TwitterFactory.getSingleton();
        mTwitter.setOAuthConsumer(
                getResources().getString(R.string.twitter_consumer_key),
                getResources().getString(R.string.twitter_consumer_secret));

        mCallbackUrl = getResources().getString(R.string.twitter_callback_url);

        mButtonLogin = (Button)findViewById(R.id.button_login);
        mButtonLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() != R.id.button_login) return;
        if(!mTwitterUtil.hasToken()){
            authStart();
        }
    }

    public void authStart(){
        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground(Void... params) {
                try {
                    mRequestToken = mTwitter.getOAuthRequestToken(mCallbackUrl);
                    return mRequestToken.getAuthorizationURL();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String url) {
                if(url == null) {

                    Log.i(TAG, "authorization URL not found");
                    Toast.makeText(TwitterOauthActivity.this, "Auth failed", Toast.LENGTH_SHORT);
                    return;
                }
                Log.v("authorization URL", url);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        }.execute();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(intent == null || intent.getData() == null) return;
        if(!intent.getDataString().startsWith(mCallbackUrl)) return;
        String verifier = intent.getData().getQueryParameter("oauth_verifier");
        new AsyncTask<String, Void, AccessToken>(){
            @Override
            protected AccessToken doInBackground(String... params) {
                String verifier = params[0];
                try {
                    return mTwitter.getOAuthAccessToken(mRequestToken, verifier);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(AccessToken accessToken) {
                if(accessToken == null) return;
                authSuccess(accessToken);
            }
        }.execute(verifier);
    }

    private void authSuccess(AccessToken token){
        Toast.makeText(this, "auth success!", Toast.LENGTH_SHORT);
        mTwitterUtil.setAccessToken(token.getToken());
        mTwitterUtil.setAccessTokenSecret(token.getTokenSecret());
    }
}
