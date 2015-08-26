package io.xpush.chat.persist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.xpush.chat.R;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "xpush.db";
    private static final int DATABASE_VERSION = 0;
    private String CHANNEL_TABLE_NAME;
    private String MESSAGE_TABLE_NAME;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        CHANNEL_TABLE_NAME = context.getString(R.string.channel_table_name);
        MESSAGE_TABLE_NAME = context.getString(R.string.message_table_name);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        MessageTable.onCreate(db, MESSAGE_TABLE_NAME);
        ChannelTable.onCreate(db, CHANNEL_TABLE_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MessageTable.onUpgrade(db, oldVersion, newVersion, MESSAGE_TABLE_NAME);
        ChannelTable.onUpgrade(db, oldVersion, newVersion, CHANNEL_TABLE_NAME);
    }

    public String getMessageTable(){
        return MESSAGE_TABLE_NAME;
    }
}