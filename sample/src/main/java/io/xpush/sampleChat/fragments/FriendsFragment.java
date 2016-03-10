package io.xpush.sampleChat.fragments;

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

import org.json.JSONArray;

import java.util.ArrayList;

import io.xpush.chat.core.CallbackEvent;
import io.xpush.chat.core.XPushCore;
import io.xpush.chat.fragments.XPushUsersFragment;
import io.xpush.chat.models.XPushChannel;
import io.xpush.chat.models.XPushUser;
import io.xpush.chat.persist.UserTable;
import io.xpush.chat.persist.XpushContentProvider;
import io.xpush.chat.util.XPushUtils;
import io.xpush.chat.view.adapters.UserCursorAdapter;
import io.xpush.sampleChat.R;
import io.xpush.sampleChat.activities.ChatActivity;

public class FriendsFragment extends XPushUsersFragment {

    private static final String TAG = FriendsFragment.class.getSimpleName();

    private String mTitle;

    @Override
    public void initDataAdapter(){
        mDataAdapter = new UserCursorAdapter(mActivity, null, 0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        final Toolbar mToolbar = ((Toolbar) mActivity.findViewById(R.id.toolbar));

        final MenuItem searchViewItem = mToolbar.getMenu().findItem(R.id.action_search);
        final SearchView mSearchView = (SearchView) searchViewItem.getActionView();

        mDataAdapter.setFilterQueryProvider(new FilterQueryProvider() {

            @Override
            public Cursor runQuery(CharSequence constraint) {

                String[] projection = {
                        UserTable.KEY_ROWID,
                        UserTable.KEY_ID,
                        UserTable.KEY_NAME,
                        UserTable.KEY_IMAGE,
                        UserTable.KEY_MESSAGE,
                        UserTable.KEY_UPDATED
                };

                if( constraint == null ){
                    constraint = "";
                }

                String selection = UserTable.KEY_NAME + " LIKE '%"+constraint.toString() +"%'";
                Cursor cur = mActivity.getContentResolver().query(XpushContentProvider.USER_CONTENT_URI, projection, selection, null, null);
                return cur;
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
                Log.d( TAG, s );
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
    public void getUsers(){

        XPushCore.getInstance().getFriends(new CallbackEvent() {
            @Override
            public void call(Object... args) {
                if (args != null && args.length > 0) {
                    JSONArray users = (JSONArray) args[0];
                    XPushCore.storeFriends(getActivity(), users);
                }
            }
        });
    }

    @Override
    public void onUserItemClick(AdapterView<?> listView, View view, int position, long id){
        Cursor cursor = (Cursor) listView.getItemAtPosition(position);

        XPushUser user = new XPushUser(cursor);

        ArrayList<String> userArray = new ArrayList<String>();
        userArray.add( user.getId() );
        userArray.add(XPushCore.getXpushSession().getId());

        XPushChannel channel = new XPushChannel();
        channel.setId(XPushUtils.generateChannelId(userArray));
        channel.setName(user.getName());
        channel.setUsers(userArray);
        channel.setImage(user.getImage());

        Bundle bundle = channel.toBundle();
        Intent intent = new Intent(mActivity, ChatActivity.class);
        intent.putExtra(channel.CHANNEL_BUNDLE, bundle);
        intent.putExtra("newChannel", true);
        startActivity(intent);
    }
}
