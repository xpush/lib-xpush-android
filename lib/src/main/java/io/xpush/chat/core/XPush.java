package io.xpush.chat.core;

import com.github.nkzawa.socketio.client.Socket;

import io.xpush.chat.ApplicationController;

/**
 * Created by 정진영 on 2015-08-05.
 */
public class XPush {
    public static XPush instance;

    private String host;
    private String appId;

    private Socket mSessionSocket;
    private Socket mChannelSocket;

    public static XPush getInstance(){
        if( instance == null ) {
            instance = new XPush();
            instance.init();
        }

        return instance;
    }

    public XPush(){
        this.host = ApplicationController.getInstance().getXpushSession().getServerUrl();
        this.appId = ApplicationController.getInstance().getXpushSession().getAppId();
    }

    public void init(){

    }
}