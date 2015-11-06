package io.xpush.chat.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
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
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.EngineIOException;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.SocketIOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import io.xpush.chat.R;
import io.xpush.chat.core.CallbackEvent;
import io.xpush.chat.core.ChannelCore;
import io.xpush.chat.core.XPushCore;
import io.xpush.chat.loaders.MessageDataLoader;
import io.xpush.chat.models.XPushChannel;
import io.xpush.chat.models.XPushMessage;
import io.xpush.chat.models.XPushSession;
import io.xpush.chat.persist.ChannelTable;
import io.xpush.chat.persist.DBHelper;
import io.xpush.chat.persist.MessageTable;
import io.xpush.chat.persist.XPushMessageDataSource;
import io.xpush.chat.persist.XpushContentProvider;
import io.xpush.chat.view.adapters.MessageListAdapter;
import io.xpush.chat.view.listeners.RecyclerOnScrollListener;

public class XPushChatFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<XPushMessage>>{

    public static final String TAG = XPushChatFragment.class.getSimpleName();

    private static final int TYPING_TIMER_LENGTH = 600;
    private int mViewCount;

    private RecyclerView mRecyclerView;
    private EditText mInputMessageView;
    private RecyclerView.Adapter mAdapter;
    private boolean mTyping = false;

    private String mUserId;
    private String mUsername;

    private XPushChannel mXpushChannel;
    private String mChannel;

    private Activity mActivity;

    private XPushSession mSession;

    private SQLiteDatabase mDatabase;
    private XPushMessageDataSource mDataSource;
    private DBHelper mDbHelper;

    private List<XPushMessage> mXpushMessages = new ArrayList<XPushMessage>();

    private MessageDataLoader mDataLoader;
    private RecyclerOnScrollListener mOnScrollListener;

    private LinearLayoutManager mLayoutManager;

    private ArrayList<String> mUsers;

    private ChannelCore mChannelCore;

    public XPushChatFragment() {
        super();
    }

    private long lastReceiveTime = 0L;

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
        mDataSource = new XPushMessageDataSource(mDatabase, getActivity().getString(R.string.message_table_name), getActivity().getString(R.string.user_table_name));

        mSession = XPushCore.getInstance().getXpushSession();

        Bundle bundle = getActivity().getIntent().getBundleExtra(XPushChannel.CHANNEL_BUNDLE);
        mXpushChannel = new XPushChannel(bundle);

        mChannel = mXpushChannel.getId();
        mUsers = mXpushChannel.getUsers();

        mUserId = mSession.getId();
        mUsername = mSession.getName();

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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
        if( mChannelCore == null || !mChannelCore.connected() ) {

            if (mUsers != null) {
                createChannelAndConnect(mXpushChannel);
            } else {
                if( mChannelCore != null ) {
                    connectChannel();
                } else {
                    getChannelAndConnect();
                }
            }
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if( mChannelCore != null && mChannelCore.connected() ) {
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
                if (mChannelCore == null || !mChannelCore.connected()) return;

                if (!mTyping) {
                    mTyping = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

                // Keyboard Popup
                if (mInputMessageView.hasFocus() && oldBottom > bottom) {
                    mRecyclerView.scrollBy(0, oldBottom - bottom);
                }
            }
        });

        Button sendButton = (Button) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });
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

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle( mActivity.getString(R.string.action_leave_dialog_title))
                    .setMessage( mActivity.getString(R.string.action_leave_dialog_description))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            channelLeave();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }

                    });

            AlertDialog dialog = builder.create();    // 알림창 객체 생성
            dialog.show();    // 알림창 띄우기

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void refreshContent(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                XPushMessage message = mXpushMessages.get(0);

                mViewCount = mViewCount * 2;
                String selection = MessageTable.KEY_CHANNEL + "='" + mChannel + "' and " + MessageTable.KEY_UPDATED + " < " + message.getUpdated();
                String sortOrder = MessageTable.KEY_UPDATED + " DESC LIMIT " + mViewCount;

                mDataLoader.setSelection(selection);
                mDataLoader.setSortOrder(sortOrder);

                List<XPushMessage> messages = mDataLoader.loadInBackground();
                mDataLoader.deliverResult(messages);
            }
        }, 1);
    }

    private void connectChannel() {

        HashMap<String, Emitter.Listener> events = new HashMap<>();

        events.put(Socket.EVENT_CONNECT_ERROR, onConnectError);
        events.put(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        events.put(Socket.EVENT_CONNECT, onConnectSuccess);

        events.put("message", onNewMessage);

        mChannelCore.connect(events);
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

    private void addTyping(String userId) {
        mXpushMessages.add(new XPushMessage.Builder(XPushMessage.TYPE_ACTION)
                .username(userId).build());
        mAdapter.notifyItemInserted(mXpushMessages.size() - 1);
    }

    private void removeTyping(String userId) {
        for (int i = mXpushMessages.size() - 1; i >= 0; i--) {
            XPushMessage xpushMessage = mXpushMessages.get(i);
            if (xpushMessage.getType() == XPushMessage.TYPE_ACTION && xpushMessage.getSenderId().equals(userId)) {
                mXpushMessages.remove(i);
                mAdapter.notifyItemRemoved(i);
            }
        }
    }

    private void attemptSend() {
        if (null == mUserId) return;
        if (mChannelCore == null || !mChannelCore.connected()) return;

        mTyping = false;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        mChannelCore.sendMessage(message);
    }

    private void leave() {

        mUsername = null;
        mChannelCore.disconnect();

        Uri channelUri = Uri.parse(XpushContentProvider.CHANNEL_CONTENT_URI + "/" + mChannel );
        mActivity.getContentResolver().delete(channelUri, null, null);

        Uri messageUri = Uri.parse(XpushContentProvider.MESSAGE_CONTENT_URI + "/" + mChannel );
        mActivity.getContentResolver().delete(messageUri, null, null);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mActivity.finish();
            }
        }, 150);
    }

    private Emitter.Listener onConnectSuccess = new Emitter.Listener() {
        @Override

        public void call(Object... args) {
            ContentValues values = new ContentValues();
            values.put(ChannelTable.KEY_ID, mChannel );
            values.put(ChannelTable.KEY_COUNT, 0);

            Uri singleUri = Uri.parse(XpushContentProvider.CHANNEL_CONTENT_URI + "/" + mChannel );
            mActivity.getContentResolver().update(singleUri, values, null, null);
        }
    };


    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            Log.d(TAG, "error");

            if( args[0] instanceof SocketIOException) {
                SocketIOException e = (SocketIOException) args[0];
                Log.d(TAG, e.getMessage());
            } else if ( args[0] instanceof EngineIOException){
                EngineIOException e = (EngineIOException) args[0];
                Log.d(TAG, e.getMessage());
            }

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(mActivity, R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            saveMessage(data);
        }
    };


    private void disconnect(){
        if( mChannelCore != null && mChannelCore.connected() ) {
            mChannelCore.disconnect();
        }
    }

    @Override
    public Loader<List<XPushMessage>> onCreateLoader(int id, Bundle args) {

        String selection = MessageTable.KEY_CHANNEL + "='" + mChannel +"'";
        String sortOrder = MessageTable.KEY_UPDATED + " DESC LIMIT " + String.valueOf(mViewCount);

        mDataLoader = new MessageDataLoader(mActivity, mDataSource, selection, null, null, null, sortOrder);
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
            mOnScrollListener = new RecyclerOnScrollListener(mLayoutManager, RecyclerOnScrollListener.RecylclerDirection.UP) {
                @Override
                public void onLoadMore(int current_page) {
                    Log.d(TAG, " onLoadMore : "+  current_page);
                    refreshContent();
                }
            };

            scrollToBottom();
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

    private void saveMessage( JSONObject data ){

        Log.d(TAG, "onMessage : " + data.toString());

        final XPushMessage xpushMessage = new XPushMessage( data );

        try {
            if (mSession.getId().equals(xpushMessage.getSenderId() ) ){
                xpushMessage.setType(XPushMessage.TYPE_SEND_MESSAGE);
            } else {
                xpushMessage.setType(XPushMessage.TYPE_RECEIVE_MESSAGE);
            }

            ContentValues values = new ContentValues();
            values.put(ChannelTable.KEY_ID, xpushMessage.getChannel());
            values.put(ChannelTable.KEY_MESSAGE, xpushMessage.getMessage());
            if ( xpushMessage.getType() != XPushMessage.TYPE_SEND_MESSAGE ){
                values.put(ChannelTable.KEY_NAME, xpushMessage.getSenderName());
            } else {
                values.put(ChannelTable.KEY_NAME, mXpushChannel.getName());
            }
            values.put(ChannelTable.KEY_IMAGE, xpushMessage.getImage());
            values.put(ChannelTable.KEY_UPDATED, xpushMessage.getUpdated());
            values.put(ChannelTable.KEY_COUNT, 0);

            values.put(XpushContentProvider.SQL_INSERT_OR_REPLACE, true);
            mActivity.getContentResolver().insert(XpushContentProvider.CHANNEL_CONTENT_URI, values);

            // INSERT INTO MESSAGE TABLE
            lastReceiveTime = xpushMessage.getUpdated();
            mDataSource.insert(xpushMessage);

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addMessage(xpushMessage);
                }
            });

        } catch (Exception e ){
            e.printStackTrace();
        }
    }

    private void createChannelAndConnect(XPushChannel xpushChannel){
        XPushCore.getInstance().createChannel(xpushChannel, new CallbackEvent() {
            @Override
            public void call(Object... args) {
                if( args[0] != null ) {
                    mChannelCore = (ChannelCore) args[0];
                    connectChannel();
                }
            }
        });
    }

    private void getChannelAndConnect(){

        XPushCore.getInstance().getChannel(mActivity, mChannel, new CallbackEvent() {
            @Override
            public void call(Object... args) {
                if (args[0] != null) {
                    mChannelCore = (ChannelCore) args[0];
                    connectChannel();
                }
            }
        });
    }

    //add unread message
    private void messageUnread(){
        mChannelCore.getMessageUnread( lastReceiveTime, new CallbackEvent(){
            @Override
            public void call(Object... args) {
                if( args != null && args[0] != null) {
                    JSONArray messages = (JSONArray) args[0];
                    try {
                        for (int inx = 0; inx < messages.length(); inx++) {
                            saveMessage(messages.getJSONObject(inx));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void channelLeave(){

        // 1:1 Channel, only delete local data
        if( mUsers.size() == 2 ){
            leave();
        } else {
            mChannelCore.channelLeave(new CallbackEvent() {
                @Override
                public void call(Object... args) {
                    leave();
                }
            });
        }
    }
}