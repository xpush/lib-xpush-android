package io.xpush.chat;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import io.xpush.chat.models.XPushSession;
import io.xpush.chat.persist.XpushContentProvider;

public class ApplicationController extends Application {

    public static final String TAG = ApplicationController.class.getSimpleName();

    private static ApplicationController mInstance;

    private RequestQueue mRequestQueue;
    private XPushSession mXpushSession;

    private Socket mClient;

    private String mAppId;

    public static synchronized ApplicationController getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        mAppId = getString(R.string.app_id);
        getXpushSession();
    }

    public XPushSession getXpushSession(){
        if( mXpushSession == null ){
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            final String loginUserStr = pref.getString("XPUSH_SESSION", "");
            if( !"".equals( loginUserStr ) ){
                try {
                    mXpushSession = new XPushSession( new JSONObject( loginUserStr ) );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return mXpushSession;
    }

    public void setXpushSession(XPushSession session){
        mXpushSession = session;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ApplicationController.getInstance());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("XPUSH_SESSION", mXpushSession.toJSON().toString());
        editor.commit();
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public void setClient( Socket socket ){
        this.mClient = socket;
    }

    public Socket getClient(){
        return mClient;
    }

    public String getAppId(){
        return this.mAppId;
    }
}