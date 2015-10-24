package io.xpush.chat.core;

import android.util.Log;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;

import io.xpush.chat.ApplicationController;
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
        xpushSession = XPushCore.getInstance().getXpushSession();

        this.mHost = XPushCore.getInstance().getHostname();
        this.mAppId = XPushCore.getInstance().getAppId();
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

        JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject user = new JSONObject();

        try {

            user.put("U", xpushSession.getId());
            user.put("NM", xpushSession.getName());

            data.put("UO", user);
            data.put("MG", message);

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
            mChannelSocket.disconnect();

            for (String eventName : mEvents.keySet()) {
                this.off(eventName);
            }
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
                            callback.call(null);
                        }
                    } else {
                        callback.call(null);
                    }
                }
            });
        }
    }

    public void channelLeave(final CallbackEvent callback) {

        mChannelSocket.emit("channel.leave", new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];

                Log.d(TAG, response.toString());
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