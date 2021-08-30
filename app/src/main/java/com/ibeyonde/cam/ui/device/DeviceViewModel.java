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
import com.ibeyonde.cam.utils.Camera;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class DeviceViewModel extends ViewModel {
    private static final String TAG= DeviceViewModel.class.getCanonicalName();
    public static final MutableLiveData<Hashtable<String, Camera>> _deviceList = new MutableLiveData<>();
    public static final MutableLiveData<Short> _update = new MutableLiveData<>();

    public void deviceList(Context ctx){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url ="https://ping.ibeyonde.com/api/iot.php?view=devicelist";

        // Request a string response from the provided URL.
        JsonArrayRequest stringRequest = new JsonArrayRequest(JsonObjectRequest.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        Log.d(TAG, "Device list json " + jsonArray.toString());
                        try {
                            Hashtable<String, Camera> jl = new Hashtable<>();
                            Camera._total = 0;
                            for(int i=0;i< jsonArray.length();i++) {
                                JSONObject json = jsonArray.getJSONObject(i);
                                Log.d(TAG, "Device = " + json.toString());
                                Camera c = new Camera(json);
                                jl.put(c._uuid, c);
                            }
                            _deviceList.setValue(jl);
                        } catch (JSONException e) {
                            Log.i(TAG, "Device List" + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Request failed ," + error.getMessage());
            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s", com.ibeyonde.cam32.ui.login.LoginViewModel._email, com.ibeyonde.cam32.ui.login.LoginViewModel._pass);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }

        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    public void getHistory(Context ctx, String uuid){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url ="https://ping.ibeyonde.com/api/iot.php?view=lastalerts&uuid=" + uuid;
        Hashtable<String, Camera> deviceList = _deviceList.getValue();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String imageList) {
                        Log.d(TAG, "History img list "+ imageList);
                        Camera c = deviceList.get(uuid);
                        try {
                            c.setHistory(imageList);
                            _update.postValue((short)1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Request failed ," + error.getMessage());
            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s", com.ibeyonde.cam32.ui.login.LoginViewModel._email, com.ibeyonde.cam32.ui.login.LoginViewModel._pass);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }

        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}

