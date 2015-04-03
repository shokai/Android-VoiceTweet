package org.shokai.voicetweet;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterUtil {

    private Context mContext; // Activity or Service
    private Properties mProperties; // save consumer key & secret
    private SharedPreferences mSharedPreferences; // save access token & secret

    public TwitterUtil(Context context){
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences("twitter", Context.MODE_PRIVATE);
        mProperties = new Properties();
        try {
            InputStream is = mContext.getResources().openRawResource(R.raw.twitter4j);
            mProperties.load(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Twitter getTwitterInstance(){
        ConfigurationBuilder confBuilder = new ConfigurationBuilder()
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
        return mProperties.getProperty("oauth.consumerKey");
    }

    public String getConsumerSecret(){
        return mProperties.getProperty("oauth.consumerSecret");
    }

    public String getAccessToken(){
        return mSharedPreferences.getString("ACCESS_TOKEN", null);
    }

    public String getAccessTokenSecret(){
        return mSharedPreferences.getString("ACCESS_TOKEN_SECRET", null);
    }

    public void setAccessToken(String token){
        mSharedPreferences.edit().putString("ACCESS_TOKEN", token).apply();
    }

    public void setAccessTokenSecret(String tokenSecret){
        mSharedPreferences.edit().putString("ACCESS_TOKEN_SECRET", tokenSecret).apply();
    }

    public void logout(){
        setAccessToken(null);
        setAccessTokenSecret(null);
    }

    public boolean hasToken(){
        return (getAccessToken() != null) && (getAccessTokenSecret() != null);
    }
}
