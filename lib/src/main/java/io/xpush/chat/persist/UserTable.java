package io.xpush.chat.persist;

import android.database.sqlite.SQLiteDatabase;

public class UserTable {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_UPDATED = "updated";
    public static final String KEY_TYPE = "type";
    public static String SQLITE_TABLE;

    public static void onCreate(SQLiteDatabase db, String tableName) {
        SQLITE_TABLE = tableName;

        db.execSQL("DROP TABLE IF EXISTS " + tableName);

        String DATABASE_CREATE =
                "CREATE TABLE if not exists " + tableName + " (" +
                        KEY_ROWID + " integer PRIMARY KEY autoincrement," +
                        KEY_ID + " unique ," +
                        KEY_NAME + "," +
                        KEY_IMAGE + "," +
                        KEY_MESSAGE + "," +
                        KEY_TYPE +" integer," +
                        KEY_UPDATED + " integer );";

        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion, String tableName) {
        SQLITE_TABLE = tableName;

        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(db, tableName);
    }
}
