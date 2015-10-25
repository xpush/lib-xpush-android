package io.xpush.chat.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.xpush.chat.R;
import io.xpush.chat.models.XPushChannel;

public class PushMsgReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 0;
    private static final String TAG = PushMsgReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context mContext, Intent intent) {
        if (intent != null) {

            String action = intent.getAction().toString();
            Bundle extras = intent.getExtras();

            if(action.isEmpty()){
            }else if ("io.xpush.chat.MGRECVD".equals(action)) {

                String channel = intent.getStringExtra("rcvd.C");
                String name = intent.getStringExtra("rcvd.NM");
                String message = intent.getStringExtra("rcvd.MG");
                showNotification(mContext, name, channel, message);
            }else if("com.google.android.c2dm.intent.RECEIVE".equals(action)) {    // gcm msg receive
                if (!extras.isEmpty()) {
                    Log.d(TAG, extras.toString());
                    try {
                        JSONObject userData =  new JSONObject(extras.getString("UO"));
                        showNotification(mContext, userData.getString("NM"), extras.getString("C"), extras.getString("MG"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void showNotification(Context mContext, String name, String channel, String message) {
        Log.d(TAG, "showNotification");

        String startActivity = null;
        Class cls = null;
        try {
            startActivity = (String) mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA).metaData.get("INTRO_ACTIVITY");
            cls = Class.forName(startActivity);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        XPushChannel xpushChannel = new XPushChannel();
        xpushChannel.setId( channel );
        Bundle bundle = xpushChannel.toBundle();

        Intent intent = new Intent(mContext, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(xpushChannel.CHANNEL_BUNDLE, bundle);

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.drawable.ic_launcher);//required
        mBuilder.setContentTitle(name);//required
        mBuilder.setContentText(message);//required
        mBuilder.setTicker( mContext.getString(R.string.app_id));//optional
        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(contentIntent);

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);

        boolean isScreenOn = isInteractive(pm);

        Log.e("screen on.....", "" + isScreenOn);
        PowerManager.WakeLock wl=null, wl_cpu = null;

        if (isScreenOn == false) {
            wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyLock");
            wl.acquire(5000);
            wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock");
            wl_cpu.acquire(5000);
        }

        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        if (wl != null)
            wl.release();

        if(wl_cpu != null)
            wl_cpu.release();
    }

    public static boolean isInteractive(PowerManager pm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return pm.isInteractive();
        } else {
            return !pm.isScreenOn();
        }
    }
}
