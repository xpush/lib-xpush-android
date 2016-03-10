package io.xpush.chat.core;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.xpush.chat.models.XPushSession;

public class ChannelCore {

    private static final String TAG = ChannelCore.class.getSimpleName();

    private String mChannelId;
    private String mServerUrl;
    private String mServerName;
    private XPushSession xpushSession;

    private String mHost;
    private String mAppId;
    private String mDeviceId;

    private Socket mChannelSocket;

    private HashMap<String, Emitter.Listener> mEvents;

    public ChannelCore() {
        init();
    }

    public ChannelCore(String channelId, String serveUrl, String serverName) {
        this();
        this.mChannelId = channelId;
        this.mServerUrl = serveUrl;
        this.mServerName = serverName;
    }

    public void init() {
        xpushSession = XPushCore.getXpushSession();

        this.mHost = XPushCore.getHostname();
        this.mAppId = XPushCore.getAppId();
        this.mDeviceId = xpushSession.getDeviceId();
    }

    public void connect(HashMap<String, Emitter.Listener> events) {

        mEvents = events;
        String url = mServerUrl + "/channel";

        IO.Options opts = new IO.Options();
        opts.forceNew = true;

        opts.query = "A=" + mAppId + "&C=" + mChannelId + "&S=" + mServerName + "&D=" + mDeviceId + "&U=" + xpushSession.getId();

        mChannelSocket = null;

        try {
            mChannelSocket = IO.socket(url, opts);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (events != null) {
            for (String eventName : events.keySet()) {
                this.on(eventName, events.get(eventName));
            }
        }

        mChannelSocket.connect();
    }

    public void sendMessage(String message) {
        sendMessage(message, null, null, null);
    }

    public void sendMessage(String message, String type) {
        sendMessage(message, type, null, null);
    }

    public void sendImage(String message, int width, int height){
        JSONObject metaData = new JSONObject();

        try {

            metaData.put("W", width);
            metaData.put("H", height);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(message, "IM", metaData, null );
    }

    public void sendMessage(String message, String type, ArrayList<String> users) {
        sendMessage(message, type, null, users);
    }

    public void sendMessage(String message, String type, JSONObject metaData, ArrayList<String> users) {

        JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject user = new JSONObject();

        try {

            user.put("I", xpushSession.getImage());
            user.put("U", xpushSession.getId());
            user.put("NM", xpushSession.getName());

            data.put("UO", user);
            data.put("MG", message);
            if( type != null ) {
                data.put("TP", type);
            }

            if( users != null ) {
                data.put("US", TextUtils.join("@!@", users));
            }

            if( metaData != null ){
                data.put("MD", metaData);
            }

            json.put("DT", data);
            json.put("NM", "message");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (mChannelSocket != null && mChannelSocket.connected()) {
            mChannelSocket.emit("send", json);
        }
    }

    public void emit(final String event, final Object... args) {
        mChannelSocket.emit(event, args);
    }

    public void on(String event, Emitter.Listener fn) {
        mChannelSocket.on(event, fn);
    }

    public void off(String event) {
        mChannelSocket.off(event);
    }

    public void off(String event, Emitter.Listener fn) {
        mChannelSocket.off(event, fn);
    }

    public boolean connected() {
        if (mChannelSocket == null) {
            return false;
        }

        return mChannelSocket.connected();
    }

    public void disconnect() {
        if (mChannelSocket != null) {
            for (String eventName : mEvents.keySet()) {
                this.off(eventName);
            }
            Log.d(TAG, "=== disconnect ===");
            mChannelSocket.disconnect();
        }
    }

    public void getMessageUnread(long lastReceiveTime, final CallbackEvent callback) {
        if (mChannelSocket != null) {

            JSONObject jsonObject = new JSONObject();
            try {

                if (lastReceiveTime > 0) {
                    jsonObject.put("TS", lastReceiveTime);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            mChannelSocket.emit("message.unread", jsonObject, new Ack() {
                @Override
                public void call(Object... args) {
                    JSONObject response = (JSONObject) args[0];
                    if (response.has("status")) {
                        try {
                            if ("ok".equalsIgnoreCase(response.getString("status"))) {
                                JSONArray messages = (JSONArray) response.get("result");
                                callback.call(messages);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.call(new JSONArray());
                        }
                    } else {
                        callback.call(new JSONArray());
                    }
                }
            });
        }
    }


    public void channelJoin(ArrayList<String> userIdArrayList, final CallbackEvent callback) {

        JSONArray userArray = new JSONArray();
        for( String userId : userIdArrayList ){
            userArray.put(userId);
        }

        JSONObject data = new JSONObject();
        try {
            data.put( "U", userArray );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mChannelSocket.emit("channel.join", data, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];

                Log.d(TAG, response.toString() );
                if (response.has("status")) {
                    try {
                        if ("ok".equalsIgnoreCase(response.getString("status"))) {
                            callback.call();
                        } else {
                            callback.call();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void channelGet(final CallbackEvent callback) {

        mChannelSocket.emit("channel.get", new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];

                Log.d(TAG, response.toString() );
                if (response.has("status")) {
                    try {
                        if ("ok".equalsIgnoreCase(response.getString("status"))) {
                            callback.call(response);
                        } else {
                            callback.call();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void channelLeave(final CallbackEvent callback) {

        mChannelSocket.emit("channel.leave", new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];

                if (response.has("status")) {
                    try {
                        if ("ok".equalsIgnoreCase(response.getString("status"))) {
                            callback.call();
                        } else {
                            if (response.has("message") && "ERR-NOTEXIST".equals(response.getString("message") ) ) {
                                callback.call();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}