package io.xpush.chat.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import io.xpush.chat.R;
import io.xpush.chat.fragments.ChannelFragment;
import io.xpush.chat.fragments.ChatFragment;
import io.xpush.chat.services.XPushService;


public class ChatActivity extends AppCompatActivity{

    public static final String TAG = ChannelActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ChatFragment f = new ChatFragment();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.list, f, TAG).commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setTitle(getString(R.string.action_bookmark));
        //toolbar.getBackground().setAlpha(0);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        //ab.setHomeAsUpIndicator(R.drawable.ic_arrow_left);
        ab.setDisplayHomeAsUpEnabled(true);
    }
}
