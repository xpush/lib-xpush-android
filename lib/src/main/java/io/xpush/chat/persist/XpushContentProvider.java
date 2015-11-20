package io.xpush.chat.persist;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import io.xpush.chat.R;


public class XpushContentProvider extends ContentProvider {

    public static final String TAG = XpushContentProvider.class.getSimpleName();

    public static final String SQL_INSERT_OR_REPLACE = "SQL_INSERT_OR_REPLACE";

    private static final int ALL_CHANNELS = 10;
    private static final int SINGLE_CHANNEL = 11;

    private static final int ALL_MESSAGES = 20;
    private static final int SINGLE_CHANNEL_MESSAGE = 21;
    private static final int SINGLE_MESSAGE = 22;

    private static final int ALL_USERS = 30;
    private static final int SINGLE_USER = 31;

    public static String CHANNEL_URI_STRING;
    public static String MESSAGE_URI_STRING;
    public static String USER_URI_STRING;

    public static Uri CHANNEL_CONTENT_URI;
    public static Uri MESSAGE_CONTENT_URI;
    public static Uri USER_CONTENT_URI;

    private static String AUTHORITY;
    private static UriMatcher uriMatcher;

    private static String CHANNEL_TABLE;
    private static String MESSAGE_TABLE;
    private static String USER_TABLE;

    private DBHelper dbHelper;

    @Override
    public boolean onCreate() {
        // get access to the database helper

        AUTHORITY = getContext().getString(R.string.content_provider_authority);
        CHANNEL_TABLE = getContext().getString(R.string.channel_table_name);
        MESSAGE_TABLE = getContext().getString(R.string.message_table_name);
        USER_TABLE = getContext().getString(R.string.user_table_name);

        CHANNEL_URI_STRING = "content://" + AUTHORITY + "/channels";
        MESSAGE_URI_STRING = "content://" + AUTHORITY + "/messages";
        USER_URI_STRING = "content://" + AUTHORITY + "/users";

        CHANNEL_CONTENT_URI = Uri.parse(CHANNEL_URI_STRING);
        MESSAGE_CONTENT_URI = Uri.parse(MESSAGE_URI_STRING);
        USER_CONTENT_URI = Uri.parse(USER_URI_STRING);

        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AUTHORITY, "channels", ALL_CHANNELS);
        uriMatcher.addURI(AUTHORITY, "channels/*", SINGLE_CHANNEL);

        uriMatcher.addURI(AUTHORITY, "messages", ALL_MESSAGES);
        uriMatcher.addURI(AUTHORITY, "messages/*", SINGLE_CHANNEL_MESSAGE);
        uriMatcher.addURI(AUTHORITY, "messages/*/*", SINGLE_MESSAGE);

        uriMatcher.addURI(AUTHORITY, "users", ALL_USERS);
        uriMatcher.addURI(AUTHORITY, "users/*", SINGLE_USER);

        dbHelper = new DBHelper(getContext());
        return false;
    }

    @Override
    public String getType(Uri uri) {

        switch (uriMatcher.match(uri)) {
            case ALL_CHANNELS:
                return "vnd.android.cursor.dir/vnd.contentprovider.channels";
            case SINGLE_CHANNEL:
                return "vnd.android.cursor.item/vnd.contentprovider.channels";
            case ALL_MESSAGES:
                return "vnd.android.cursor.dir/vnd.contentprovider.messages";
            case SINGLE_CHANNEL_MESSAGE:
                return "vnd.android.cursor.dir/vnd.contentprovider.messages.in";
            case SINGLE_MESSAGE:
                return "vnd.android.cursor.item/vnd.contentprovider.messages";
            case ALL_USERS:
                return "vnd.android.cursor.dir/vnd.contentprovider.users";
            case SINGLE_USER:
                return "vnd.android.cursor.item/vnd.contentprovider.users";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String tableName = "";
        Uri contentUri = null;

        switch (uriMatcher.match(uri)) {
            case ALL_CHANNELS:
                contentUri = CHANNEL_CONTENT_URI;
                tableName = CHANNEL_TABLE;
                break;
            case ALL_MESSAGES:
                contentUri = MESSAGE_CONTENT_URI;
                tableName = MESSAGE_TABLE;
                break;
            case ALL_USERS:
                contentUri = USER_CONTENT_URI;
                tableName = USER_TABLE;
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        boolean replace = false;
        if ( values.containsKey( SQL_INSERT_OR_REPLACE ) ){
            replace = values.getAsBoolean( SQL_INSERT_OR_REPLACE );
            values.remove( SQL_INSERT_OR_REPLACE );
        }

        long rowId;
        if ( replace ) {
            rowId = db.replace(tableName, null, values);
        } else {
            rowId = db.insert(tableName, null, values);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(contentUri + "/" + rowId);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String id = null;
        String tableName = null;

        switch (uriMatcher.match(uri)) {
            case ALL_CHANNELS:
                tableName = CHANNEL_TABLE;
                if ( sortOrder == null ){
                    sortOrder = ChannelTable.KEY_UPDATED + " DESC";
                }

                break;
            case SINGLE_CHANNEL:
                tableName = CHANNEL_TABLE;

                id = uri.getPathSegments().get(1);

                Log.d( TAG, "===== id ====");
                Log.d( TAG, id);
                queryBuilder.appendWhere(ChannelTable.KEY_ID + "='" + id + "'");
                break;
            case ALL_MESSAGES:
                tableName = MESSAGE_TABLE;
                if ( sortOrder == null ){
                    sortOrder = MessageTable.KEY_UPDATED + " ASC";
                }

                break;
            case SINGLE_CHANNEL_MESSAGE:
                tableName = MESSAGE_TABLE;
                if ( sortOrder == null ){
                    sortOrder = MessageTable.KEY_UPDATED + " ASC";
                }

                String channel = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(MessageTable.KEY_CHANNEL + "='" + channel+"'");
                break;
            case ALL_USERS:
                tableName = USER_TABLE;
                if ( sortOrder == null ){
                    sortOrder = UserTable.KEY_NAME + " ASC";
                }

                break;
            case SINGLE_USER:
                tableName = USER_TABLE;
                if ( sortOrder == null ){
                    sortOrder = UserTable.KEY_NAME + " ASC";
                }

                String userId = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(UserTable.KEY_ID + "='" + userId+"'");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        queryBuilder.setTables(tableName);

        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String id = null;
        String tableName = null;

        switch (uriMatcher.match(uri)) {
            case ALL_CHANNELS:
                tableName = CHANNEL_TABLE;
                break;
            case SINGLE_CHANNEL:
                tableName = CHANNEL_TABLE;
                id = uri.getPathSegments().get(1);
                selection = ChannelTable.KEY_ID + "='" + id+"'"
                        + (!TextUtils.isEmpty(selection) ?
                        " AND (" + selection + ')' : "");
                break;
            case ALL_MESSAGES:
                tableName = MESSAGE_TABLE;
                break;
            case SINGLE_CHANNEL_MESSAGE:
                tableName = MESSAGE_TABLE;

                String channel = uri.getPathSegments().get(1);
                selection = MessageTable.KEY_CHANNEL + "='" + channel+"'"
                        + (!TextUtils.isEmpty(selection) ?
                        " AND (" + selection + ')' : "");
                break;
            case ALL_USERS:
                tableName = USER_TABLE;
                break;
            case SINGLE_USER:
                tableName = USER_TABLE;
                id = uri.getPathSegments().get(1);
                selection = MessageTable.KEY_ID + "='" + id+"'"
                        + (!TextUtils.isEmpty(selection) ?
                        " AND (" + selection + ')' : "");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        int deleteCount = db.delete(tableName, selection, selectionArgs);

        getContext().getContentResolver().notifyChange(uri, null);
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String id = null;
        String tableName = null;

        switch (uriMatcher.match(uri)) {
            case ALL_CHANNELS:
                tableName = CHANNEL_TABLE;
                //do nothing
                break;
            case SINGLE_CHANNEL:
                tableName = CHANNEL_TABLE;
                id = uri.getPathSegments().get(1);
                selection = ChannelTable.KEY_ID + "= '" + id + "'"
                        + (!TextUtils.isEmpty(selection) ?
                        " AND (" + selection + ')' : "");
                break;
            case ALL_MESSAGES:
                tableName = MESSAGE_TABLE;
                break;
            case SINGLE_MESSAGE:
                tableName = MESSAGE_TABLE;
                id = uri.getPathSegments().get(1);
                selection = MessageTable.KEY_ROWID + "='" + id+ "'"
                        + (!TextUtils.isEmpty(selection) ?
                        " AND (" + selection + ')' : "");
                break;
            case ALL_USERS:
                tableName = USER_TABLE;
                break;
            case SINGLE_USER:
                tableName = USER_TABLE;
                id = uri.getPathSegments().get(1);
                selection = MessageTable.KEY_ID + "='" + id+ "'"
                        + (!TextUtils.isEmpty(selection) ?
                        " AND (" + selection + ')' : "");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        int updateCount = db.update(tableName, values, selection, selectionArgs);

        getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }
}