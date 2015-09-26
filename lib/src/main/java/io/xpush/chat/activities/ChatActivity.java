package io.xpush.chat.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import io.xpush.chat.R;
import io.xpush.chat.fragments.ChatFragment;
import io.xpush.chat.models.XPushChannel;

public class ChatActivity extends AppCompatActivity{

    public static final String TAG = ChatActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ChatFragment f = new ChatFragment();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.list, f, TAG).commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getBundleExtra(XPushChannel.CHANNEL_BUNDLE);
        XPushChannel xpushChannel = new XPushChannel(bundle);
        toolbar.setTitle(xpushChannel.getName()) ;
    }
}
