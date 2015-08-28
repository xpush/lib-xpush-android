package io.xpush.chat.models;

import android.database.Cursor;
import android.os.Bundle;

import io.xpush.chat.persist.UserTable;

public class XPushUser {

    public static final String USER_BUNDLE = "USER_BUNDLE";

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String IMAGE = "image";
    public static final String MESSAGE = "message";
    public static final String UPDATED = "updated";

    public String rowId;
    public String id;
    public String name;
    public String image;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public XPushUser(){
    }

    public XPushUser(Cursor cursor){
        this.rowId= cursor.getString(cursor.getColumnIndexOrThrow(UserTable.KEY_ROWID));
        this.id= cursor.getString(cursor.getColumnIndexOrThrow(UserTable.KEY_ID));
        this.name= cursor.getString(cursor.getColumnIndexOrThrow(UserTable.KEY_NAME));
        this.image= cursor.getString(cursor.getColumnIndexOrThrow(UserTable.KEY_IMAGE));
        this.message= cursor.getString(cursor.getColumnIndexOrThrow(UserTable.KEY_MESSAGE));
        this.updated= cursor.getLong(cursor.getColumnIndexOrThrow(UserTable.KEY_UPDATED));
    }

    public XPushUser(Bundle bundle){
        this.id= bundle.getString(ID);
        this.name= bundle.getString(NAME);
        this.image= bundle.getString(IMAGE);
        this.message= bundle.getString(MESSAGE);
        this.updated= bundle.getLong(UPDATED);
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putString(ID, this.id);
        b.putString(NAME, this.name);
        b.putString(IMAGE, this.image);
        b.putString(MESSAGE, this.message);
        b.putLong(UPDATED, this.updated);

        return b;
    }

    @Override
    public String toString(){
        return "XPushUser{" +
                "rowId='" + rowId + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", message='" + message + '\'' +
                ", updated='" + updated + '\'' +
                '}';
    }
}