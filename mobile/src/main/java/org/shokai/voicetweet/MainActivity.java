package org.shokai.voicetweet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
public class MainActivity extends GoogleApiClientActivity {

    public final static String TAG = "MainActivity";
    public final static int CODE_TWITTER_LOGIN = 1;

    private TwitterUtil mTwitterUtil;
    private Twitter mTwitter;
    private String mScreenName = null;

    @ViewById(R.id.imageViewProfile)
    ImageView mImageViewProfile;

    @ViewById(R.id.textViewScreenName)
    TextView mTextViewScreenName;

    @OptionsMenuItem(R.id.action_launch_wear)
    MenuItem mItemLaunchWear;

    @OptionsMenuItem(R.id.action_tweet_test)
    MenuItem mItemTweetTest;

    @OptionsMenuItem(R.id.action_logout)
    MenuItem mItemLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTwitterUtil = new TwitterUtil(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!mTwitterUtil.hasToken()) {
            TwitterOAuthActivity_.intent(this).start();
            return;
        }
        mTextViewScreenName.setText("Loading..");
        mTwitter = mTwitterUtil.getTwitterInstance();
        getTwitterScreenNameAsync();
        getTwitterProfileImageAsync();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mItemTweetTest.setVisible(BuildConfig.DEBUG && mTwitterUtil.hasToken());
        mItemLaunchWear.setVisible(mTwitterUtil.hasToken());
        return super.onPrepareOptionsMenu(menu);
    }

    @OptionsItem(R.id.action_logout)
    void logout() {
        mTwitterUtil.logout();
        Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
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
    }

    @OptionsItem(R.id.action_launch_wear)
    void launchWearApp(){
        Log.i(TAG, "launch wear app");
        sendMessageAsync(MessagePath.LAUNCH_APP, "launch wear app");
    }

}
