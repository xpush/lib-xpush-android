package io.xpush.sampleChat.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

import io.xpush.chat.core.CallbackEvent;
import io.xpush.chat.core.XPushCore;
import io.xpush.chat.models.XPushSession;
import io.xpush.sampleChat.R;
import io.xpush.chat.services.RegistrationIntentService;
import io.xpush.chat.services.XPushService;

public class SplashActivity extends Activity {

    public static final String TAG = SplashActivity.class.getSimpleName();

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private int SPLASH_TIME = 1500;

    private Activity mActivity;
    private Button mReloadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        Fresco.initialize(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mActivity = this;

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        mReloadBtn = (Button) findViewById(R.id.reloadBtn);
        mReloadBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mActivity.finish();
                mActivity.startActivity(getIntent());
            }
        });

        if (checkPlayServices()) {
            if( null == pref.getString("REGISTERED_NOTIFICATION_ID", null ) ){
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            } else {
                Log.d( TAG, "TOKEN : " + pref.getString("REGISTERED_NOTIFICATION_ID", null ) );
            }
        }

        XPushService.actionStart(this);

        final long started = System.currentTimeMillis();
        final AtomicInteger i = new AtomicInteger();

        final Handler handler = new Handler();
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // connected, not yet splash or not logined
                if (  ( ( !XPushCore.isLogined() || XPushCore.isGlobalConnected() )&& System.currentTimeMillis() - started < SPLASH_TIME)
                        // not connected, splash * 4
                        || ( XPushCore.isLogined() &&!XPushCore.isGlobalConnected() && System.currentTimeMillis() - started < (SPLASH_TIME * 4))) {
                    handler.postDelayed(this, 150);
                } else {

                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    overridePendingTransition(0, android.R.anim.fade_in);

                    Intent intent = null;
                    XPushSession mSession = XPushCore.getXpushSession();
                    if (null == mSession ) {
                        intent = new Intent(SplashActivity.this, LoginActivity.class);
                    } else {

                        // differ with session notiId

                        if( !pref.getString("REGISTERED_NOTIFICATION_ID", "" ).equals( mSession.getNotiId() ) ) {
                            XPushCore.updateToken(new CallbackEvent() {
                                @Override
                                public void call(Object... args) {
                                    JSONObject result = (JSONObject) args[0];
                                    Log.d(TAG, result.toString());
                                }
                            });
                        }

                        intent = new Intent(SplashActivity.this, MainActivity.class);
                    }

                    startActivity(intent);
                    finish();
                }
            }
        }, 150);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}