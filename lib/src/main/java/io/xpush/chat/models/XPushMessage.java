package io.xpush.chat.models;

import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;

import io.xpush.chat.persist.ChannelTable;
import io.xpush.chat.persist.MessageTable;

public class XPushMessage {

    public static final int TYPE_SEND_MESSAGE = 0;
    public static final int TYPE_RECEIVE_MESSAGE = 1;

    public static final int TYPE_INVITE = 2;
    public static final int TYPE_ACTION = 3;

    private String rowId;
    private String id;
    private String channel;
    private String senderId;
    private String senderName;
    private String image;
    private String count;
    private String message;
    private int type;
    private long updated;
    private ArrayList<String> users;

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }


    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public ArrayList<String> getUsers(){
        return this.users;
    }

    public void setUsers(ArrayList<String> users){
        this.users = users;
    }

    public XPushMessage(){
    }

    public XPushMessage(JSONObject data) {

        JSONObject uo = null;
        try {
            if( data.has("UO") ) {
                uo = data.getJSONObject("UO");

                if( uo.has("U") ){
                    this.senderId = uo.getString("U");
                }

                if( uo.has("NM") ){
                    this.senderName = uo.getString("NM");
                }

                if( uo.has("I") ) {
                    this.image = uo.getString("I");
                }
            }

            if( data.has("TP") ){
                if( "IN".equals(data.getString("TP")) ) {
                    this.type = 2;
                }
            }

            this.channel = data.getString("C");

            if( data.has("US") ){
                String usersStr = data.getString("US");
                this.users = new ArrayList<String>(Arrays.asList(usersStr.split("#!#")));
            }  else if( this.channel != null && this.channel.indexOf("#!#") > -1 && this.channel.lastIndexOf("^") > 0 ){
                String usersStr = this.channel.substring( 0, this.channel.lastIndexOf("^") );
                this.users = new ArrayList<String>(Arrays.asList(usersStr.split("#!#")));
            }

            this.message = URLDecoder.decode( data.getString("MG"), "UTF-8");
            this.updated = data.getLong("TS");

            this.id = channel +"_" + updated;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public XPushMessage(Cursor cursor){
        this.rowId= cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.KEY_MESSAGE));
        this.id= cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.KEY_ID));
        this.senderName= cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.KEY_SENDER));
        this.image= cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.KEY_IMAGE));
        this.message= cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.KEY_MESSAGE));
        this.type= cursor.getInt(cursor.getColumnIndexOrThrow(MessageTable.KEY_TYPE));
        this.updated= cursor.getLong(cursor.getColumnIndexOrThrow(MessageTable.KEY_UPDATED));
    }

    public static class Builder {
        private final int mType;
        private String mUserId;
        private String mUsername;
        private String mMessage;
        private long mTimestamp;

        public Builder(int type) {
            mType = type;
        }

        public Builder userId(String mUserId) {
            mUserId = mUserId;
            return this;
        }

        public Builder username(String username) {
            mUsername = username;
            return this;
        }

        public Builder message(String message) {
            mMessage = message;
            return this;
        }

        public Builder timestamp(long timestamp) {
            mTimestamp = timestamp;
            return this;
        }

        public XPushMessage build() {
            XPushMessage xpushMessage = new XPushMessage();
            xpushMessage.type = mType;
            xpushMessage.senderName= mUsername;
            xpushMessage.message = mMessage;
            xpushMessage.updated = mTimestamp;
            return xpushMessage;
        }
    }

    @Override
    public String toString(){
        return "XPushMessage{" +
                "rowId='" + rowId + '\'' +
                ", id='" + id + '\'' +
                ", senderId='" + senderId + '\'' +
                ", image='" + image + '\'' +
                ", message='" + message + '\'' +
                ", type='" + type + '\'' +
                ", updated='" + updated + '\'' +
                '}';
    }
}

