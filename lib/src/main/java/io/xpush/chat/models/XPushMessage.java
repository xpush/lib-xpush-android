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

    private int mType;

    private String mUsername = "";
    private String mMessage;
    private String mImage = "";
    private String mChannel;
    private long mTimestamp;


    private XPushMessage() {}

    public int getType() {
        return mType;
    };

    public void setType(int type) {
        this.mType = type;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        this.mUsername = username;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public String getImage() {
        return mImage;
    }

    public void setImage(String image) {
        this.mImage = image;
    }

    public String getChannel() {
        return mChannel;
    }

    public void setChannel(String mChannel) {
        this.mChannel = mChannel;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public XPushMessage(JSONObject data) {

        JSONObject uo = null;
        try {
            if( data.has("UO") ) {
                uo = data.getJSONObject("UO");

                if( uo.has("U") ){
                    this.mUsername = uo.getString("U");
                }

                if( uo.has("I") ) {
                    this.mImage = uo.getString("I");
                }
            }

            this.mMessage = URLDecoder.decode( data.getString("MG"), "UTF-8");
            this.mTimestamp = data.getLong("TS");
            this.mChannel = data.getString("C");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public XPushMessage(Cursor cursor){
        //this.id= cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.KEY_ID));
        this.mUsername= cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.KEY_SENDER));
        this.mImage= cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.KEY_IMAGE));
        //this.count= cursor.getInt(cursor.getColumnIndexOrThrow(MessageTable.KEY_COUNT));
        this.mMessage= cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.KEY_MESSAGE));
        this.mType= cursor.getInt(cursor.getColumnIndexOrThrow(MessageTable.KEY_TYPE));
        this.mTimestamp= cursor.getLong(cursor.getColumnIndexOrThrow(MessageTable.KEY_UPDATED));
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
            xpushMessage.mType = mType;
            xpushMessage.mUsername = mUsername;
            xpushMessage.mMessage = mMessage;
            xpushMessage.mTimestamp = mTimestamp;
            return xpushMessage;
        }
    }
}

