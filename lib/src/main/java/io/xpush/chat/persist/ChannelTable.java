package io.xpush.chat.persist;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by luffy on 2015-07-05.
 */
public class ChannelTable {

    public static final String SEPARATOR = "#$SEP$#";

    public static final String KEY_ROWID = "_id";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_USERS = "users";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_COUNT = "count";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_UPDATED = "updated";
    public static String SQLITE_TABLE;

    public static void onCreate(SQLiteDatabase db, String tableName) {
        SQLITE_TABLE = tableName;

        db.execSQL("DROP TABLE IF EXISTS " + tableName);

        String DATABASE_CREATE =
                "CREATE TABLE if not exists " + tableName + " (" +
                        KEY_ROWID + " integer PRIMARY KEY autoincrement," +
                        KEY_ID + "," +
                        KEY_NAME + "," +
                        KEY_USERS + "," +
                        KEY_IMAGE + "," +
                        KEY_COUNT + " integer ," +
                        KEY_MESSAGE + "," +
                        KEY_UPDATED + " integer );";

        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion, String tableName) {
        SQLITE_TABLE = tableName;

        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(db, tableName);
    }
}
