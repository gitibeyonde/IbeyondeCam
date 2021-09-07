package com.ibeyonde.cam.ui.device.lastalerts;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class DeviceViewModel extends ViewModel {
    private static final String TAG= DeviceViewModel.class.getCanonicalName();
    public static final MutableLiveData<Hashtable<String, Camera>> _deviceList = new MutableLiveData<>();
    public static final MutableLiveData<Short> _update = new MutableLiveData<>();

    public Camera getCamera(String uuid){
        Hashtable<String, Camera> cl = _deviceList.getValue();
        return cl.get(uuid);
    }

    public boolean isAllCameraWithHistory(){
        Hashtable<String, Camera> cl = _deviceList.getValue();
        Iterator<Camera> e = cl.values().iterator();
        while (e.hasNext()) {
            if (!e.next().isHistorySet()) return true;
        }
        return true;
    }

    public void deviceList(Context ctx){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url ="https://ping.ibeyonde.com/api/iot.php?view=devicelist";

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
                                Log.d(TAG, "deviceList Device = " + json.toString());
                                Camera c = new Camera(json);
                                jl.put(c._uuid, c);
                            }
                            _deviceList.setValue(jl);
                        } catch (JSONException e) {
                            Log.i(TAG, "deviceList Device List" + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "deviceList Request failed ," + error.getMessage());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s", LoginViewModel._email, LoginViewModel._pass);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
        };
        queue.add(stringRequest);
    }


    public void getHistory(Context ctx, String uuid){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url ="https://ping.ibeyonde.com/api/iot.php?view=lastalerts&uuid=" + uuid;
        Hashtable<String, Camera> deviceList = _deviceList.getValue();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String imageList) {
                        Log.d(TAG, "deviceList History img list "+ imageList);
                        Camera c = deviceList.get(uuid);
                        try {
                            c.setHistory(imageList);
                            _update.postValue((short)1);
                        } catch (JSONException e) {
                            Log.d(TAG, "getHistory JSON Exception ," + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "getHistory Request failed ," + error.getMessage());
            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s", LoginViewModel._email, LoginViewModel._pass);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }

        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

}

