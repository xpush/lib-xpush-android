package io.xpush.chat;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import io.xpush.chat.core.BaseContextListener;
import io.xpush.chat.core.XPushCore;
import io.xpush.chat.models.XPushSession;
import io.xpush.chat.persist.XpushContentProvider;

public class ApplicationController extends Application implements BaseContextListener{

    public static final String TAG = ApplicationController.class.getSimpleName();
    private static ApplicationController mInstance;

    public static synchronized ApplicationController getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    @Override
    public synchronized Context getBaseContext(){
        return mInstance;
    }
}