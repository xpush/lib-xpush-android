package io.xpush.chat.persist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.xpush.chat.models.XPushMessage;

public class XPushMessageDataSource extends DataSource<XPushMessage> {

    public final static String TAG = XPushMessageDataSource.class.getSimpleName();

    private String mTableName;
    private String mUserTableName;

    public XPushMessageDataSource(SQLiteDatabase database, String tableName) {
        super(database);
        this.mTableName = tableName;
        this.mUserTableName = null;
    }

    public XPushMessageDataSource(SQLiteDatabase database, String tableName, String userTableName) {
        super(database);
        this.mTableName = tableName;
        this.mUserTableName = userTableName;
    }

    @Override
    public boolean insert(XPushMessage entity) {
        if (entity == null) {
            return false;
        }
        long result = mDatabase.insert(mTableName, null,
                generateContentValuesFromObject(entity));
        return result != -1;
    }

    @Override
    public boolean delete(XPushMessage entity) {
        if (entity == null) {
            return false;
        }
        int result = mDatabase.delete(mTableName,
                MessageTable.KEY_ID + " = " + entity.getId(), null);
        return result != 0;
    }

    @Override
    public boolean update(XPushMessage entity) {
        if (entity == null) {
            return false;
        }
        int result = mDatabase.update(mTableName,
                generateContentValuesFromObject(entity), MessageTable.KEY_ID + " = "
                        + entity.getId(), null);
        return result != 0;
    }

    @Override
    public List<XPushMessage> read() {

        Cursor cursor = mDatabase.query(mTableName, getAllColumns(), null,
                null, null, null, null);
        List messages = new ArrayList();
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                messages.add(generateObjectFromCursor(cursor));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return messages;
    }

    @Override
    public List<XPushMessage> read(String selection, String[] selectionArgs,
                           String groupBy, String having, String orderBy) {

        Cursor cursor = null;

        if( "".equals( mUserTableName ) || mUserTableName == null ){
            cursor = mDatabase.query(mTableName, getAllColumns(), selection, selectionArgs, groupBy, having, orderBy);
        } else {
            String messageTableAlias = "a";
            String userTableAlias = "b";

            StringBuffer query = new StringBuffer();
            query.append("select ");
            query.append(MessageTable.KEY_CHANNEL).append(", ");
            query.append(messageTableAlias+"."+MessageTable.KEY_ID).append(", ");
            query.append(UserTable.KEY_NAME).append(" as sender, ");
            query.append(userTableAlias+"."+UserTable.KEY_IMAGE).append(" as image, ");
            query.append(MessageTable.KEY_COUNT).append(", ");
            query.append(messageTableAlias+"."+MessageTable.KEY_MESSAGE).append(", ");
            query.append(messageTableAlias+"."+MessageTable.KEY_TYPE).append(", ");
            query.append(messageTableAlias+"."+MessageTable.KEY_UPDATED).append(" ");
            query.append(" from " + mTableName + " " + messageTableAlias + " LEFT outer join " + mUserTableName + " " + userTableAlias);
            query.append(" on " + messageTableAlias + "." + MessageTable.KEY_SENDER + "=" + userTableAlias + "." + UserTable.KEY_ID);

            cursor = mDatabase.rawQuery(query.toString() +" where " +selection+ " order by " + orderBy , selectionArgs);
        }

        List messages = new ArrayList();
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                messages.add(generateObjectFromCursor(cursor));
                cursor.moveToNext();
            }
            cursor.close();
        }

        return messages;
    }

    @Override
    public int count() {
        Cursor mCount = mDatabase.rawQuery("select count(*) from " + mTableName, null );
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();

        return count;
    }

    @Override
    public int count(String selection, String[] selectionArgs) {
        Cursor mCount = mDatabase.rawQuery("select count(*) from " + mTableName + " where " + selection, selectionArgs);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();

        return count;
    }

    public String[] getAllColumns() {
        return new String[] { MessageTable.KEY_ROWID,
                MessageTable.KEY_CHANNEL,
                MessageTable.KEY_ID,
                MessageTable.KEY_SENDER ,
                MessageTable.KEY_IMAGE ,
                MessageTable.KEY_COUNT ,
                MessageTable.KEY_MESSAGE ,
                MessageTable.KEY_TYPE ,
                MessageTable.KEY_UPDATED };
    }

    public XPushMessage generateObjectFromCursor(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        XPushMessage message = new XPushMessage(cursor);
        return message;
    }

    public ContentValues generateContentValuesFromObject(XPushMessage entity) {
        if (entity == null) {
            return null;
        }
        ContentValues values = new ContentValues();
        values.put(MessageTable.KEY_CHANNEL, entity.getChannel());
        values.put(MessageTable.KEY_ID, entity.getId());
        values.put(MessageTable.KEY_SENDER, entity.getSenderId());
        values.put(MessageTable.KEY_IMAGE, entity.getImage());
        values.put(MessageTable.KEY_COUNT, entity.getCount());
        values.put(MessageTable.KEY_MESSAGE, entity.getMessage());
        values.put(MessageTable.KEY_TYPE, entity.getType());
        values.put(MessageTable.KEY_UPDATED, entity.getUpdated());
        return values;
    }
}