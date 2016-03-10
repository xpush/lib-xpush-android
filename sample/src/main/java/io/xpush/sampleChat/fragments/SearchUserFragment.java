package io.xpush.sampleChat.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.xpush.chat.core.CallbackEvent;
import io.xpush.chat.core.XPushCore;
import io.xpush.chat.models.XPushUser;
import io.xpush.chat.view.listeners.RecyclerOnScrollListener;
import io.xpush.sampleChat.R;
import io.xpush.sampleChat.adapters.SearchUserListAdapter;

public class SearchUserFragment extends Fragment  {

    private static final String TAG = SearchUserFragment.class.getSimpleName();
    private static final int PAGE_SIZE = 50;

    private List<XPushUser> mXpushUsers = new ArrayList<XPushUser>();
    private SearchUserListAdapter mAdapter;

    private Activity mActivity;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();

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

        mEditSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.search || id == EditorInfo.IME_NULL) {
                    search();
                    return true;
                }
                return false;
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
        mSearchKey = mEditSearch.getText().toString();
        searchUsers(mViewPage, true);
    }

    private void displayListView() {
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mOnScrollListener = new RecyclerOnScrollListener(mLayoutManager, RecyclerOnScrollListener.RecylclerDirection.DOWN) {
            @Override
            public void onLoadMore(int currentPage) {
                Log.d(TAG, " onLoadMore : " + currentPage);
                searchUsers(++mViewPage, false);
            }
        };

        mRecyclerView.addOnScrollListener(mOnScrollListener);
    }

    public void searchUsers(int page, final boolean resetFlag) {
        XPushCore.searchUser(getActivity(), mSearchKey, page, PAGE_SIZE, new CallbackEvent() {

            @Override
            public void call(Object... args) {
                if (args[0] instanceof ArrayList) {
                    ArrayList<XPushUser> users = (ArrayList<XPushUser>) args[0];

                    if (resetFlag) {
                        mXpushUsers.clear();
                    }

                    if (users.size() > 0) {
                        mXpushUsers.addAll(users);
                        mHandler.sendEmptyMessage(0);
                    } else {
                        mXpushUsers.clear();
                        mHandler.sendEmptyMessage(1);
                    }
                }
            }
        });
    }

    public void addFriend(final XPushUser user){
        XPushCore.addFriend( mActivity, user, new CallbackEvent(){
            @Override
            public void call(Object... args) {
                if( args == null || args.length == 0 ){
                    mActivity.finish();
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
                case 1:
                    mTvMessage.setVisibility(View.VISIBLE);
                    mTvMessage.setText(R.string.message_no_search_user);
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
}