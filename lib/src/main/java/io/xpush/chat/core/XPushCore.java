package io.xpush.chat.core;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.socket.client.SocketIOException;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;
import io.xpush.chat.ApplicationController;
import io.xpush.chat.R;
import io.xpush.chat.common.Constants;
import io.xpush.chat.models.XPushChannel;
import io.xpush.chat.models.XPushSession;
import io.xpush.chat.models.XPushUser;
import io.xpush.chat.network.LoginRequest;
import io.xpush.chat.network.StringRequest;
import io.xpush.chat.persist.ChannelTable;
import io.xpush.chat.persist.UserTable;
import io.xpush.chat.persist.XpushContentProvider;
import io.xpush.chat.services.XPushService;
import io.xpush.chat.util.ContentUtils;
import io.xpush.chat.util.XPushUtils;

public class XPushCore {

    private static XPushSession mXpushSession;

    private static final String TAG = XPushCore.class.getSimpleName();

    public static XPushCore sInstance;

    private static String mHostname;
    private static String mAppId;
    private static String mDeviceId;
    private static Context baseContext;

    private static Socket mGlobalSocket;

    private static final AtomicBoolean mGlobalSocketConnected = new AtomicBoolean();

    private static CountDownLatch latch = new CountDownLatch(2);

    public static void initialize(Context context){
        if( sInstance == null ){
            sInstance =  new XPushCore(context);
            sInstance.init();
        }
    }

    public static XPushCore getInstance(){

        if( sInstance == null ){
            sInstance =  new XPushCore();
            sInstance.init();
        }

        return sInstance;
    }

    public XPushCore(){
    }

    public XPushCore(Context context){
        baseContext = context;
    }


    private void init(){
        if( getBaseContext() != null ) {
            mXpushSession = restoreXpushSession();
            mHostname = getBaseContext().getString(R.string.host_name);
            mAppId = getBaseContext().getString(R.string.app_id);
            mDeviceId = getBaseContext().getString(R.string.device_id);
        }
    }

    public static String getHostname(){
        return mHostname;
    }

    public static String getAppId(){
        return mAppId;
    }

    public static void setBaseContext(Context context){
        baseContext = context;
        XPushCore.getInstance().init();
    }

    public static Context getBaseContext(){
        if( baseContext == null){
            Log.w(TAG, "!!!!! baseContext is null !!!!!");
            baseContext = ApplicationController.getInstance();
        }
        return baseContext;
    }

    public static void setGlobalSocket(Socket socket){
        mGlobalSocket = socket;
        latch.countDown();
    }

    public static void setGlobalSocketConnected(){
        mGlobalSocketConnected.getAndSet(true);
    }

    private Socket getClient() {

        try {
            if( !mGlobalSocketConnected.get() ) {

                // create three threads passing a CountDownLatch
                Thread T1 = new Thread(){
                    public void run() {
                        Log.d(TAG, "--- connect --- ");
                        connect();
                    }
                };

                Thread T2 = new Thread(){
                    public void run() {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        latch.countDown();
                    }
                };

                T1.start();
                T2.start();

                try {
                    latch.await(6, TimeUnit.SECONDS);
                } catch(InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        } catch (Exception e) {
            // Happens if someone interrupts your thread.
            e.printStackTrace();
        }

        if( mGlobalSocket == null ){
            Log.d(TAG, "null null null");
        }

        return mGlobalSocket;
    }

    public static boolean isGlobalConnected(){
        return mGlobalSocketConnected.get();
    }

    /**
     *
     * Auth start
     *
     *
     */
    public static void register(String id, String password, String name, final CallbackEvent callbackEvent){

        getInstance();

        final Map<String,String> params = new HashMap<String, String>();

        if( name != null ) {

            JSONObject data = new JSONObject();
            try {
                data.put("NM", name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            params.put("DT", data.toString());
        }

        params.put("A", mAppId);
        params.put("U", id);
        params.put("PW", password);
        params.put("D", mDeviceId);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if( null != pref.getString("REGISTERED_NOTIFICATION_ID", null)){
            params.put("N", pref.getString("REGISTERED_NOTIFICATION_ID", null) );
        }
        String url = mHostname+"/user/register";

        StringRequest request = new StringRequest(url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            if( "ok".equalsIgnoreCase(response.getString("status")) ){
                                callbackEvent.call();
                            } else {
                                if( response.has("message") ){
                                    callbackEvent.call(response.getString("status"), response.getString("message") );
                                } else {
                                    callbackEvent.call(response.getString("status"));
                                }
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
                        callbackEvent.call( error.getMessage() );
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(getBaseContext());
        queue.add(request);
    }

    /**
     *
     * Auth start
     *
     *
     */
    public static void login(String id, String password, final CallbackEvent callbackEvent){

        getInstance();

        final Map<String,String> params = new HashMap<String, String>();

        params.put("A", mAppId);
        params.put("U", id);
        params.put("PW", password);
        params.put("D", mDeviceId);

        String url = mHostname+"/auth";

        LoginRequest request = new LoginRequest(getBaseContext(), url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if( "ok".equalsIgnoreCase(response.getString("status")) ){
                                callbackEvent.call();
                            } else {
                                if( response.has("message") ){
                                    callbackEvent.call(response.getString("status"), response.getString("message") );
                                } else {
                                    callbackEvent.call(response.getString("status"));
                                }
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
                        callbackEvent.call( "SERVER-ERROR", error.getMessage() );
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(getBaseContext());
        queue.add(request);
    }

    /**
     *
     * logout
     *
     *
     */
    public static void logout(){
        getInstance();
        mXpushSession = null;
        XPushService.actionStop(baseContext);
        sInstance = null;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("XPUSH_SESSION");
        editor.commit();
    }

    /**
     *
     * Add device start
     *
     *
     */
    public static void addDevice(String id, String password, final CallbackEvent callbackEvent){

        getInstance();

        final Map<String,String> params = new HashMap<String, String>();

        params.put("A", mAppId);
        params.put("U", id);
        params.put("PW", password);
        params.put("D", mDeviceId);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if( null != pref.getString("REGISTERED_NOTIFICATION_ID", null)){
            params.put("N", pref.getString("REGISTERED_NOTIFICATION_ID", null) );
        }

        String url = mHostname+"/device/add";

        LoginRequest request = new LoginRequest(getBaseContext(), url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if( "ok".equalsIgnoreCase(response.getString("status")) ){
                                callbackEvent.call();
                            } else {
                                if( response.has("message") ){
                                    callbackEvent.call(response.getString("status"), response.getString("message") );
                                } else {
                                    callbackEvent.call(response.getString("status"));
                                }
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
                        callbackEvent.call("SERVER-ERROR", error.getMessage());
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(getBaseContext());
        queue.add(request);
    }

    public static void updateUser(final JSONObject mJsonUserData, final CallbackEvent callbackEvent){

        getInstance();

        final Map<String,String> params = new HashMap<String, String>();

        params.put("A", mAppId);
        params.put("U", mXpushSession.getId());
        params.put("DT", mJsonUserData.toString());
        params.put("PW", mXpushSession.getPassword());
        params.put("D", mXpushSession.getDeviceId());

        String url = getBaseContext().getString(R.string.host_name)+"/user/update";

        StringRequest request = new StringRequest(url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Update user success ======================");
                        Log.d(TAG, response.toString());
                        try {
                            if( "ok".equalsIgnoreCase(response.getString("status")) ){
                                Log.d(TAG, response.getString("status"));

                                if( mJsonUserData.has("I") ) {
                                    mXpushSession.setImage(mJsonUserData.getString("I"));
                                }
                                if( mJsonUserData.has("NM") ) {
                                    mXpushSession.setName(mJsonUserData.getString("NM"));
                                }

                                if( mJsonUserData.has("MG") ) {
                                    mXpushSession.setMessage(mJsonUserData.getString("MG"));
                                }

                                callbackEvent.call( mJsonUserData );
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Update user error ======================");
                        error.printStackTrace();
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(getBaseContext());
        queue.add(request);
    }

    /**
     *
     * Session Start
     *
     */
    public static XPushSession restoreXpushSession(){
        getInstance();
        if( mXpushSession == null ){
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            final String sessionStr = pref.getString("XPUSH_SESSION", "");
            if( !"".equals( sessionStr ) ){
                try {
                    mXpushSession = new XPushSession( new JSONObject( sessionStr ) );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return mXpushSession;
    }

    public static boolean isLogined(){
        getInstance();
        if( restoreXpushSession() != null ){
            return true;
        } else {
            return false;
        }
    }

    public static XPushSession getXpushSession(){
        getInstance();
        if( mXpushSession == null ){
            restoreXpushSession();
        }

        return mXpushSession;
    }

    /**
     *
     * Channel start
     *
     *
     */

    public static void createChannel(final XPushChannel xpushChannel, final CallbackEvent callbackEvent){

        JSONArray userArray = new JSONArray();
        for( String userId : xpushChannel.getUsers() ){
            userArray.put(userId);
        }
        final String cid = xpushChannel.getId() != null ? xpushChannel.getId() : XPushUtils.generateChannelId(xpushChannel.getUsers());
        xpushChannel.setId( cid );

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

        getInstance().getClient().emit("channel.create", jsonObject, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];

                if (response.has("status")) {
                    try {
                        Log.d(TAG, response.getString("status"));
                        if ("ok".equalsIgnoreCase(response.getString("status")) || "WARN-EXISTED".equals(response.getString("status"))
                                // duplicate
                                || ("ERR-INTERNAL".equals(response.getString("status"))) && response.get("message").toString().indexOf("E11000") > -1) {

                            getChannel(cid, callbackEvent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    public static void getChannel(final String channelId, final CallbackEvent callbackEvent){
        getChannel(getBaseContext(), channelId, callbackEvent);
    }

    public static void getChannel(Context context, final String channelId, final CallbackEvent callbackEvent){
        getInstance();
        String url = null;
        try {
            url = mHostname + "/node/"+ getInstance().getAppId()+"/"+ URLEncoder.encode(channelId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringRequest request = new StringRequest(url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            ChannelCore result = null;
                            if( "ok".equalsIgnoreCase(response.getString("status")) ){
                                JSONObject serverInfo = response.getJSONObject("result").getJSONObject("server");
                                result = new ChannelCore(channelId, serverInfo.getString("url"), serverInfo.getString("name") );
                            }

                            callbackEvent.call( result );
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackEvent.call();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        callbackEvent.call(error.getMessage());
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public static void storeChannel(XPushChannel channel){
        getInstance();
        ContentValues values = new ContentValues();
        values.put(ChannelTable.KEY_ID, channel.getId());
        values.put(ChannelTable.KEY_NAME, channel.getName());
        values.put(ChannelTable.KEY_IMAGE, channel.getImage());
        values.put(ChannelTable.KEY_MESSAGE, channel.getMessage());
        values.put(ChannelTable.KEY_UPDATED, System.currentTimeMillis());

        values.put(XpushContentProvider.SQL_INSERT_OR_REPLACE, true);
        getBaseContext().getContentResolver().insert(XpushContentProvider.CHANNEL_CONTENT_URI, values);
    }

    /**
     *
     * Group Start
     *
     */
    public static void getFriends(final CallbackEvent callbackEvent) {


        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("GR", mXpushSession.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            getInstance().getClient().emit("group.list", jsonObject, new Ack() {
                @Override
                public void call(Object... args) {
                    JSONObject response = (JSONObject) args[0];

                    Log.d(TAG, response.toString());
                    if (response.has("result")) {
                        try {
                            JSONArray result = (JSONArray) response.getJSONArray("result");
                            callbackEvent.call(result);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch ( Exception ee ){
            ee.printStackTrace();
        }
    }

    public static void storeFriends(final Context mContext, final JSONArray result) {
        getInstance();
        try {
            List<ContentValues> valuesToInsert = new ArrayList<ContentValues>();

            for (int inx = 0; inx < result.length(); inx++) {
                JSONObject json = (JSONObject) result.get(inx);

                ContentValues contentValues = new ContentValues();
                contentValues.put(UserTable.KEY_ID, json.getString("U"));

                if (json.has("DT") && !json.isNull("DT")) {
                    Object obj = json.get("DT");
                    JSONObject data = null;
                    if (obj instanceof JSONObject) {
                        data = (JSONObject) obj;
                    } else if (obj instanceof String) {
                        data = new JSONObject((String) obj);
                    }

                    if (data.has("NM")) {
                        contentValues.put(UserTable.KEY_NAME, data.getString("NM"));
                    }
                    if (data.has("MG")) {
                        contentValues.put(UserTable.KEY_MESSAGE, data.getString("MG"));
                    }
                    if (data.has("I")) {
                        contentValues.put(UserTable.KEY_IMAGE, data.getString("I"));
                    }
                } else {
                    contentValues.put(UserTable.KEY_NAME, json.getString("U"));
                }

                contentValues.put(XpushContentProvider.SQL_INSERT_OR_REPLACE, true);
                valuesToInsert.add(contentValues);
            }


            mContext.getContentResolver().bulkInsert(XpushContentProvider.USER_CONTENT_URI, valuesToInsert.toArray(new ContentValues[0]));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void addFriend(final Context mContext, final XPushUser user, final CallbackEvent callbackEvent) {

        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();

        try {
            array.put( user.getId() );

            jsonObject.put("GR", XPushCore.getXpushSession().getId()  );
            jsonObject.put("U", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getInstance().getClient().emit("group.add", jsonObject, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];
                Log.d(TAG, response.toString());
                try {
                    if ("ok".equalsIgnoreCase(response.getString("status"))) {

                        ContentValues contentValues = new ContentValues();
                        contentValues.put(UserTable.KEY_ID, user.getId());
                        contentValues.put(UserTable.KEY_NAME, user.getName());
                        contentValues.put(UserTable.KEY_MESSAGE, user.getMessage());
                        contentValues.put(UserTable.KEY_IMAGE, user.getImage());

                        contentValues.put(XpushContentProvider.SQL_INSERT_OR_REPLACE, true);
                        mContext.getContentResolver().insert(XpushContentProvider.USER_CONTENT_URI, contentValues);

                        callbackEvent.call();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void searchUser(final Context context, String searchKey, final CallbackEvent callbackEvent){
        searchUser(context, searchKey, 1, 50, callbackEvent);
    }

    public static void searchUser(final Context context, String searchKey, int pageNum, int pageSize, final CallbackEvent callbackEvent){

        getInstance();

        JSONObject options = new JSONObject();

        try {
            options.put("pageNum", pageNum);
            options.put("pageSize", pageSize);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Map<String,String> params = new HashMap<String, String>();
        params.put("A", mAppId);
        params.put("K", searchKey);
        params.put("option", options.toString());

        String url = mHostname+"/user/search";

        StringRequest request = new StringRequest(url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "====== search user response ====== " + response.toString() );
                            if( "ok".equalsIgnoreCase(response.getString("status")) ){
                                JSONArray result = (JSONArray) response.getJSONObject("result").getJSONArray("users");

                                ArrayList<XPushUser> users = new ArrayList<XPushUser>();

                                for (int inx = 0; inx < result.length(); inx++) {
                                    JSONObject json = (JSONObject) result.get(inx);
                                    Log.d(TAG, json.toString());

                                    XPushUser xpushUser = new XPushUser();

                                    xpushUser.setId(json.getString("U"));

                                    if (json.has("DT") && !json.isNull("DT")) {
                                        Object obj = json.get("DT");
                                        JSONObject data = null;
                                        if (obj instanceof JSONObject) {
                                            data = (JSONObject) obj;
                                        } else if (obj instanceof String) {
                                            data = new JSONObject((String) obj);
                                        }

                                        if (data.has("NM")) {
                                            xpushUser.setName(data.getString("NM"));
                                        } else {
                                            xpushUser.setName(json.getString("U"));
                                        }
                                        if (data.has("MG")) {
                                            xpushUser.setMessage(data.getString("MG"));
                                        }
                                        if (data.has("I")) {
                                            xpushUser.setImage(data.getString("I"));
                                        }
                                    } else {
                                        xpushUser.setName(json.getString("U"));
                                    }

                                    users.add(xpushUser);
                                }

                                callbackEvent.call(users);
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
                        callbackEvent.call(error.getMessage());
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public static String[] uploadImage(Uri uri){

        getInstance();

        String[] results = new String[3];

        String downloadUrl = null;
        String url = mXpushSession.getServerUrl()+"/upload";
        JSONObject userData = new JSONObject();

        try {
            userData.put( "U", mXpushSession.getId() );
            userData.put( "D", mDeviceId );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String realPath = ContentUtils.getRealPath(getBaseContext(), uri);
        File aFile = new File(realPath);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(realPath, options);

        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file", aFile.getName(), RequestBody.create(Constants.MEDIA_TYPE_PNG, aFile)).build();


        String appId = XPushCore.getAppId();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("XP-A", appId )
                .addHeader("XP-C", getXpushSession().getId() +"^"+ appId)
                .addHeader("XP-U", userData.toString() )
                .addHeader("XP-FU-org",  aFile.getName())
                .addHeader("XP-FU-nm", aFile.getName().substring(0, aFile.getName().lastIndexOf(".") ) )
                .addHeader("XP-FU-tp", "image")
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();

        com.squareup.okhttp.Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            JSONObject res = new JSONObject( response.body().string() );
            Log.d(TAG,  res.toString() );

            if( "ok".equals(res.getString("status")) ) {
                JSONObject result = res.getJSONObject("result");

                String channel = result.getString("channel");
                String tname = result.getString("name");

                downloadUrl = mXpushSession.getServerUrl() + "/download/" + appId + "/" + channel + "/" + mXpushSession.getId() + "/" + tname;

                Log.d(TAG, downloadUrl );
            }


            results[0] = downloadUrl;
            results[1] = String.valueOf(imageWidth);
            results[2] = String.valueOf(imageHeight);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return results;
    }

    private Socket socket;
    private synchronized void connect() {
        // fetch the device ID from the preferences.
        String appId = getBaseContext().getString(R.string.app_id);
        String url = mXpushSession.getServerUrl() + "/global";

        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnectionAttempts = 20;
        try {
            opts.query = "A="+appId+"&U="+ URLEncoder.encode(mXpushSession.getId(), "UTF-8")+"&TK="+mXpushSession.getToken()+"&D="+mXpushSession.getDeviceId();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        IO.Options mOpts = opts;
        Log.i(TAG, "Connecting with URL: " + url);
        try {
            socket = IO.socket(url, mOpts);
            socket.on(Socket.EVENT_CONNECT, onConnectSuccess);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            setGlobalSocket(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }

        socket.connect();
    }

    private Emitter.Listener onConnectSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "connect sucesss");
            setGlobalSocketConnected();
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if( args[0] instanceof SocketIOException) {
                SocketIOException e = (SocketIOException) args[0];
                Log.d(TAG, e.getMessage());
            } else if ( args[0] instanceof EngineIOException){
                EngineIOException e = (EngineIOException) args[0];
                Log.d(TAG, e.getMessage());
            }
        }
    };
}