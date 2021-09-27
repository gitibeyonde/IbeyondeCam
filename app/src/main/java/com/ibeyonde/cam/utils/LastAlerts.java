package com.ibeyonde.cam.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class LastAlerts {
    private static final String TAG= com.ibeyonde.cam.utils.LastAlerts.class.getCanonicalName();
    public ArrayList<JSONArray> _lastalerts = new ArrayList<>();
    public int _total;
    public int _current;



    // DEVICE ALERTS
    public LastAlerts(String imgList) throws JSONException {
        JSONArray jr = new JSONArray(imgList);
        Log.d(TAG, jr.toString());
        _total = jr.length();
        _current = 0;
        for(int i=0;i< jr.length();i++){
            JSONArray jo = jr.getJSONArray(i);
            _lastalerts.add(jo);
        }
    }

    public String getCurrentURL() {
        try {
            JSONArray curr = _lastalerts.get(_current++);
            if (_current == _total)_current = 0;
            return curr.getString(0);
        } catch (JSONException e) {
            e.printStackTrace();
            return "http://udp1.ibeyonde.com/img/error.jpg";
        }
    }

}
