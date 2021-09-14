package com.ibeyonde.cam.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class History {
    private static final String TAG= History.class.getCanonicalName();
    public ArrayList<JSONArray> _history = new ArrayList<>();
    public int _total;
    public int _current;


    public ArrayList<JSONObject> _history_list = new ArrayList<>();

    // DEVICE ALERTS
    public History(String imgList) throws JSONException {
        JSONArray jr = new JSONArray(imgList);
        Log.d(TAG, jr.toString());
        _total = jr.length();
        _current = 0;
        for(int i=0;i< jr.length();i++){
            JSONArray jo = jr.getJSONArray(i);
            _history.add(jo);
        }
    }

    public String getCurrentURL() {
        try {
            JSONArray curr = _history.get(_current++);
            if (_current == _total)_current = 0;
            return curr.getString(0);
        } catch (JSONException e) {
            e.printStackTrace();
            return "http://udp1.ibeyonde.com/img/error.jpg";
        }
    }

    // HISTORY
    public History(JSONArray jr) throws JSONException {
        Log.d(TAG, jr.toString());
        _total = jr.length();
        _current = 0;
        for(int i=0;i< jr.length();i++){
            JSONObject jo = jr.getJSONObject(i);
            _history_list.add(jo);
        }
    }


    public ArrayList<JSONObject> getHistory(){
        return _history_list;
    }
}
