package io.xpush.chat.fragments;

import android.app.Activity;
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
import io.xpush.chat.persist.ChannelTable;
import io.xpush.chat.persist.XpushContentProvider;
import io.xpush.chat.view.adapters.ChannelCursorAdapter;

public abstract class XPushChannelsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = XPushChannelsFragment.class.getSimpleName();

    protected Activity mActivity;
    protected ChannelCursorAdapter mDataAdapter;
    private TextView mEmptyMsg;

    protected String[] mProjection = {
        ChannelTable.KEY_ROWID,
                ChannelTable.KEY_ID,
                ChannelTable.KEY_NAME,
                ChannelTable.KEY_USERS,
                ChannelTable.KEY_IMAGE,
                ChannelTable.KEY_COUNT,
                ChannelTable.KEY_MESSAGE,
                ChannelTable.KEY_UPDATED
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_channel, container, false);
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
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                XpushContentProvider.CHANNEL_CONTENT_URI, mProjection, null, null, null);
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


        final ListView listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(mDataAdapter);

        getLoaderManager().initLoader(0, null, this);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                onChannelItemClick(listView, view, position, id );
            }
        });
    }

    public abstract void onChannelItemClick(AdapterView<?> listView, View view, int position, long id);
}
