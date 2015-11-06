package io.xpush.link.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;

import io.xpush.chat.fragments.XPushChannelsFragment;
import io.xpush.chat.models.XPushChannel;
import io.xpush.chat.persist.UserTable;
import io.xpush.chat.persist.XpushContentProvider;
import io.xpush.link.R;
import io.xpush.link.activities.ChatActivity;

public class ChannelsFragment extends XPushChannelsFragment {

    private static final String TAG = ChannelsFragment.class.getSimpleName();

    private String mTitle = "";

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.channel_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        final Toolbar mToolbar = ((Toolbar) mActivity.findViewById(R.id.toolbar));

        final MenuItem searchViewItem = mToolbar.getMenu().findItem(R.id.action_search);
        final SearchView mSearchView = (SearchView) searchViewItem.getActionView();

        mDataAdapter.setFilterQueryProvider(new FilterQueryProvider() {

            @Override
            public Cursor runQuery(CharSequence constraint) {

                if( constraint == null ){
                    constraint = "";
                }

                String selection = UserTable.KEY_NAME + " LIKE '%"+constraint.toString() +"%'";
                Cursor cursor = mActivity.getContentResolver().query(XpushContentProvider.CHANNEL_CONTENT_URI, mProjection, selection, null, null);
                return cursor;
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                Log.d(TAG, s);
                mDataAdapter.getFilter().filter(s);
                mDataAdapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mDataAdapter.getFilter().filter(s);
                mDataAdapter.notifyDataSetChanged();
                return false;
            }
        });

        mSearchView.setOnSearchClickListener(new SearchView.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTitle = mToolbar.getTitle().toString();
                mToolbar.setTitle("");
                mSearchView.setMaxWidth(10000);
            }
        });

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mToolbar.setTitle(mTitle);
                return false;
            }
        });
    }

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