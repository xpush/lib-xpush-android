package io.xpush.chat.core;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.Socket;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.xpush.chat.ApplicationController;
import io.xpush.chat.models.XPushSession;
import io.xpush.chat.network.LoginRequest;
import io.xpush.chat.util.XPushUtils;

public class XPushCore {

    private static final String TAG = XPushCore.class.getSimpleName();

    public static XPushCore instance;
    private XPushSession xpushSession;

    private String mHostname;
    private String mAppId;
    private String mDeviceId;

    private Socket mGlobalSocket;

    public static XPushCore getInstance(){
        if( instance == null ) {
            instance = new XPushCore();
            instance.init();
        }

        return instance;
    }

    public XPushCore(){
    }

    public void init(){
        xpushSession = ApplicationController.getInstance().getXpushSession();

        this.mHostname = ApplicationController.getInstance().getHostname();
        this.mAppId = ApplicationController.getInstance().getAppId();
        this.mDeviceId = xpushSession.getDeviceId();
        mGlobalSocket = ApplicationController.getInstance().getClient();
    }

    public void setGlobalSocket(Socket socket){
        this.mGlobalSocket = socket;
    }

    public boolean isGlobalConnected(){
        if( mGlobalSocket == null ){
            return false;
        }

        return mGlobalSocket.connected();
    }

    public void createChannel(ArrayList<String> users, final CallbackEvent callbackEvent){
        createChannel(null, users, callbackEvent);
    }

    public void createChannel(String channelId, ArrayList<String> users, final CallbackEvent callbackEvent){

        JSONArray userArray = new JSONArray();
        for( String userId : users ){
            userArray.put(userId);
        }
        final String cid = channelId != null ? channelId : XPushUtils.generateChannelId(users);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("C", cid);
            jsonObject.put("U", userArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if ( mGlobalSocket == null || !mGlobalSocket.connected() ){
            Log.d(TAG, "Not connected");
            return;
        }

        mGlobalSocket.emit("channel.create", jsonObject, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];

                Log.d(TAG, "==== response =====");
                Log.d(TAG, response.toString());
                if (response.has("status")) {
                    try {
                        Log.d(TAG, response.getString("status"));
                        if ("ok".equalsIgnoreCase(response.getString("status")) || "WARN-EXISTED".equals(response.getString("status"))
                                // duplicate
                                || ("ERR-INTERNAL".equals(response.getString("status"))) && response.get("message").toString().indexOf("E11000") > -1) {

                            ChannelCore channelCore = createChannel(cid);
                            callbackEvent.call(channelCore);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public ChannelCore createChannel(String channelId){

        ChannelCore result = null;
        String url = null;

        try {
            url = mHostname + "/node/"+ ApplicationController.getInstance().getAppId()+"/"+ URLEncoder.encode(channelId, "UTF-8");
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            com.squareup.okhttp.Response response = null;

            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            JSONObject res = new JSONObject( response.body().string() );
            if( "ok".equals(res.getString("status")) ) {
                JSONObject serverInfo = res.getJSONObject("result").getJSONObject("server");
                result = new ChannelCore(channelId, serverInfo.getString("url"), serverInfo.getString("name") );
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if( result == null ){
                result = new ChannelCore();
            }
        }

        return result;
    }


}