package io.xpush.chat.network;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


public class StringRequest extends Request<JSONObject> {

    public static final String TAG = StringRequest.class.getSimpleName();

    private final Response.Listener<JSONObject> mListener;

    private Map<String, String> params;

    private String mUrl;

    public StringRequest(String url, Map<String,String> params, Response.Listener<JSONObject> listener,
                         Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.mListener = listener;
        this.params = params;

    }

    @Override
    protected void deliverResponse(JSONObject response) {
        mListener.onResponse(response);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        JSONObject parsed;
        try {
            parsed = new JSONObject( new String(response.data) );
        } catch (JSONException e) {
            parsed = null;
        }

        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected Map<String,String> getParams(){
        return this.params;
    }
}