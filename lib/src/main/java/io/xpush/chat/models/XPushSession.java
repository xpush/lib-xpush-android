package io.xpush.chat.models;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 정진영 on 2015-08-22.
 */
public class XPushSession {

    public static final String APP_ID = "A";
    public static final String ID = "U";
    public static final String PASSWORD = "PW";
    public static final String DEVICE_ID = "D";
    public static final String TOKEN = "TK";
    public static final String NOTI_ID = "N";
    public static final String SERVER_NAME = "SERVER_NAME";
    public static final String SERVER_URL = "SERVER_URL";

    private String appId;
    private String id;
    private String password;
    private String deviceId;
    private String token;
    private String notiId;
    private String serverName;
    private String serverUrl;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNotiId() {
        return notiId;
    }

    public void setNotiId(String notiId) {
        this.notiId = notiId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public XPushSession(){

    }

    public XPushSession(Bundle bundle){
        this.id= bundle.getString(ID);
        this.password= bundle.getString(PASSWORD);
        this.deviceId= bundle.getString(DEVICE_ID);
        this.token= bundle.getString(TOKEN);
        this.notiId= bundle.getString(NOTI_ID);
        this.serverName= bundle.getString(SERVER_NAME);
        this.serverUrl= bundle.getString(SERVER_URL);
    }

    public XPushSession(JSONObject object){
        try {
            if( object.has(ID)) {
                this.id = object.getString(ID);
            }
            if( object.has(PASSWORD)) {
                this.password = object.getString(PASSWORD);
            }
            if( object.has(DEVICE_ID)) {
                this.deviceId = object.getString(DEVICE_ID);
            }
            if( object.has(TOKEN)) {
                this.token = object.getString(TOKEN);
            }
            if( object.has(NOTI_ID)) {
                this.notiId = object.getString(NOTI_ID);
            }
            if( object.has(SERVER_NAME)) {
                this.serverName = object.getString(SERVER_NAME);
            }
            if( object.has(SERVER_URL)) {
                this.serverUrl = object.getString(SERVER_URL);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() {
        JSONObject j = new JSONObject();
        try {
            j.put(ID, this.id);
            j.put(PASSWORD, this.password);
            j.put(DEVICE_ID, this.deviceId);
            j.put(TOKEN, this.token);
            j.put(NOTI_ID, this.notiId);
            j.put(SERVER_NAME, this.serverName);
            j.put(SERVER_URL, this.serverUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return j;
    }
}
