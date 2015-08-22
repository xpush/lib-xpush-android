package io.xpush.chat.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.util.Log;

/**
 * Created by jhkim on 2014-12-31.
 */
public class ChangeStatusReceiver extends BroadcastReceiver {
    private static final String TAG = "XPushService";

    public synchronized void onReceive(Context context, Intent intent) {
        Log.d(TAG, "ChangeStatusReceiver onReceive() " + intent);

        String action = intent.getAction();
        if (action == null) {
            XPushService.actionStart(context.getApplicationContext());
        } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d(TAG, "BOOT_COMPLETED " + intent);
            XPushService.actionStart(context.getApplicationContext());
        } else if (action.equals("android.intent.action.ACTION_PACKAGE_RESTARTED")) {
            Log.d(TAG, "ACTION_PACKAGE_RESTARTED " + intent);
            XPushService.actionStart(context.getApplicationContext());
        } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            Log.d(TAG, "CONNECTIVITY_CHANGE " + intent);
            XPushService.actionNetworkChangeRestart(context.getApplicationContext());
            Log.d(TAG, "CHANGERECONNECT " + intent);
        } else if (action.equals( Intent.ACTION_USER_PRESENT )) {
            Log.d(TAG, "ACTION_USER_PRESENT " + intent);
            XPushService.actionReconnect(context.getApplicationContext());
        } else if (action.equals( Intent.ACTION_PACKAGE_ADDED ) ) {
            Log.d(TAG, "ACTION_PACKAGE_ADDED " + intent.getDataString());
            if( intent.getDataString().equals( "package:io.xpush.xpushchat" ) ) {
                XPushService.actionStart(context.getApplicationContext());
            }
        } else if (action.equals( Intent.ACTION_PACKAGE_REPLACED )) {
            Log.d(TAG, "ACTION_PACKAGE_REPLACED" + intent.getDataString());
            if( intent.getDataString().equals( "package:io.xpush.xpushchat" ) ){
                XPushService.actionStart(context.getApplicationContext());
            }

        } else if (action.equals( Intent.ACTION_PACKAGE_REMOVED )) {
            Log.d(TAG, "ACTION_PACKAGE_REMOVED " + intent.getDataString());
            if( intent.getDataString().equals( "package:io.xpush.xpushchat" ) ) {
                XPushService.actionStop(context.getApplicationContext());
            }

        }
    }
}