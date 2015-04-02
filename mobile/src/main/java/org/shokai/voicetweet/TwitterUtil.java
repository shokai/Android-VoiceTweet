package org.shokai.voicetweet;

import android.content.Context;
import android.content.SharedPreferences;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterUtil {

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public TwitterUtil(Context context){
        mContext = context;
    }

    public Twitter getTwitterInstance(){
        ConfigurationBuilder confBuilder = new ConfigurationBuilder()
                .setOAuthConsumerKey(mContext.getResources().getString(R.string.twitter_consumer_key))
                .setOAuthConsumerSecret(mContext.getResources().getString(R.string.twitter_consumer_secret));
        if(hasToken()) {
            confBuilder.setOAuthAccessToken(getAccessToken()).setOAuthAccessTokenSecret(getAccessTokenSecret());
        }
        return new TwitterFactory(confBuilder.build()).getInstance();
    }

    public SharedPreferences getPreference(){
        if(mSharedPreferences != null) return mSharedPreferences;
        return mContext.getSharedPreferences("twitter", Context.MODE_PRIVATE);
    }

    public String getAccessToken(){
        return getPreference().getString("ACCESS_TOKEN", null);
    }

    public String getAccessTokenSecret(){
        return getPreference().getString("ACCESS_TOKEN_SECRET", null);
    }

    public void setAccessToken(String token){
        getPreference().edit().putString("ACCESS_TOKEN", token).commit();
    }

    public void setAccessTokenSecret(String tokenSecret){
        getPreference().edit().putString("ACCESS_TOKEN_SECRET", tokenSecret).commit();
    }

    public void logout(){
        setAccessToken(null);
        setAccessTokenSecret(null);
    }

    public boolean hasToken(){
        return (getAccessToken() != null) && (getAccessTokenSecret() != null);
    }
}
