package io.xpush.sampleChat.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.List;

import io.xpush.chat.fragments.ChatFragment;
import io.xpush.chat.view.activities.AppCompatPreferenceActivity;
import io.xpush.sampleChat.R;
import io.xpush.sampleChat.fragments.ProfileFragment;
import io.xpush.sampleChat.fragments.SettingsFragment;

/**
 * Created by James on 2015-09-05.
 */
public class ProfileActivity extends AppCompatActivity {

    public static final String TAG = ProfileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(io.xpush.chat.R.layout.activity_profile);

        ProfileFragment f = new ProfileFragment();

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
