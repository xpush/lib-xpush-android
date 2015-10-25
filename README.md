# XPUSH android library

Android library for xpush

## Setting-up your project

	git clone https://github.com/xpush/lib-xpush-android.git

And open/import this project in Android Studio.


## Usage
Initialize XPushCore in user application. 

```java
public class MyApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
	    XPushCore.initialize(this);	    
	}
}
```

AndroidManifest.xml
```xml
<activity
    android:name="io.xpush.chat.activities.ChatActivity"
    android:windowSoftInputMode="adjustResize">
</activity>

<receiver
    android:name="io.xpush.chat.services.PushMsgReceiver"
    android:permission="com.google.android.c2dm.permission.SEND" >
    <intent-filter>
        <action android:name="com.google.android.c2dm.intent.RECEIVE" />
        <action android:name="io.xpush.chat.MGRECVD" />
    </intent-filter>
</receiver>

<receiver android:name="io.xpush.chat.services.ChangeStatusReceiver">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
        <action android:name="android.intent.action.USER_PRESENT" />
        <action android:name="android.intent.action.ACTION_PACKAGE_RESTARTED" />
        <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
    </intent-filter>
</receiver>

<service android:name="io.xpush.chat.services.XPushService" android:enabled="true" android:exported="true" >
</service>

<service android:name="io.xpush.chat.services.RegistrationIntentService" android:exported="false" >
</service>

<meta-data android:name="INTRO_ACTIVITY" android:value="io.xpush.chat.activities.ChatActivity"/>

<provider
    android:name="io.xpush.chat.persist.XpushContentProvider"
    android:authorities="@string/content_provider_authority" />

```

## Sample application

SampleChat is simple chat application using [XPUSH](https://github.com/xpush/xpush-chat).
This sample project helps you learn how to use XPUSH platform.

## License
[MIT Licensed](https://github.com/xpush/lib-xpush-android/blob/master/LICENSE).