package com.ibeyonde.cam.ui.device;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ibeyonde.cam.ui.login.LoginViewModel;
import com.ibeyonde.cam.utils.Camera;
import com.ibeyonde.cam.utils.History;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class HistoryViewModel extends ViewModel {
    private static final String TAG= HistoryViewModel.class.getCanonicalName();

    public static final MutableLiveData<History> _historyList = new MutableLiveData<>();

    public void getHistoryOn(Context ctx, String uuid, String date, int hour){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url ="https://ping.ibeyonde.com/api/iot.php?view=history&uuid=" + uuid;//&date=" + date + "&hour=" + hour;

        JsonArrayRequest stringRequest = new JsonArrayRequest(JsonObjectRequest.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        Log.d(TAG, "History list json " + jsonArray.toString());
                        try {
                            History c = new History(jsonArray);
                            _historyList.postValue(c);
                        } catch (JSONException e) {
                            Log.i(TAG, "FATAL JSON Exception: deviceList Device List " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "ERROR Response: getHistoryOn Request failed ," + error.getMessage());
            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s", LoginViewModel._email,LoginViewModel._pass);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }

        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}