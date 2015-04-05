package org.shokai.voicetweet.lib;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterUtil {

    public interface TwitterInstanceListener{
        public void onCreate(Twitter twitter);
        public void onError(Exception error);
    }

    public static final String TAG = "TwitterUtil";
    public static final String DATAMAP_PATH = "/voicetweet/twitter";
    public static final String DATAMAP_ITEM_ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String DATAMAP_ITEM_ACCESS_TOKEN_SECRET = "ACCESS_TOKEN_SECRET";
    public static final String SHAREDPREFERENCE_KEY = "twitter";

    private Context mContext; // Activity or Service

    public TwitterUtil(Context context){
        mContext = context;
    }

    public Twitter getTwitterInstanceFromLocal(){
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

    public void getTwitterInstance(GoogleApiClient googleClient, final TwitterInstanceListener callback){
        final ConfigurationBuilder confBuilder = new ConfigurationBuilder()
                .setPrettyDebugEnabled(TwitterConfig.DEBUG)
                .setOAuthConsumerKey(getConsumerKey())
                .setOAuthConsumerSecret(getConsumerSecret());

        Wearable.DataApi.getDataItems(googleClient)
                .setResultCallback(new ResultCallback<DataItemBuffer>() {
                    @Override
                    public void onResult(DataItemBuffer dataItems) {
                        String token = null;
                        String tokenSecret = null;
                        for(DataItem dataItem : dataItems){
                            if(dataItem.getUri().getPath().equals(TwitterUtil.DATAMAP_PATH)) {
                                DataMap dataMap = DataMap.fromByteArray(dataItem.getData());
                                token = dataMap.getString(TwitterUtil.DATAMAP_ITEM_ACCESS_TOKEN);
                                tokenSecret = dataMap.getString(TwitterUtil.DATAMAP_ITEM_ACCESS_TOKEN_SECRET);
                            }
                        }
                        if(token != null && tokenSecret != null) {
                            Log.i("getTwitterInstance", "found Token and Secret");
                            confBuilder.setOAuthAccessToken(token)
                                    .setOAuthAccessTokenSecret(tokenSecret);
                            callback.onCreate(new TwitterFactory(confBuilder.build()).getInstance());
                        }
                        else{
                            Log.i("getTwitterInstance", "Login info not found");
                            callback.onError(new Exception("Login info not found"));
                        }
                        dataItems.release();
                    }
                });
    }

    public String getConsumerKey(){
        return TwitterConfig.CONSUMER_KEY;
    }

    public String getConsumerSecret(){
        return TwitterConfig.CONSUMER_SECRET;
    }

    public String getAccessToken(){
        return getSharedPreferences().getString(DATAMAP_ITEM_ACCESS_TOKEN, null);
    }

    public String getAccessTokenSecret(){
        return getSharedPreferences().getString(DATAMAP_ITEM_ACCESS_TOKEN_SECRET, null);
    }

    public SharedPreferences getSharedPreferences(){
        return mContext.getSharedPreferences(SHAREDPREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public void setAccessTokenAndSecret(GoogleApiClient googleApiClient, String token, String tokenSecret){
        getSharedPreferences().edit()
                .putString(DATAMAP_ITEM_ACCESS_TOKEN, token)
                .putString(DATAMAP_ITEM_ACCESS_TOKEN_SECRET, tokenSecret)
                .apply();

        PutDataMapRequest mapReq = PutDataMapRequest.create(TwitterUtil.DATAMAP_PATH);
        mapReq.getDataMap().putString(DATAMAP_ITEM_ACCESS_TOKEN, token);
        mapReq.getDataMap().putString(DATAMAP_ITEM_ACCESS_TOKEN_SECRET, tokenSecret);
        Wearable.DataApi.putDataItem(googleApiClient, mapReq.asPutDataRequest());
    }


    public void logout(GoogleApiClient googleApiClient){
        setAccessTokenAndSecret(googleApiClient, null, null);
    }

    public boolean hasToken(){
        return (getAccessToken() != null) && (getAccessTokenSecret() != null);
    }
}
