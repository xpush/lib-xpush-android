package io.xpush.chat.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import io.xpush.chat.R;
import io.xpush.chat.activities.ChatActivity;
import io.xpush.chat.models.XPushChannel;
import io.xpush.chat.persist.ChannelTable;
import io.xpush.chat.persist.XpushContentProvider;
import io.xpush.chat.views.adapters.ChannelCursorAdapter;

/**
 * Created by luffy on 2015-07-05.
 */
public class ChannelFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ChannelFragment.class.getSimpleName();

    private ChannelCursorAdapter mDataAdapter;
    private Activity mActivity;
    private TextView mEmptyMsg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_channel, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmptyMsg = (TextView) getActivity().findViewById(R.id.emptyMsg);
        displayListView();
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
            ChannelTable.KEY_ROWID,
            ChannelTable.KEY_ID,
            ChannelTable.KEY_NAME,
            ChannelTable.KEY_USERS,
            ChannelTable.KEY_IMAGE,
            ChannelTable.KEY_COUNT,
            ChannelTable.KEY_MESSAGE,
            ChannelTable.KEY_UPDATED
        };

        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                XpushContentProvider.CHANNEL_CONTENT_URI, projection, null, null, null);
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

    private void displayListView() {

        String[] columns = new String[]{
            ChannelTable.KEY_ROWID,
            ChannelTable.KEY_ID,
            ChannelTable.KEY_NAME,
            ChannelTable.KEY_USERS,
            ChannelTable.KEY_IMAGE,
            ChannelTable.KEY_COUNT,
            ChannelTable.KEY_MESSAGE,
            ChannelTable.KEY_UPDATED
        };

        mActivity = getActivity();
        mDataAdapter = new ChannelCursorAdapter(mActivity, null, 0);


        final ListView listView = (ListView) getActivity().findViewById(R.id.list_bookmark);
        listView.setAdapter(mDataAdapter);

        getLoaderManager().initLoader(0, null, this);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                XPushChannel xpushChannel = new XPushChannel(cursor);


                Bundle bundle = xpushChannel.toBundle();

                Intent intent = new Intent(mActivity, ChatActivity.class);
                intent.putExtra(xpushChannel.CHANNEL_BUNDLE, bundle);
                startActivity(intent);
            }
        });
    }
}
