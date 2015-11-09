package org.shokai.voicetweet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {

    private final static String TAG = "MainActivity";
    public final static int CODE_TWITTER_LOGIN = 1;

    private TwitterUtil mTwitterUtil;
    private Twitter mTwitter;
    private String mScreenName;

    @ViewById(R.id.imageViewProfile)
    ImageView mImageViewProfile;

    @ViewById(R.id.textViewScreenName)
    TextView mTextViewScreenName;

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
            displayTwitterScreenName();
        }
        else{
            mTextViewScreenName.setText(getResources().getText(R.string.text_screen_name));
            mImageViewProfile.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemTweetTest = menu.findItem(R.id.action_tweet_test);
        itemTweetTest.setVisible(BuildConfig.DEBUG && mTwitterUtil.hasToken());

        MenuItem itemLogin = menu.findItem(R.id.action_login);
        itemLogin.setTitle(mTwitterUtil.hasToken() ? "Logout" : "Login");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_login:
                Intent intent = new Intent(this, TwitterOAuthActivity.class);
                startActivityForResult(intent, CODE_TWITTER_LOGIN);
                return true;
            case R.id.action_tweet_test:
                startActivity(new Intent(MainActivity.this, TweetTestActivity_.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayTwitterScreenName() {
        if(mTwitter == null) return;
        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground(Void... params) {
                try {
                    return mScreenName = mTwitter.getScreenName();
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String screen_name) {
                if(mScreenName == null) return;
                mScreenName = screen_name;
                Log.i(TAG, "screen_name: " + screen_name);
                mTextViewScreenName.setText("@" + screen_name);
                displayTwitterProfileImage(screen_name);
            }
        }.execute();
    }

    private void displayTwitterProfileImage(final String screen_name){
        if(mTwitter == null) return;
        new AsyncTask<Void, Void, Bitmap>(){
            @Override
            protected Bitmap doInBackground(Void... params) {
                try {
                    User user = mTwitter.showUser(screen_name);
                    URL imageUrl = new URL(user.getBiggerProfileImageURL());
                    return BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (TwitterException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap profileImage) {
                if(profileImage == null) return;
                Log.i(TAG, "load profile image");
                mImageViewProfile.setImageBitmap(profileImage);
                mImageViewProfile.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

}
