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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import io.xpush.chat.R;

import io.xpush.chat.persist.UserTable;
import io.xpush.chat.persist.XpushContentProvider;
import io.xpush.chat.view.adapters.UserCursorAdapter;

public abstract class XPushUsersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = XPushUsersFragment.class.getSimpleName();

    protected UserCursorAdapter mDataAdapter;
    protected Activity mActivity;
    private TextView mEmptyMsg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mActivity = getActivity();
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setHasOptionsMenu(true);
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

        initDataAdapter();

        final ListView listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(mDataAdapter);

        getLoaderManager().initLoader(0, null, this);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                onUserItemClick(listView, view, position, id );
            }
        });

        getUsers();
    }

    public abstract void initDataAdapter();

    public abstract void getUsers();

    public abstract void onUserItemClick(AdapterView<?> listView, View view, int position, long id);
}
