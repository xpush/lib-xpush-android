package io.xpush.chat.models;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import io.xpush.chat.persist.ChannelTable;

public class XPushChannel {

    public static final String CHANNEL_BUNDLE = "CHANNEL_BUNDLE";

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String USERS = "users";
    public static final String USER_NAMES = "user_names";
    public static final String IMAGE = "image";
    public static final String COUNT = "count";
    public static final String MESSAGE = "message";
    public static final String UPDATED = "updated";

    public String rowId;
    public String id;
    public String name;
    public ArrayList<String> users;

    public ArrayList<String> userNames;
    public String image;
    public int count;
    public String message;
    public long updated;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public ArrayList<String> getUserNames() {
        return userNames;
    }

    public void setUserNames(ArrayList<String> userNames) {
        this.userNames = userNames;
    }

    public XPushChannel(){
    }

    public XPushChannel(Cursor cursor){
        this.rowId= cursor.getString(cursor.getColumnIndexOrThrow(ChannelTable.KEY_ROWID));
        this.id= cursor.getString(cursor.getColumnIndexOrThrow(ChannelTable.KEY_ID));
        this.name= cursor.getString(cursor.getColumnIndexOrThrow(ChannelTable.KEY_NAME));
        this.image= cursor.getString(cursor.getColumnIndexOrThrow(ChannelTable.KEY_IMAGE));
        this.count= cursor.getInt(cursor.getColumnIndexOrThrow(ChannelTable.KEY_COUNT));
        this.message= cursor.getString(cursor.getColumnIndexOrThrow(ChannelTable.KEY_MESSAGE));
        this.updated= cursor.getLong(cursor.getColumnIndexOrThrow(ChannelTable.KEY_UPDATED));
        String usersStr = cursor.getString(cursor.getColumnIndexOrThrow(ChannelTable.KEY_USERS));
        if( usersStr != null && usersStr.indexOf("#!#") > 0 ) {
            this.users = new ArrayList<String>(Arrays.asList(usersStr.split("#!#")));
        }
    }

    public XPushChannel(Bundle bundle){
        this.id= bundle.getString(ID);
        this.name= bundle.getString(NAME);
        this.users= bundle.getStringArrayList(USERS);
        this.userNames= bundle.getStringArrayList(USER_NAMES);
        this.image= bundle.getString(IMAGE);
        this.count= bundle.getInt(COUNT);
        this.message= bundle.getString(MESSAGE);
        this.updated= bundle.getLong(UPDATED);
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putString(ID, this.id);
        b.putString(NAME, this.name);
        b.putStringArrayList(USERS, this.users);
        b.putStringArrayList(USER_NAMES, this.userNames);

        b.putString(IMAGE, this.image);
        b.putInt(COUNT, this.count);
        b.putString(MESSAGE, this.message);
        b.putLong(UPDATED, this.updated);


        return b;
    }

    @Override
    public String toString(){
        return "XPushChannel{" +
                "rowId='" + rowId + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", users='" + users + '\'' +
                ", image='" + image + '\'' +
                ", count='" + count + '\'' +
                ", message='" + message + '\'' +
                ", updated='" + updated + '\'' +
                '}';
    }
}

