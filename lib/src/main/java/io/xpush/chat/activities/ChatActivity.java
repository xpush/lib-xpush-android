package io.xpush.chat.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import io.xpush.chat.R;
import io.xpush.chat.fragments.ChatFragment;
import io.xpush.chat.models.XPushChannel;
import io.xpush.chat.persist.XpushContentProvider;

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
        toolbar.setTitle(xpushChannel.getName()) ;
        setSupportActionBar(toolbar);
    }
}