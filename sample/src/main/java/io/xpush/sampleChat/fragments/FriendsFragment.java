package io.xpush.sampleChat.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FilterQueryProvider;

import com.github.nkzawa.socketio.client.Ack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.xpush.chat.ApplicationController;
import io.xpush.chat.fragments.UsersFragment;
import io.xpush.chat.persist.UserTable;
import io.xpush.chat.persist.XpushContentProvider;
import io.xpush.sampleChat.R;

public class FriendsFragment extends UsersFragment {

    private static final String TAG = FriendsFragment.class.getSimpleName();

    private String mTitle = "";

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
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("GR", ApplicationController.getInstance().getXpushSession().getId() );

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApplicationController.getInstance().getClient().emit("group-list", jsonObject, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];

                Log.d( TAG, response.toString() );
                if( response.has( "result" ) ){
                    try {

                        JSONArray result = (JSONArray) response.getJSONArray("result");
                        List<ContentValues> valuesToInsert = new ArrayList<ContentValues>();

                        for( int inx = 0 ; inx < result.length() ; inx++ ){
                            JSONObject json =  (JSONObject)result.get(inx);
                            Log.d(TAG, json.toString());

                            ContentValues contentValues = new ContentValues();
                            contentValues.put( UserTable.KEY_ID, json.getString("U"));

                            if( json.has("DT") && !json.isNull("DT")  ){
                                Object obj = json.get("DT");
                                JSONObject data = null;
                                if( obj instanceof JSONObject ){
                                    data = (JSONObject) obj;
                                } else if ( obj instanceof String){
                                    data = new JSONObject( (String)obj );
                                }

                                if( data.has("NM")) {
                                    contentValues.put(UserTable.KEY_NAME, data.getString("NM"));
                                }
                                if( data.has("MG")) {
                                    contentValues.put(UserTable.KEY_MESSAGE, data.getString("MG"));
                                }
                                if( data.has("I")) {
                                    contentValues.put(UserTable.KEY_IMAGE, data.getString("I"));
                                }
                            } else {
                                contentValues.put(UserTable.KEY_NAME, json.getString("U"));
                            }

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
