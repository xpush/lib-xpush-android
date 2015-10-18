package io.xpush.chat.core;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.Future;

import io.xpush.chat.ApplicationController;
import io.xpush.chat.R;
import io.xpush.chat.models.XPushSession;
import io.xpush.chat.network.StringRequest;
import io.xpush.chat.util.XPushUtils;

/**
 * Created by 정진영 on 2015-08-05.
 */
public class XPush {

    private static final String TAG = XPush.class.getSimpleName();

    public static XPush instance;
    private XPushSession xpushSession;

    private String host;
    private String appId;

    private Socket mGlobalSocket;
    private Socket mChannelSocket;

    public static XPush getInstance(){
        if( instance == null ) {
            instance = new XPush();
            instance.init();
        }

        return instance;
    }

    public XPush(){
    }

    public void init(){
        this.host = xpushSession.getServerUrl();
        this.appId = xpushSession.getAppId();

        xpushSession = ApplicationController.getInstance().getXpushSession();
        mGlobalSocket = ApplicationController.getInstance().getClient();
    }

    public void createChannel(ArrayList<String> users, final CallbackEvent callbackEvent){

        ArrayList<String> userArray = new ArrayList<String>();
        userArray.addAll(users);
        userArray.add(ApplicationController.getInstance().getXpushSession().getId());

        final String channelId = XPushUtils.generateChannelId(userArray);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("C", XPushUtils.generateChannelId(userArray));
            jsonObject.put("U", userArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        mGlobalSocket.emit("channel.create", jsonObject, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];

                Log.d(TAG, response.toString());
                if (response.has("status")) {
                    try {
                        Log.d(TAG, response.getString("status"));
                        if ("ok".equalsIgnoreCase(response.getString("status")) || "WARN-EXISTED".equals(response.getString("status"))) {
                            getServerUrl(channelId, callbackEvent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void getServerUrl(String channelId, final CallbackEvent callbackEvent){
        String url = null;
        try {
            url = host + "/node/"+ ApplicationController.getInstance().getAppId()+"/"+ URLEncoder.encode(channelId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Log.d(TAG, url);
        StringRequest request = new StringRequest(url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject res) {

                        try {
                            if ("ok".equalsIgnoreCase(res.getString("status"))) {
                                //mServerName = res.getJSONObject("result").getJSONObject("server").getString("name");
                                //mServerUrl = res.getJSONObject("result").getJSONObject("server").getString("url");
                                //connect();
                                callbackEvent.callbackMethod();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(ApplicationController.getInstance());
        queue.add(request);
    }
}