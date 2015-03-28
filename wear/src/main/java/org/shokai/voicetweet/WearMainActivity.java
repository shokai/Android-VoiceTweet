package org.shokai.voicetweet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class WearMainActivity extends Activity {

    private TextView mTextView;
    private TextView mButton;
    final private int CODE_RECOGNIZE_SPEECH = 567;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mButton = (Button) stub.findViewById(R.id.button);
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(WearMainActivity.this, "click", Toast.LENGTH_SHORT).show();
                        recognizeSpeech();
                    }
                });
            }
        });
    }

    private void recognizeSpeech(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, CODE_RECOGNIZE_SPEECH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CODE_RECOGNIZE_SPEECH &&
           resultCode == RESULT_OK){
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String res = results.get(0);
            mTextView.setText(res);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
