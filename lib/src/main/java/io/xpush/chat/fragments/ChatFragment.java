package io.xpush.chat.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.xpush.chat.ApplicationController;
import io.xpush.chat.R;
import io.xpush.chat.loaders.MessageDataLoader;
import io.xpush.chat.models.XPushChannel;
import io.xpush.chat.models.XPushMessage;
import io.xpush.chat.models.XPushSession;
import io.xpush.chat.persist.ChannelTable;
import io.xpush.chat.persist.DBHelper;
import io.xpush.chat.persist.MessageTable;
import io.xpush.chat.persist.XPushMessageDataSource;
import io.xpush.chat.persist.XpushContentProvider;
import io.xpush.chat.view.listeners.RecyclerOnScrollListener;
import io.xpush.chat.views.adapters.MessageListAdapter;


/**
 * A chat fragment containing messages view and input form.
 */
public class ChatFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<XPushMessage>>{

    public static final String TAG = ChatFragment.class.getSimpleName();

    private static final int TYPING_TIMER_LENGTH = 600;
    private int mViewCount;

    private RecyclerView mRecyclerView;
    private EditText mInputMessageView;
    private RecyclerView.Adapter mAdapter;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private String mUsername;
    private Socket mSocket;

    private XPushChannel mXpushChannel;
    private String mChannel;

    private Activity mActivity;

    private XPushSession mSession;

    private SQLiteDatabase mDatabase;
    private XPushMessageDataSource mDataSource;
    private DBHelper mDbHelper;

    private List<XPushMessage> mXpushMessages = new ArrayList<XPushMessage>();

    private MessageDataLoader mDataLoader;

    private int mTotalMessageCount;

    private RecyclerOnScrollListener mOnScrollListener;

    private LinearLayoutManager mLayoutManager;

    public ChatFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAdapter = new MessageListAdapter(activity, mXpushMessages);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        mViewCount = 16;

        mActivity = getActivity();

        mDbHelper = new DBHelper(mActivity);
        mDatabase = mDbHelper.getWritableDatabase();
        mDataSource = new XPushMessageDataSource(mDatabase, getActivity().getString(R.string.message_table_name));

        mSession = ApplicationController.getInstance().getXpushSession();

        Bundle bundle = getActivity().getIntent().getBundleExtra(XPushChannel.CHANNEL_BUNDLE);
        mXpushChannel = new XPushChannel(bundle);

        mChannel = mXpushChannel.getId();

        mUsername = ApplicationController.getInstance().getXpushSession().getId();

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }


    @Override
    public void onResume(){
        super.onResume();
        if( mSocket == null || !mSocket.connected() ) {
            connect();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if( mSocket != null && mSocket.connected() ) {
            disconnect();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.messages);


        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mInputMessageView = (EditText) view.findViewById(R.id.message_input);
        mInputMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.send || id == EditorInfo.IME_NULL) {
                    attemptSend();
                    return true;
                }
                return false;
            }
        });
        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == mUsername) return;
                if (!mSocket.connected()) return;

                if (!mTyping) {
                    mTyping = true;
                    mSocket.emit("typing");
                }

                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK != resultCode) {
            getActivity().finish();
            return;
        }

        mUsername = data.getStringExtra("U");
        int numUsers = data.getIntExtra("numUsers", 1);

        addLog(getResources().getString(R.string.message_welcome));
        addParticipantsLog(numUsers);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_leave) {
            leave();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void refreshContent(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                XPushMessage message =  mXpushMessages.get(0);

                mViewCount = mViewCount * 2;
                String selection = MessageTable.KEY_UPDATED + " < " + message.getUpdated();
                String sortOrder = MessageTable.KEY_UPDATED + " DESC LIMIT " + mViewCount;

                mDataLoader.setSelection(selection);
                mDataLoader.setSortOrder(sortOrder);

                List<XPushMessage> messages = mDataLoader.loadInBackground();
                mDataLoader.deliverResult(messages);
            }
        }, 1);
    }


    private void connect(){
        try {
            ;
            String url = "http://stalk-front-l01.cloudapp.net:8880/channel";
            mChannel = mXpushChannel.getId();

            IO.Options opts = new IO.Options();
            opts.forceNew = true;

            String appId = getString(R.string.app_id);

            opts.query = "A=" + appId+"&C="+ mChannel+"&S="+mSession.getServerName()+"&D=web&U="+mSession.getId();

            mSocket = IO.socket( url, opts );

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT, onConnectSuccess);


        mSocket.on("message", onNewMessage);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeft);
        mSocket.on("typing", onTyping);
        mSocket.on("stop typing", onStopTyping);

        mSocket.connect();
    }

    private void addMessage(XPushMessage xpushMessage) {
        mXpushMessages.add(xpushMessage);
        mAdapter.notifyItemInserted(mXpushMessages.size() - 1);
        scrollToBottom();
    }

    private void addLog(String message) {
        mXpushMessages.add(new XPushMessage.Builder(XPushMessage.TYPE_LOG)
                .message(message).build());
        mAdapter.notifyDataSetChanged();
    }

    private void addParticipantsLog(int numUsers) {
        addLog(getResources().getQuantityString(R.plurals.message_participants, numUsers, numUsers));
    }

    private void addTyping(String username) {
        mXpushMessages.add(new XPushMessage.Builder(XPushMessage.TYPE_ACTION)
                .username(username).build());
        mAdapter.notifyItemInserted(mXpushMessages.size() - 1);
        scrollToBottom();
    }

    private void removeTyping(String username) {
        for (int i = mXpushMessages.size() - 1; i >= 0; i--) {
            XPushMessage xpushMessage = mXpushMessages.get(i);
            if (xpushMessage.getType() == XPushMessage.TYPE_ACTION && xpushMessage.getSender().equals(username)) {
                mXpushMessages.remove(i);
                mAdapter.notifyItemRemoved(i);
            }
        }
    }

    private void attemptSend() {
        if (null == mUsername) return;
        if (!mSocket.connected()) return;

        mTyping = false;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        // perform the sending message attempt.

        JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject user = new JSONObject();

        try {

            user.put( "U", mUsername );
            data.put( "UO", user  );
            data.put( "MG", message );

            json.put("DT", data );
            json.put("NM", "message" );

        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSocket.emit("send", json);
    }

    private void leave() {
        mUsername = null;
        mSocket.disconnect();
    }

    private Emitter.Listener onConnectSuccess = new Emitter.Listener() {
        @Override

        public void call(Object... args) {

            ContentValues values = new ContentValues();
            values.put(ChannelTable.KEY_ID, mChannel );
            values.put(ChannelTable.KEY_COUNT, 0);

            Uri singleUri = Uri.parse(XpushContentProvider.CHANNEL_CONTENT_URI + "/" + mChannel );
            getActivity().getContentResolver().update(singleUri, values, null, null);
        }
    };


    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];

            final XPushMessage xpushMessage = new XPushMessage( data );

            try {
                ContentValues values = new ContentValues();
                values.put(ChannelTable.KEY_ID, xpushMessage.getChannel());
                values.put(ChannelTable.KEY_UPDATED, xpushMessage.getUpdated());
                values.put(ChannelTable.KEY_MESSAGE, xpushMessage.getMessage());
                values.put(ChannelTable.KEY_IMAGE, xpushMessage.getImage());
                values.put(ChannelTable.KEY_COUNT, 0);

                Uri singleUri = Uri.parse(XpushContentProvider.CHANNEL_CONTENT_URI + "/" + xpushMessage.getChannel());
                getActivity().getContentResolver().update(singleUri, values, null, null);

                /**
                 values.put(ChannelTable.KEY_ID, xpushMessage.getChannel() + "_" + xpushMessage.getUpdated());
                 values.put(MessageTable.KEY_CHANNEL, xpushMessage.getChannel());
                 values.put(MessageTable.KEY_SENDER, xpushMessage.getSender());


                 if(  mSession.getId().equals( xpushMessage.getSender() ) ){
                 values.put(MessageTable.KEY_TYPE, XPushMessage.TYPE_SEND_MESSAGE);
                 } else {
                 values.put(MessageTable.KEY_TYPE, XPushMessage.TYPE_RECEIVE_MESSAGE);
                 }

                 getActivity().getContentResolver().insert(XpushContentProvider.MESSAGE_CONTENT_URI, values);
                 */

                if(  mSession.getId().equals( xpushMessage.getSender() ) ){
                    xpushMessage.setType(XPushMessage.TYPE_SEND_MESSAGE);
                } else {
                    xpushMessage.setType(XPushMessage.TYPE_RECEIVE_MESSAGE);
                }

                mDataSource.insert(xpushMessage);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addMessage(xpushMessage);
                    }
                });

            } catch (Exception e ){
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("U");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_joined, username));
                    addParticipantsLog(numUsers);
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("U");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_left, username));
                    addParticipantsLog(numUsers);
                    removeTyping(username);
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("U");
                    } catch (JSONException e) {
                        return;
                    }
                    addTyping(username);
                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("U");
                    } catch (JSONException e) {
                        return;
                    }
                    removeTyping(username);
                }
            });
        }
    };

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;

            mTyping = false;
            mSocket.emit("stop typing");
        }
    };

    private void disconnect(){
        if( mSocket != null && mSocket.connected() ) {
            mSocket.disconnect();
            mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mSocket.off("message", onNewMessage);
            mSocket.off("user joined", onUserJoined);
            mSocket.off("user left", onUserLeft);
            mSocket.off("typing", onTyping);
            mSocket.off("stop typing", onStopTyping);
        }
    }

    @Override
    public Loader<List<XPushMessage>> onCreateLoader(int id, Bundle args) {

        String selection = MessageTable.KEY_CHANNEL + "='" + mChannel +"'";
        String sortOrder = MessageTable.KEY_UPDATED + " DESC LIMIT " + String.valueOf(mViewCount);

        mTotalMessageCount = mDataSource.count(selection, null);

        mDataLoader = new MessageDataLoader(getActivity(), mDataSource, selection, null, null, null, sortOrder);
        return mDataLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<XPushMessage>> loader, List<XPushMessage> data) {

        boolean onStarting = false;
        if( mXpushMessages.size() == 0 ){
            onStarting = true;
        }

        Collections.sort(data, new TimestampAscCompare());

        mXpushMessages.addAll(0, data);
        mAdapter.notifyDataSetChanged();

        if( onStarting ) {
            scrollToBottom();

            mOnScrollListener = new RecyclerOnScrollListener(mLayoutManager) {
                @Override
                public void onLoadMore(int current_page) {
                    Log.d(TAG, " onLoadMore : "+  current_page);
                    refreshContent();
                }
            };

            mRecyclerView.addOnScrollListener(mOnScrollListener);


        } else {
            mRecyclerView.scrollToPosition( mXpushMessages.size() - data.size() + 1);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<XPushMessage>> loader) {
        mXpushMessages.clear();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
        mDatabase.close();
        mDataSource = null;
        mDbHelper = null;
        mDatabase = null;
    }

    private void scrollToBottom() {
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
    }


    static class TimestampAscCompare implements Comparator<XPushMessage> {
        public int compare(XPushMessage arg0, XPushMessage arg1) {
            return arg0.getUpdated() < arg1.getUpdated() ? -1 : arg0.getUpdated() > arg1.getUpdated() ? 1:0;
        }
    }
}