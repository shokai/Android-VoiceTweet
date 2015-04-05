package org.shokai.voicetweet.lib;

import android.content.Context;
import android.content.SharedPreferences;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterUtil {

    private Context mContext; // Activity or Service

    public TwitterUtil(Context context){
        mContext = context;
    }

    public Twitter getTwitterInstance(){
        ConfigurationBuilder confBuilder = new ConfigurationBuilder()
                .setPrettyDebugEnabled(TwitterConfig.DEBUG)
                .setOAuthConsumerKey(getConsumerKey())
                .setOAuthConsumerSecret(getConsumerSecret());

        if(hasToken()) {
            confBuilder
                    .setOAuthAccessToken(getAccessToken())
                    .setOAuthAccessTokenSecret(getAccessTokenSecret());
        }
        return new TwitterFactory(confBuilder.build()).getInstance();
    }

    public String getConsumerKey(){
        return TwitterConfig.CONSUMER_KEY;
    }

    public String getConsumerSecret(){
        return TwitterConfig.CONSUMER_SECRET;
    }

    public String getAccessToken(){
        return getSharedPreferences().getString("ACCESS_TOKEN", null);
    }

    public SharedPreferences getSharedPreferences(){
        return mContext.getSharedPreferences("twitter", Context.MODE_PRIVATE);
    }

    public String getAccessTokenSecret(){
        return getSharedPreferences().getString("ACCESS_TOKEN_SECRET", null);
    }

    public void setAccessToken(String token){
        getSharedPreferences().edit().putString("ACCESS_TOKEN", token).apply();
    }

    public void setAccessTokenSecret(String tokenSecret){
        getSharedPreferences().edit().putString("ACCESS_TOKEN_SECRET", tokenSecret).apply();
    }

    public void logout(){
        setAccessToken(null);
        setAccessTokenSecret(null);
    }

    public boolean hasToken(){
        return (getAccessToken() != null) && (getAccessTokenSecret() != null);
    }
}
