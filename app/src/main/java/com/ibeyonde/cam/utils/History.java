package com.ibeyonde.cam.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class History {
    private static final String TAG= History.class.getCanonicalName();
    public int _total;
    public int _current;

    public ArrayList<JSONObject> _history_list = new ArrayList<>();

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
