package io.xpush.sampleChat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.Ack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.xpush.chat.ApplicationController;
import io.xpush.sampleChat.R;
import io.xpush.sampleChat.fragments.ProfileFragment;
import io.xpush.sampleChat.fragments.SearchUserFragment;

/**
 * Created by James on 2015-09-05.
 */
public class SearchUserActivity extends AppCompatActivity {

    public static final String TAG = SearchUserActivity.class.getSimpleName();

    private TextView mTextView;
    private SearchUserFragment f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        f = new SearchUserFragment();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.content, f, TAG).commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        //ab.setHomeAsUpIndicator(R.drawable.ic_arrow_left);
        //ab.setDisplayHomeAsUpEnabled(true);
    }

}
