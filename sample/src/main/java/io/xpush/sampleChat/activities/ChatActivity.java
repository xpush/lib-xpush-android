package io.xpush.sampleChat.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;

import io.xpush.chat.R;
import io.xpush.chat.core.XPushCore;
import io.xpush.chat.fragments.XPushChatFragment;
import io.xpush.chat.models.XPushChannel;
import io.xpush.sampleChat.fragments.ChatFragment;
import io.xpush.sampleChat.fragments.FriendsFragment;

public class ChatActivity extends AppCompatActivity{

    public static final String TAG = ChatActivity.class.getSimpleName();
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mActivity = this;

        ChatFragment f = new ChatFragment();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.list, f, TAG).commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        Bundle bundle = getIntent().getBundleExtra(XPushChannel.CHANNEL_BUNDLE);
        XPushChannel xpushChannel = new XPushChannel(bundle);

        if( xpushChannel.getUsers() != null && xpushChannel.getUsers().size() > 2 ){
            toolbar.setTitle(xpushChannel.getName()+" (" + xpushChannel.getUsers().size() + ")") ;
        } else {
            toolbar.setTitle(xpushChannel.getName()) ;
        }
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.d(TAG, "===== newIntent =====" );

        if (null != intent) {
            Log.d(TAG, intent.toString());
            int defaultValue = 0;
            setIntent(intent);
        }
    }
}