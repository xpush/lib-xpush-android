package io.xpush.sampleChat.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import io.xpush.chat.fragments.XPushChannelsFragment;
import io.xpush.chat.models.XPushChannel;
import io.xpush.sampleChat.activities.ChatActivity;


public class ChannelsFragment extends XPushChannelsFragment {

    private static final String TAG = ChannelsFragment.class.getSimpleName();

    private String mTitle = "";

    @Override
    public void onChannelItemClick(AdapterView<?> listView, View view, int position, long id) {
        Cursor cursor = (Cursor) listView.getItemAtPosition(position);

        XPushChannel xpushChannel = new XPushChannel(cursor);

        Bundle bundle = xpushChannel.toBundle();

        Intent intent = new Intent(mActivity, ChatActivity.class);
        intent.putExtra(xpushChannel.CHANNEL_BUNDLE, bundle);
        startActivity(intent);
    }
}