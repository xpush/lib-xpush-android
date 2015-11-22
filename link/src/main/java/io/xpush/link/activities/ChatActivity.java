package io.xpush.link.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import io.xpush.chat.fragments.XPushChatFragment;
import io.xpush.chat.models.XPushChannel;
import io.xpush.link.R;

public class ChatActivity extends AppCompatActivity{

    public static final String TAG = ChatActivity.class.getSimpleName();
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mActivity = this;

        XPushChatFragment f = new XPushChatFragment();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.list, f, TAG).commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        Bundle bundle = getIntent().getBundleExtra(XPushChannel.CHANNEL_BUNDLE);
        XPushChannel xpushChannel = new XPushChannel(bundle);
        toolbar.setTitle(xpushChannel.getName()) ;
        setSupportActionBar(toolbar);
    }
}