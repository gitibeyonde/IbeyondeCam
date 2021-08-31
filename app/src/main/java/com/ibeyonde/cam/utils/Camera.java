package com.ibeyonde.cam.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Camera {
    private static final String TAG= Camera.class.getCanonicalName();

    public static int _total=0;
    public int _index;
    public String _uuid;
    public String _name;
    public String _timezone;
    public String _capabilities;
    public String _localIp;
    public String _visibleIp;
    public String _port;
    public String _token;
    public History _history;

    public Camera(JSONObject json){
        try {
            _index = _total++;
            _uuid = json.getString("uuid");
            _name = json.getString("device_name");
            _timezone = json.getString("timezone");
            _capabilities = json.getString("capabilities");
            _localIp = json.getString("deviceip");
            _visibleIp = json.getString("visibleip");
            _port = json.getString("port");
            _token = json.getString("token");
        } catch (JSONException e) {
            Log.i(TAG, "Open Jobs Exception" + e.getMessage());
        }
    }

    public void setHistory(String imgList) throws JSONException {
        _history = new History(imgList);
    }
}
