package io.xpush.sampleChat.fragments;

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
import io.xpush.chat.fragments.UsersFragment;
import io.xpush.chat.models.XPushChannel;
import io.xpush.chat.models.XPushUser;
import io.xpush.chat.persist.UserTable;
import io.xpush.chat.persist.XpushContentProvider;
import io.xpush.chat.util.XPushUtils;
import io.xpush.chat.view.adapters.UserCursorAdapter;

public class FriendsFragment extends UsersFragment {

    private static final String TAG = FriendsFragment.class.getSimpleName();

    @Override
    public void getUsers(){
        JSONObject jsonObject = new JSONObject();
        JSONObject column = new JSONObject();

        try {
            column.put( "U", true );
            column.put( "DT", true );
            column.put( "A", true );
            column.put( "_id", false );

            jsonObject.put("query", new JSONObject().put("A", getString(R.string.app_id)) );
            jsonObject.put("options", new JSONObject() );
            jsonObject.put("column", column );

        } catch (JSONException e) {
            e.printStackTrace();
        }

        while( !ApplicationController.getInstance().getClient().connected() ){
            Log.d( TAG, "connected : " + ApplicationController.getInstance().getClient().connected() );
        }

        ApplicationController.getInstance().getClient().emit("user-query", jsonObject, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];

                Log.d( TAG, response.toString() );
                if( response.has( "result" ) ){
                    try {
                        JSONArray result = (JSONArray) response.getJSONObject("result").getJSONArray("users");
                        List<ContentValues> valuesToInsert = new ArrayList<ContentValues>();

                        for( int inx = 0 ; inx < result.length() ; inx++ ){
                            JSONObject json =  (JSONObject)result.get(inx);
                            Log.d(TAG, json.toString());

                            ContentValues contentValues = new ContentValues();
                            contentValues.put( UserTable.KEY_ID, json.getString("U"));

                            if( json.has("DT") && !json.isNull("DT")  ){
                                JSONObject data = json.getJSONObject("DT");

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
