package io.xpush.chat;

import android.app.Application;

import io.xpush.chat.core.XPushCore;

public class ApplicationController extends Application {

    public static final String TAG = ApplicationController.class.getSimpleName();
    private static ApplicationController mInstance;

    public static synchronized ApplicationController getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        XPushCore.initialize(this);
    }
}