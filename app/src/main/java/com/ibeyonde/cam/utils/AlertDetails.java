package com.ibeyonde.cam.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AlertDetails { private static final String TAG= History.class.getCanonicalName();
    public int _total;
    public int _current;

    public ArrayList<JSONObject> _alert_details = new ArrayList<>();



    // DEVICE ALERTS
    public AlertDetails(String imgList) throws JSONException {
        JSONArray jr = new JSONArray(imgList);
        Log.d(TAG, "Alert Details Size=" + jr.length());
        _total = jr.length();
        _current = 0;
        for(int i=0;i< jr.length();i++){
            JSONObject jo = jr.getJSONObject(i);
            _alert_details.add(jo);
        }
    }

    public String getCurrentURL() {
        try {
            JSONObject curr = _alert_details.get(_current);
            _current++;
            if (_current >= _total){
                _current = 0;
            }
            return curr.getString("url");
        } catch (Exception e) {
            return "http://udp1.ibeyonde.com/img/error.jpg";
        }
    }
}