package io.xpush.sampleChat.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.Ack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.xpush.chat.ApplicationController;
import io.xpush.chat.models.XPushUser;
import io.xpush.chat.persist.UserTable;
import io.xpush.chat.persist.XpushContentProvider;
import io.xpush.chat.view.listeners.RecyclerOnScrollListener;
import io.xpush.sampleChat.R;
import io.xpush.sampleChat.adapters.SearchUserListAdapter;

public class SearchUserFragment extends Fragment  {

    private static final String TAG = SearchUserFragment.class.getSimpleName();
    private static final int PAGE_SIZE = 50;

    private List<XPushUser> mXpushUsers = new ArrayList<XPushUser>();
    private SearchUserListAdapter mAdapter;

    private Activity mActivity;
    private String mUsername;
    private String mSearchKey = "";
    private int mViewPage;

    private LinearLayoutManager mLayoutManager;

    private RecyclerOnScrollListener mOnScrollListener;
    private LoadMoreHandler mHandler;

    @Bind(R.id.tvMessage)
    TextView mTvMessage;

    @Bind(R.id.listView)
    RecyclerView mRecyclerView;

    @Bind(R.id.editSearch)
    EditText mEditSearch;

    @Bind(R.id.iconSearch)
    ImageView mIconSearch;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAdapter = new SearchUserListAdapter(this, mXpushUsers);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mActivity = getActivity();
        mUsername = ApplicationController.getInstance().getXpushSession().getId();

        View view = inflater.inflate(R.layout.fragment_search_users, container, false);
        ButterKnife.bind(this, view);

        mEditSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
        });

        mHandler = new LoadMoreHandler();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewPage = 1;
        displayListView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @OnClick(R.id.iconSearch)
    public void search() {
        mViewPage = 1;
        mSearchKey = "%" + mEditSearch.getText().toString() + "%";
        getUsers(mViewPage);
    }

    private void displayListView() {
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mOnScrollListener = new RecyclerOnScrollListener(mLayoutManager, RecyclerOnScrollListener.RecylclerDirection.DOWN) {
            @Override
            public void onLoadMore(int currentPage) {
                Log.d(TAG, " onLoadMore : " + currentPage);
                getUsers(++mViewPage);
            }
        };

        mRecyclerView.addOnScrollListener(mOnScrollListener);
    }

    public void getUsers(int page){
        JSONObject jsonObject = new JSONObject();
        JSONObject column = new JSONObject();
        JSONObject options = new JSONObject();
        JSONObject query = new JSONObject();
        JSONArray innerQuerys = new JSONArray();

        try {
            innerQuerys.put( new JSONObject().put("DT.NM", mSearchKey) );
            innerQuerys.put( new JSONObject().put("U", mSearchKey) );

            if( !"".equals( mSearchKey.trim() ) ) {
                query.put("$or", innerQuerys);
            }

            column.put( "U", true );
            column.put( "DT", true );
            column.put( "A", true );
            column.put( "_id", false );

            options.put( "pageNum", mViewPage );
            options.put( "pageSize", PAGE_SIZE );
            options.put( "skipCount", true );
            options.put( "sortBy", "DT.NM" );

            jsonObject.put("options", options );
            jsonObject.put("column", column );
            jsonObject.put("query", query);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApplicationController.getInstance().getClient().emit("user-query", jsonObject, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];
                Log.d(TAG, response.toString());
                if (response.has("result")) {
                    try {
                        JSONArray result = (JSONArray) response.getJSONObject("result").getJSONArray("users");

                        ArrayList<XPushUser> users = new ArrayList<XPushUser>();

                        for (int inx = 0; inx < result.length(); inx++) {
                            JSONObject json = (JSONObject) result.get(inx);
                            Log.d(TAG, json.toString());

                            XPushUser xpushUser = new XPushUser();

                            xpushUser.setId(json.getString("U"));

                            if (json.has("DT") && !json.isNull("DT")) {
                                Object obj = json.get("DT");
                                JSONObject data = null;
                                if (obj instanceof JSONObject) {
                                    data = (JSONObject) obj;
                                } else if (obj instanceof String) {
                                    data = new JSONObject((String) obj);
                                }

                                if (data.has("NM")) {
                                    xpushUser.setName(data.getString("NM"));
                                } else {
                                    xpushUser.setName(json.getString("U"));
                                }
                                if (data.has("MG")) {
                                    xpushUser.setMessage(data.getString("MG"));
                                }
                                if (data.has("I")) {
                                    xpushUser.setImage(data.getString("I"));
                                }
                            } else {
                                xpushUser.setName(json.getString("U"));
                            }

                            users.add(xpushUser);
                        }

                        if (users.size() > 0) {
                            mXpushUsers.addAll(users);
                            mHandler.sendEmptyMessage(0);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void addFriend(final XPushUser user){
        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();

        try {
            array.put( user.getId() );

            jsonObject.put("GR", ApplicationController.getInstance().getXpushSession().getId()  );
            jsonObject.put("U", array );

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, jsonObject.toString());

        ApplicationController.getInstance().getClient().emit("group-add", jsonObject, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];
                Log.d(TAG, response.toString());
                try {
                    if( "ok".equalsIgnoreCase(response.getString("status")) ){

                        ContentValues contentValues = new ContentValues();
                        contentValues.put(UserTable.KEY_ID, user.getId());
                        contentValues.put(UserTable.KEY_NAME, user.getName());
                        contentValues.put(UserTable.KEY_MESSAGE, user.getMessage());
                        contentValues.put(UserTable.KEY_IMAGE, user.getImage());

                        contentValues.put( XpushContentProvider.SQL_INSERT_OR_REPLACE, true );
                        getActivity( ).getContentResolver().insert(XpushContentProvider.USER_CONTENT_URI, contentValues);

                        mActivity.finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Handler 클래스
    class LoadMoreHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0:
                    mTvMessage.setVisibility(View.INVISIBLE);
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
}
