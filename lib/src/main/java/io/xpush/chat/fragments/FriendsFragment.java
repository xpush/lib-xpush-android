package io.xpush.chat.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.github.nkzawa.engineio.client.EngineIOException;
import com.github.nkzawa.socketio.client.Ack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.xpush.chat.ApplicationController;
import io.xpush.chat.R;
import io.xpush.chat.activities.ChatActivity;
import io.xpush.chat.models.XPushChannel;
import io.xpush.chat.models.XPushUser;
import io.xpush.chat.persist.UserTable;
import io.xpush.chat.persist.XpushContentProvider;
import io.xpush.chat.util.XPushUtils;
import io.xpush.chat.view.adapters.UserCursorAdapter;

public class FriendsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = FriendsFragment.class.getSimpleName();

    private UserCursorAdapter mDataAdapter;
    private Activity mActivity;
    private TextView mEmptyMsg;

    public static FriendsFragment newInstance(int page, String title) {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmptyMsg = (TextView) view.findViewById(R.id.emptyMsg);
        displayListView(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
            UserTable.KEY_ROWID,
            UserTable.KEY_ID,
            UserTable.KEY_NAME,
            UserTable.KEY_IMAGE,
            UserTable.KEY_MESSAGE,
            UserTable.KEY_UPDATED
        };

        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                XpushContentProvider.USER_CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data == null || data.getCount() == 0) {
            mEmptyMsg.setVisibility(View.VISIBLE);
        } else {
            mEmptyMsg.setVisibility(View.INVISIBLE);
        }

        mDataAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDataAdapter.swapCursor(null);
    }

    private void displayListView(View view) {

        String[] columns = new String[]{
            UserTable.KEY_ROWID,
            UserTable.KEY_ID,
            UserTable.KEY_NAME,
            UserTable.KEY_IMAGE,
            UserTable.KEY_MESSAGE,
            UserTable.KEY_UPDATED
        };

        mActivity = getActivity();
        mDataAdapter = new UserCursorAdapter(mActivity, null, 0);


        final ListView listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(mDataAdapter);

        getLoaderManager().initLoader(0, null, this);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                XPushUser user = new XPushUser(cursor);

                ArrayList<String> userArray = new ArrayList<String>();
                userArray.add( user.getId() );
                userArray.add(ApplicationController.getInstance().getXpushSession().getId());

                XPushChannel channel = new XPushChannel();
                channel.setId(XPushUtils.generateChannelId( userArray ) );
                channel.setUsers( userArray );

                Bundle bundle = channel.toBundle();
                Intent intent = new Intent(mActivity, ChatActivity.class);
                intent.putExtra(channel.CHANNEL_BUNDLE, bundle);
                startActivity(intent);
            }
        });

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("GR", ApplicationController.getInstance().getXpushSession().getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        ApplicationController.getInstance().getClient().emit("group-list", jsonObject, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];
                if( response.has( "result" ) ){
                    try {
                        JSONArray result = (JSONArray) response.getJSONArray("result");
                        List<ContentValues> valuesToInsert = new ArrayList<ContentValues>();

                        for( int inx = 0 ; inx < result.length() ; inx++ ){
                            JSONObject json =  (JSONObject)result.get(inx);
                            JSONObject data = json.getJSONObject("DT");

                            ContentValues contentValues = new ContentValues();
                            contentValues.put( UserTable.KEY_ID, json.getString("U"));
                            contentValues.put( UserTable.KEY_NAME, data.getString("NM"));
                            contentValues.put( UserTable.KEY_MESSAGE, data.getString("MG"));
                            contentValues.put(UserTable.KEY_IMAGE, data.getString("I"));
                            contentValues.put( XpushContentProvider.SQL_INSERT_OR_REPLACE, true );
                            valuesToInsert.add( contentValues );
                        }

                        synchronized( this ) {
                            getActivity( ).getContentResolver().bulkInsert(XpushContentProvider.USER_CONTENT_URI, valuesToInsert.toArray(new ContentValues[0]));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}