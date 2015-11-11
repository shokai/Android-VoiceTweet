package org.shokai.voicetweet;

import android.app.Activity;
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
    private String mScreenName = null;

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
            mTextViewScreenName.setText("Loading..");
            mTwitter = mTwitterUtil.getTwitterInstance();
            getTwitterScreenNameAsync();
            getTwitterProfileImageAsync();
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
        TwitterOAuthActivity_.intent(this).startForResult(CODE_TWITTER_LOGIN);
    }

    @OptionsItem(R.id.action_tweet_test)
    void tweetTest(){
        TweetTestActivity_.intent(this).start();
    }

    @Background(serial = "twitter")
    void getTwitterScreenNameAsync(){
        if(mTwitter == null) return;
        try {
            mScreenName = mTwitter.getScreenName();
            Log.i(TAG, "screen_name: @" + mScreenName);
            displayTwitterScreenName(mScreenName);

        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    @UiThread
    void displayTwitterScreenName(String name){
        mTextViewScreenName.setText("@" + name);
    }

    @Background(serial = "twitter")
    void getTwitterProfileImageAsync() {
        Log.i(TAG, "getTWitterProfileImageAsync()");
        if(mScreenName == null) return;
        try {
            User user = mTwitter.showUser(mScreenName);
            URL imageUrl = new URL(user.getBiggerProfileImageURL());
            Bitmap profileImage = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());
            Log.i(TAG, "load profile image");
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
        mImageViewProfile.setImageBitmap(image);
        mImageViewProfile.setVisibility(View.VISIBLE);
    }

}
