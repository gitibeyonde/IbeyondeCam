package com.ibeyonde.cam.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Alerts {
    private static final String TAG= Alerts.class.getCanonicalName();
    public ArrayList<JSONObject> _alerts = new ArrayList<>();
    public int _total;


    public Alerts(JSONArray jr)throws JSONException {
        Log.d(TAG, jr.toString());
        _total = jr.length();
        for(int i=0;i< jr.length();i++){
            JSONObject jo = jr.getJSONObject(i);
            _alerts.add(jo);
        }
    }
    public ArrayList<JSONObject> getAlerts(){
        return _alerts;
    }
}
