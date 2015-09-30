package io.xpush.chat.models;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class XPushSession {

    public static final String APP_ID = "A";
    public static final String ID = "U";
    public static final String PASSWORD = "PW";
    public static final String DEVICE_ID = "D";
    public static final String TOKEN = "TK";
    public static final String NOTI_ID = "N";
    public static final String SERVER_NAME = "SERVER_NAME";
    public static final String SERVER_URL = "SERVER_URL";
    public static final String IMAGE = "I";
    public static final String NAME = "NM";
    public static final String MESSAGE = "MG";

    private String appId;
    private String id;
    private String password;
    private String deviceId;
    private String token;
    private String notiId;
    private String serverName;
    private String image;
    private String serverUrl;
    private String name;
    private String message;

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
        this.image = bundle.getString(IMAGE);
        this.name = bundle.getString(NAME);
        this.message = bundle.getString(MESSAGE);
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

            if( object.has(IMAGE)) {
                this.image = object.getString(IMAGE);
            }

            if( object.has(NAME)) {
                this.name = object.getString(NAME);
            }

            if( object.has(MESSAGE)) {
                this.message = object.getString(MESSAGE);
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
            j.put(IMAGE, this.image);
            j.put(NAME, this.name);
            j.put(MESSAGE, this.message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return j;
    }

    public JSONObject getUserData(){
        JSONObject userData = new JSONObject();
        try {
            userData.put("NM", name);
            userData.put("I", image);
            userData.put("MG", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return userData;
    }
}
