package io.xpush.chat.network;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import io.xpush.chat.ApplicationController;
import io.xpush.chat.models.XPushSession;
import io.xpush.chat.services.XPushService;


public class LoginRequest extends StringRequest {

    public LoginRequest(String url, Map<String, String> params, Response.Listener<JSONObject> listener,
                        Response.ErrorListener errorListener) {
        super(url, params, listener, errorListener);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ApplicationController.getInstance());
        String notiId = pref.getString("REGISTERED_NOTIFICATION_ID", null);
        if( notiId != null ) {
            params.put("N", notiId);
        }
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        JSONObject parsed;
        try {
            parsed = new JSONObject( new String(response.data) );

            Log.d(TAG, parsed.toString());

            if ("ok".equalsIgnoreCase(parsed.getString("status"))) {
                JSONObject result = parsed.getJSONObject("result");

                String token = result.getString("token");
                String server = result.getString("server");
                String serverUrl = result.getString("serverUrl");

                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ApplicationController.getInstance());
                SharedPreferences.Editor editor = pref.edit();

                XPushSession xpushSession = new XPushSession();

                xpushSession.setAppId(getParams().get("A"));

                xpushSession.setId(getParams().get("U"));
                xpushSession.setPassword(getParams().get("PW"));
                xpushSession.setDeviceId(getParams().get("D"));
                if( result.getJSONObject("user").has("DT") ){
                    JSONObject data = null;
                    try {
                        data = result.getJSONObject("user").getJSONObject("DT");
                    } catch( JSONException je ){
                        je.printStackTrace();
                        data =  new JSONObject( result.getJSONObject("user").getString("DT") );
                    }
                    if( data.has("I") ){
                        xpushSession.setImage(data.getString("I"));
                    }

                    if( data.has("NM") ) {
                        xpushSession.setName(data.getString("NM"));
                    }
                }

                xpushSession.setToken(token);
                xpushSession.setServerName(server);
                xpushSession.setServerUrl(serverUrl);

                editor.putString("XPUSH_SESSION", xpushSession.toJSON().toString());
                editor.commit();

                XPushService.actionStart(ApplicationController.getInstance());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            parsed = null;
        }

        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }
}