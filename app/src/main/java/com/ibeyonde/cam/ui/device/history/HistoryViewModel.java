package com.ibeyonde.cam.ui.device.history;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ibeyonde.cam.MainActivity;
import com.ibeyonde.cam.ui.login.LoginViewModel;
import com.ibeyonde.cam.utils.History;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class HistoryViewModel extends ViewModel {
    private static final String TAG= HistoryViewModel.class.getCanonicalName();

    public static History _history_list;
    public final MutableLiveData<Short> _history = new MutableLiveData<>();

    public void getHistoryOn(Context ctx, String uuid, String date, int hour, int size){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url ="https://ping.ibeyonde.com/api/iot.php?view=history&uuid=" + uuid + "&cnt=" + size;//&date=" + date + "&hour=" + hour;

        JsonArrayRequest stringRequest = new JsonArrayRequest(JsonObjectRequest.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        Log.d(TAG, "History list json " + jsonArray.toString());
                        try {
                            _history_list = new History(jsonArray);
                            _history.postValue((short) 1);
                        } catch (JSONException e) {
                            Log.i(TAG, "FATAL JSON Exception: deviceList Device List " + e.getMessage());
                            _history.postValue((short) 0);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "ERROR Response: getHistoryOn Request failed ," + error.getMessage());
                _history.postValue((short) 0);
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

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(120000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }
}