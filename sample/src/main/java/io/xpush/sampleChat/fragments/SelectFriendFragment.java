package io.xpush.sampleChat.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FilterQueryProvider;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
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

public class SelectFriendFragment extends XPushUsersFragment {

    private static final String TAG = XPushUsersFragment.class.getSimpleName();

    private String mTitle = "";

    private HashMap<String, XPushUser> mSelectedUserMap;

    private String mChannelId = "";
    private ArrayList<String> mCurrentChannelUsers;

    @Bind(R.id.layoutSearch)
    View layoutSearch;

    @Bind(R.id.editSearch)
    EditText mEditSearch;

    @Override
    public void createDataAdapter(){
        mDataAdapter = new UserCursorAdapter(mActivity, null, 0, UserCursorAdapter.Mode.CHECKABLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.select_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( item.getItemId() == R.id.action_invite ){
            invite();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        Bundle args = getArguments();
        if( args != null ) {
            mChannelId = args.getString("channelId", "");
            mCurrentChannelUsers = args.getStringArrayList("channelUsers");


            Log.d(TAG, mChannelId );
            Log.d(TAG, mCurrentChannelUsers.toString() );
            Log.d(TAG, String.valueOf( mCurrentChannelUsers.size() ) );
        }

        mSelectedUserMap = new HashMap<String, XPushUser>();

        layoutSearch.setVisibility(View.VISIBLE);

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

                if (constraint == null) {
                    constraint = "";
                }

                String selection = UserTable.KEY_NAME + " LIKE '%" + constraint.toString() + "%'" + " OR " +
                        UserTable.KEY_ID + " LIKE '%" + constraint.toString() + "%'";
                Log.d(TAG, selection);
                Cursor cur = mActivity.getContentResolver().query(XpushContentProvider.USER_CONTENT_URI, projection, selection, null, null);
                return cur;
            }
        });

        mEditSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {
                mDataAdapter.getFilter().filter(editable.toString());
                mDataAdapter.notifyDataSetChanged();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }
        });
    }

    @Override
    public void getUsers(){
    }

    @Override
    public void onUserItemClick(AdapterView<?> listView, View view, int position, long id){

        Cursor cursor = (Cursor) listView.getItemAtPosition(position);
        XPushUser user = new XPushUser(cursor);

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);

        if( checkBox.isChecked() ){
            checkBox.setChecked(false);
            if( mSelectedUserMap.containsKey(user.getId())) {
                mSelectedUserMap.remove(user.getId());
            }
        } else {
            checkBox.setChecked(true);
            mSelectedUserMap.put(user.getId(), user);
        }
    }

    private void invite(){

        ArrayList<String> userArray = new ArrayList<String>();
        ArrayList<String> userNameArray = new ArrayList<String>();

        for (String userId : mSelectedUserMap.keySet()) {
            userArray.add(userId);
            userNameArray.add(mSelectedUserMap.get(userId).getName());
        }

        if( !"".equals( mChannelId ) && mCurrentChannelUsers != null && mCurrentChannelUsers.size() > 2 ){

            XPushChannel channel = new XPushChannel();
            channel.setId(mChannelId);

            // create new channel;
            Intent intent = new Intent();
            intent.putStringArrayListExtra("userArray", userArray);
            intent.putStringArrayListExtra("userNameArray", userNameArray);

            mActivity.setResult(mActivity.RESULT_OK, intent);
            mActivity.finish();
        } else {

            if( mCurrentChannelUsers != null ){
                userArray.addAll(mCurrentChannelUsers);
            }

            userArray.add(XPushCore.getInstance().getXpushSession().getId());

            XPushChannel channel = new XPushChannel();
            channel.setId(XPushUtils.generateChannelId(userArray));
            channel.setName(TextUtils.join(",", userNameArray) + " (" + userNameArray.size() + ")");
            channel.setUsers(userArray);
            //channel.setImage(user.getImage());

            Bundle bundle = channel.toBundle();
            Intent intent = new Intent(mActivity, ChatActivity.class);
            intent.putExtra(channel.CHANNEL_BUNDLE, bundle);
            intent.putExtra("newChannel", true);
            startActivity(intent);

            mActivity.setResult(mActivity.RESULT_OK, intent);
            mActivity.finish();
        }
    }
}