package io.xpush.chat.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by 정진영 on 2015-08-22.
 */
public class JSONUtils {

    public static Map<String, String> jsonToMap(JSONObject object) {
        Map<String, String> result = new HashMap<String, String>();

        try {
            Iterator<String> keysItr = object.keys();
            while(keysItr.hasNext()) {
                String key = keysItr.next();
                String value = object.getString(key);
                result.put(key, value);
            }
        } catch ( JSONException e ) {
            result = null;
        }

        return result;
    }

    public static JSONObject mapToJson(Map<String, String> map){
        JSONObject result = new JSONObject();
        Iterator<String> keysItr = map.keySet().iterator();

        try {
            while (keysItr.hasNext()) {
                String key = keysItr.next();
                String value = map.get(key);
                result.put(key, value);
            }
        } catch ( JSONException e ){
            result = null;
        }
        return result;
    }
}
