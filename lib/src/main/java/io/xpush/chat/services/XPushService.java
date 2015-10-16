package io.xpush.chat.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.EngineIOException;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.SocketIOException;
import com.github.nkzawa.thread.EventThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.xpush.chat.ApplicationController;
import io.xpush.chat.R;
import io.xpush.chat.models.XPushMessage;
import io.xpush.chat.models.XPushSession;
import io.xpush.chat.persist.DBHelper;
import io.xpush.chat.persist.XPushMessageDataSource;
import io.xpush.chat.persist.XpushContentProvider;
import io.xpush.chat.persist.ChannelTable;

/**
 * Created by jhkim on 2014-12-17.
 */
public class XPushService extends Service {
    private static final String TAG = "XPushService"; // Debug TAG

    private static final String XPUSH_THREAD_NAME = "XPushService[" + TAG + "]"; // Handler Thread ID

    private static final int XPUSH_KEEP_ALIVE = 1000 * 5 * 60; // KeepAlive Interval in MS. 4 min
    private static final int PING_TIMEOUT = 1000 * 1 * 10; // KeepAlive Interval in MS. 4 min

    private static final String ACTION_START = TAG + ".START"; // Action to start
    private static final String ACTION_STOP = TAG + ".STOP"; // Action to stop
    private static final String ACTION_KEEPALIVE = TAG + ".KEEPALIVE"; // Action to keep alive used by alarm manager
    private static final String ACTION_RECONNECT = TAG + ".RECONNECT"; // Action to
    private static final String ACTION_CHANGERECONNECT = TAG + ".CHANGERECONNECT";
    private static final String ACTION_RESTART = TAG + ".RESTART"; // Action to

    private static final String DEVICE_ID_FORMAT = "andr_%s"; // Device ID Format, add any prefix you'd like

    private boolean mStarted = false; // Is the Client started?
    private String mDeviceId;          // Device ID, Secure.ANDROID_ID
    private Handler mConnHandler;      // Seperate Handler thread for networking

    private IO.Options mOpts;

    private Socket mClient;                    // Socketio Client

    private AlarmManager mAlarmManager;            // Alarm manager to perform repeating tasks
    private ConnectivityManager mConnectivityManager; // To check for connectivity changes

    private Future pingTimeoutTimer;
    private ScheduledExecutorService heartbeatScheduler;

    private Boolean mPingTimeout = false;

    private boolean mConnecting = false;

    private XPushSession mXpushSession;

    private SQLiteDatabase mDatabase;
    private XPushMessageDataSource mDataSource;
    private DBHelper mDbHelper;

    public static void actionStart(Context mContext) {
        Intent i = new Intent(mContext, XPushService.class);
        i.setAction(ACTION_START);
        mContext.startService(i);
    }

    public static void actionRestart(Context mContext) {
        Intent i = new Intent(mContext, XPushService.class);
        i.setAction(ACTION_RESTART);
        mContext.startService(i);
    }

    public static void actionStop(Context mContext) {
        Intent i = new Intent(mContext, XPushService.class);
        i.setAction(ACTION_STOP);
        mContext.startService(i);
    }

    public static void actionNetworkChangeRestart(Context ctx)
    {
        Intent i = new Intent(ctx, XPushService.class);
        i.setAction(ACTION_CHANGERECONNECT);
        ctx.startService(i);
    }

    public static void actionReconnect(Context ctx)
    {
        Intent i = new Intent(ctx, XPushService.class);
        i.setAction(ACTION_RECONNECT);
        ctx.startService(i);
    }

    /**
     * Send a KeepAlive Message
     *
     * @param mContext context to start the service with
     * @return void
     */
    public static void actionKeepalive(Context mContext) {
        Intent i = new Intent(mContext, XPushService.class);
        i.setAction(ACTION_KEEPALIVE);
        mContext.startService(i);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");

        mDbHelper = new DBHelper(this);
        mDatabase = mDbHelper.getWritableDatabase();
        mDataSource = new XPushMessageDataSource(mDatabase, getString(R.string.message_table_name));

        mDeviceId = String.format(DEVICE_ID_FORMAT,
                Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

        HandlerThread thread = new HandlerThread(XPUSH_THREAD_NAME);
        thread.start();

        mConnHandler = new Handler(thread.getLooper());

        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    }

    /**
     * Service onStartCommand
     * Handles the action passed via the Intent
     *
     * @return START_REDELIVER_INTENT
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.d(TAG, "onStartCommand");

        String action = intent.getAction();

        Log.i(TAG, "Received action of " + action);

        if (action == null) {
            Log.i(TAG, "Starting service with no action\n Probably from a crash");
        } else {
            if (action.equals(ACTION_START)) {
                start();
            } else if (action.equals(ACTION_STOP)) {
                stop();
            } else if (action.equals(ACTION_KEEPALIVE)) {
                keepAlive();
            } else if (action.equals(ACTION_RECONNECT) || (action.equals(ACTION_CHANGERECONNECT))) {
                if (isNetworkAvailable()) {
                    reconnectIfNecessary();
                } else {
                    stop();
                }
            } else if ( action.equals(ACTION_RESTART) ){
                stop();
                start();
            }
        }

        // 프로세스 킬 되었더라고 실행되도록
        // 리스타트하고 마지막 인텐트 다시 전달
        return START_REDELIVER_INTENT;
    }

    /**
     * keepalive 설정되어 있으면 stop 후 connect
     * 리시버 등록(connection)
     */
    private synchronized void start() {

        if( ApplicationController.getInstance().getXpushSession() == null) {

            Log.i(TAG, "Not logged in user");
            mStarted = false;
            return;
        } else {
            mXpushSession = ApplicationController.getInstance().getXpushSession();
        }

        if (mStarted) {
            if( !isConnected() ) {
                Log.i(TAG, "not connected 1");
                connect();
            } else {
                ApplicationController.getInstance().setClient(mClient);
                Log.i(TAG, "Attempt to start while already started and connected");
                return;
            }
        }

        if (hasScheduledKeepAlives()) {
            stopKeepAlives();
        }

        if( !isConnected() ) {
            Log.i(TAG, "not connected 2");
            connect();
        }

        registerReceiver(mConnectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private synchronized void stop() {
        if (!mStarted) {
            Log.i(TAG, "Attemtpign to stop connection that isn't running");
            return;
        }

        Log.d(TAG, "stop");

        if (mClient != null) {
            mConnHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "disconnect");
                        if (mClient != null && mClient.connected()) {
                            mClient.disconnect();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    mClient = null;
                    mStarted = false;

                    stopKeepAlives();
                }
            });
        }

        try {
            unregisterReceiver(mConnectivityReceiver);
        }catch( IllegalArgumentException e ){
            e.printStackTrace();
        }
    }

    private synchronized void connect() {

        if( mXpushSession == null ){
            mStarted = false;
            return;
        }

        log("Connecting...");
        mConnecting = true;
        // fetch the device ID from the preferences.
        String hostName = "";
        String appId = getString(R.string.app_id);
        String url = mXpushSession.getServerUrl() + "/global";

        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnectionAttempts = 20;
        opts.query = "A="+appId+"&U="+mXpushSession.getId()+"&TK="+mXpushSession.getToken()+"&D="+mXpushSession.getDeviceId();

        mOpts = opts;
        Log.i(TAG, "Connecting with URL: " + url);
        try {

            Log.i(TAG, "Connecting with MemStore");
            mClient = IO.socket(url, mOpts);

            mClient.on(Socket.EVENT_CONNECT, onConnectSuccess);
            mClient.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mClient.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mClient.on("_event", onNewMessage);
            mClient.on("pong", onPong);

            mClient.on("message", onNewMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mConnHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mClient.connect();

                    ApplicationController.getInstance().setClient( mClient );

                    mStarted = true; // Service is now connected
                    Log.i(TAG, "Successfully connected and subscribed starting keep alives");

                    startKeepAlives();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 알람 매니저 통해 keepalive 스케줄 설정
     */
    private void startKeepAlives() {
        Intent i = new Intent();
        i.setClass(this, XPushService.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + XPUSH_KEEP_ALIVE,
                XPUSH_KEEP_ALIVE, pi);
    }

    private void stopKeepAlives() {
        Intent i = new Intent();
        i.setClass(this, XPushService.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        mAlarmManager.cancel(pi);
    }

    /**
     * Publishes a KeepALive to the topic
     * in the broker
     */
    private synchronized void keepAlive() {

        log("isConnected() : " + isConnected());
        if (isConnected()) {
            try {
                sendKeepAlive();
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            reconnectIfNecessary();
        }
    }

    private synchronized void reconnectIfNecessary() {
        Log.d(TAG, "reconnectIfNecessary");
        Log.d(TAG, "mStarted : " + mStarted + ", mConnecting : " + mConnecting);
        if (mStarted ) {
            if( !mClient.connected() && !mConnecting  ){
                connect();
            }
        } else {
            start();
        }
    }

    private boolean isNetworkAvailable() {
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();

        return (info == null) ? false : info.isConnected();
    }

    public boolean isConnected() {

        if (mStarted && mClient != null && !mClient.connected()) {
            Log.i(TAG, "mClient.isConnected() : " + mClient.connected() );
            Log.i(TAG, "Mismatch between what we think is connected and what is connected");
        }

        if (mClient != null) {
            return (mStarted && mClient.connected() && !mPingTimeout ) ? true : false;
        }

        if( mConnecting ){
            return false;
        }

        return false;
    }

    private final BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Connectivity Changed...");
            if (isNetworkAvailable()) {
                reconnectIfNecessary();
            } else {
                mClient.disconnect();
                stopKeepAlives();
                mClient = null;
            }

        }
    };

    private synchronized void sendKeepAlive() {

        Log.i(TAG, "Sending Keepalive to " + mXpushSession.getServerUrl());
        if (mStarted == true && mClient != null && mClient.connected()  ) {
            Log.i(TAG, "ping " + mXpushSession.getServerUrl());

            mClient.emit("ping", "ping");

            this.pingTimeoutTimer = this.getHeartbeatScheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    EventThread.exec(new Runnable() {
                        @Override
                        public void run() {
                            pingTimeoutTimer = null;
                            mPingTimeout = true;
                        }
                    });
                }
            }, PING_TIMEOUT, TimeUnit.MILLISECONDS);
        }
    }

    private synchronized boolean hasScheduledKeepAlives() {
        Intent i = new Intent();
        i.setClass(this, XPushService.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_NO_CREATE);

        return (pi != null) ? true : false;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void connectionLost(Throwable arg0) {
        Log.e(TAG, "mqtt connection Lost");
        stopKeepAlives();

        mClient = null;

        if (isNetworkAvailable()) {
            reconnectIfNecessary();
        }
    }

    // 수신 메세지 noti
    private void broadcastReceivedMessage(String channel, String message) {
        Log.d(TAG, "broadcastReceivedMessage:message = " + message);

        Intent broadcastIntent = new Intent(getApplicationContext(), PushMsgReceiver.class);
        broadcastIntent.setAction("io.xpush.chat.MGRECVD");

        broadcastIntent.putExtra("rcvd.C", channel);
        broadcastIntent.putExtra("rcvd.MG", message);

        sendBroadcast(broadcastIntent);

    }

    // log helper function
    private void log(String message) {
        log(message, null);
    }
    private void log(String message, Throwable e) {
        if (e != null) {
            Log.e(TAG, message, e);

        } else {
            Log.i(TAG, message);
        }
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject json = (JSONObject) args[0];

            String username = "";
            String message;
            String image = "";
            String channel;

            long timestamp;

            try {
                if( "NOTIFICATION".equals( json.getString("event") ) ){
                    JSONObject data = json.getJSONObject("DT");


                    XPushMessage xpushMessage = new XPushMessage( data );

                    try {
                        ContentValues values = new ContentValues();
                        values.put(ChannelTable.KEY_ID, xpushMessage.getChannel());

                        values.put(ChannelTable.KEY_UPDATED, xpushMessage.getUpdated());
                        values.put(ChannelTable.KEY_MESSAGE, xpushMessage.getMessage());
                        values.put(ChannelTable.KEY_IMAGE, xpushMessage.getImage());

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

                        Uri singleUri = Uri.parse(XpushContentProvider.CHANNEL_CONTENT_URI + "/" + xpushMessage.getChannel());
                        Cursor cursor = getContentResolver().query(singleUri, projection, null, null, null);
                        if (cursor != null && cursor.getCount() > 0) {
                            cursor.moveToFirst();

                            int count = cursor.getInt(cursor.getColumnIndexOrThrow(ChannelTable.KEY_COUNT));
                            values.put(ChannelTable.KEY_COUNT, count + 1 );
                            getContentResolver().update(singleUri, values, null, null);
                        } else {
                            values.put(ChannelTable.KEY_COUNT, 1 );
                            getContentResolver().insert(XpushContentProvider.CHANNEL_CONTENT_URI, values);
                        }

                        if (mXpushSession.getId().equals(xpushMessage.getSender() ) ){
                            xpushMessage.setType(XPushMessage.TYPE_SEND_MESSAGE);
                        } else {
                            xpushMessage.setType(XPushMessage.TYPE_RECEIVE_MESSAGE);
                        }

                        mDataSource.insert(xpushMessage);


                    } catch (Exception e ){
                        e.printStackTrace();
                    }

                    broadcastReceivedMessage( xpushMessage.getChannel(), xpushMessage.getMessage() );
                } else {

                }

            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
    };

    private Emitter.Listener onConnectSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            log("connect sucesss");
            mConnecting = false;
        }
    };

    private Emitter.Listener onPong = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            log("onPong sucesss");

            if (pingTimeoutTimer != null) {
                pingTimeoutTimer.cancel(false);
                mPingTimeout = false;
            }
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            if( args[0] instanceof SocketIOException ) {
                SocketIOException e = (SocketIOException) args[0];
                log(e.getMessage());
            } else if ( args[0] instanceof  EngineIOException){
                EngineIOException e = (EngineIOException) args[0];
                log(e.getMessage());
            }
            mConnecting = false;
        }
    };

    private ScheduledExecutorService getHeartbeatScheduler() {
        if (this.heartbeatScheduler == null || this.heartbeatScheduler.isShutdown()) {
            this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        }
        return this.heartbeatScheduler;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mDbHelper.close();
        mDatabase.close();
        mDataSource = null;
        mDbHelper = null;
        mDatabase = null;
    }
}