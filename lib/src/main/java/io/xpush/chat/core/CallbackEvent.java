package io.xpush.chat.core;

import org.json.JSONException;
import org.json.JSONObject;

public interface CallbackEvent {
    public void call(Object... args);
}