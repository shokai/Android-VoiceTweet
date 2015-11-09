package org.shokai.voicetweet;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.androidannotations.annotations.EActivity;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

@EActivity(R.layout.activity_twitter_oauth)
public class TwitterOAuthActivity extends Activity {

    private final String TAG = "TwitterOauthActivity";

    private TwitterUtil mTwitterUtil;
    private Twitter mTwitter;
    private String mCallbackUrl;
    private RequestToken mRequestToken;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTwitterUtil = new TwitterUtil(this);
        if(mTwitterUtil.hasToken()){
            mTwitterUtil.logout();
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mTwitter = mTwitterUtil.getTwitterInstance();
        Resources resources = getResources();
        mCallbackUrl = resources.getString(R.string.auth_scheme)+"://"+resources.getString(R.string.auth_host);
        authStart();
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
                    Log.e(TAG, "authorization URL not found");
                    Toast.makeText(TwitterOAuthActivity.this, "Auth failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i(TAG, "open authorization URL: " + url);
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
        Toast.makeText(this, "Login", Toast.LENGTH_SHORT).show();
        mTwitterUtil.setAccessToken(token.getToken());
        mTwitterUtil.setAccessTokenSecret(token.getTokenSecret());
        finish();
    }
}
