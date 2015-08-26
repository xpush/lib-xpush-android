package io.xpush.chat.models;

import android.database.Cursor;

import org.json.JSONObject;

import java.net.URLDecoder;

import io.xpush.chat.persist.ChannelTable;
import io.xpush.chat.persist.MessageTable;

public class XPushMessage {

    public static final int TYPE_SEND_MESSAGE = 0;
    public static final int TYPE_RECEIVE_MESSAGE = 1;

    public static final int TYPE_LOG = 2;
    public static final int TYPE_ACTION = 3;

    private String rowId;
    private String id;
    private String channel;
    private String sender;
    private String image;
    private String count;
    private String message;
    private int type;
    private long updated;

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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
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

    public XPushMessage(){
    }

    public XPushMessage(JSONObject data) {

        JSONObject uo = null;
        try {
            if( data.has("UO") ) {
                uo = data.getJSONObject("UO");

                if( uo.has("U") ){
                    this.sender = uo.getString("U");
                }

                if( uo.has("I") ) {
                    this.image = uo.getString("I");
                }
            }

            this.message = URLDecoder.decode( data.getString("MG"), "UTF-8");
            this.updated = data.getLong("TS");
            this.channel = data.getString("C");

            this.id = channel +"_" + updated;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public XPushMessage(Cursor cursor){
        this.rowId= cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.KEY_ID));
        this.id= cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.KEY_ID));
        this.sender= cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.KEY_SENDER));
        this.image= cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.KEY_IMAGE));
        this.message= cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.KEY_MESSAGE));
        this.type= cursor.getInt(cursor.getColumnIndexOrThrow(MessageTable.KEY_TYPE));
        this.updated= cursor.getLong(cursor.getColumnIndexOrThrow(MessageTable.KEY_UPDATED));
    }

    public static class Builder {
        private final int mType;
        private String mUsername;
        private String mMessage;
        private long mTimestamp;

        public Builder(int type) {
            mType = type;
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
            xpushMessage.sender= mUsername;
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
                ", sender='" + sender + '\'' +
                ", image='" + image + '\'' +
                ", message='" + message + '\'' +
                ", type='" + type + '\'' +
                ", updated='" + updated + '\'' +
                '}';
    }
}

