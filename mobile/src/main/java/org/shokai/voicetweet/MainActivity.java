package org.shokai.voicetweet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.menu_main)
public class MainActivity extends Activity {

    private final static String TAG = "MainActivity";
    public final static int CODE_TWITTER_LOGIN = 1;

    private TwitterUtil mTwitterUtil;
    private Twitter mTwitter;

    @ViewById(R.id.imageViewProfile)
    ImageView mImageViewProfile;

    @ViewById(R.id.textViewScreenName)
    TextView mTextViewScreenName;

    @OptionsMenuItem(R.id.action_tweet_test)
    MenuItem mItemTweetTest;

    @OptionsMenuItem(R.id.action_login)
    MenuItem mItemLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTwitterUtil = new TwitterUtil(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mTwitterUtil.hasToken()){
            mTwitter = mTwitterUtil.getTwitterInstance();
            getTwitterScreenNameAsync();
        }
        else{
            mTextViewScreenName.setText(getResources().getText(R.string.text_screen_name));
            mImageViewProfile.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mItemTweetTest.setVisible(BuildConfig.DEBUG && mTwitterUtil.hasToken());
        mItemLogin.setTitle(mTwitterUtil.hasToken() ? "Logout" : "Login");
        return super.onPrepareOptionsMenu(menu);
    }

    @OptionsItem(R.id.action_login)
    void login(){
        Intent intent = new Intent(this, TwitterOAuthActivity.class);
        startActivityForResult(intent, CODE_TWITTER_LOGIN);
    }

    @OptionsItem(R.id.action_tweet_test)
    void tweetTest(){
        TweetTestActivity_.intent(this).start();
    }

    @Background
    void getTwitterScreenNameAsync(){
        if(mTwitter == null) return;
        try {
            String name = mTwitter.getScreenName();
            displayTwitterScreenName(name);
            getTwitterProfileImageAsync(name);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    @UiThread
    void displayTwitterScreenName(String name){
        Log.i(TAG, "screen_name: " + name);
        mTextViewScreenName.setText("@" + name);
    }

    @Background
    void getTwitterProfileImageAsync(String screenName) {
        try {
            User user = mTwitter.showUser(screenName);
            URL imageUrl = new URL(user.getBiggerProfileImageURL());
            Bitmap profileImage = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());
            displayTwitterProfileImage(profileImage);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (TwitterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @UiThread
    void displayTwitterProfileImage(Bitmap image){
        Log.i(TAG, "load profile image");
        mImageViewProfile.setImageBitmap(image);
        mImageViewProfile.setVisibility(View.VISIBLE);
    }

}
