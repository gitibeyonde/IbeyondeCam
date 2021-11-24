package com.ibeyonde.cam.ui.device.live;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ibeyonde.cam.ui.device.lastalerts.DeviceViewModel;
import com.ibeyonde.cam.ui.login.LoginViewModel;
import com.ibeyonde.cam.utils.Camera;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class LiveViewModel extends ViewModel {
    private static final String TAG= LiveViewModel.class.getCanonicalName();

    public final MutableLiveData<Boolean> _url_updated = new MutableLiveData<>();
    public static String _url = null;


    public void getLiveUrl(Context ctx, String uuid){
        Camera c = DeviceViewModel.getCamera(uuid);
        if (c == null){
            RequestQueue queue = Volley.newRequestQueue(ctx);
            String url ="https://ping.ibeyonde.com/api/iot.php?view=devicelist";

            JsonArrayRequest stringRequest = new JsonArrayRequest(JsonObjectRequest.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray jsonArray) {
                            Log.d(TAG, "Device list json " + jsonArray.toString());
                            try {
                                Camera._total = 0;
                                for(int i=0;i< jsonArray.length();i++) {
                                    JSONObject json = jsonArray.getJSONObject(i);
                                    Log.d(TAG, "deviceList Device = " + json.toString());
                                    Camera c = new Camera(json);
                                    DeviceViewModel._deviceList.put(c._uuid, c);
                                }
                                getLocalLiveUrl(ctx, uuid);
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
        else {
            getLocalLiveUrl(ctx, uuid);
        }
    }


    private void getLocalLiveUrl(Context ctx, String uuid){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        Camera c = DeviceViewModel._deviceList.get(uuid);
        String localUrl ="http://" + c._localIp + "/";
        Log.d(TAG, "Local Live Request = " + localUrl);

        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, localUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("Ibeyonde")) {
                            _url="http://" + c._localIp + "/stream";
                            _url_updated.setValue(true);
                            Log.i(TAG, "Local URL value set to " + _url);
                        }
                        else {
                            getUdpLiveUrl(ctx, uuid);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Local Live Request failed ," + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    private void getUdpLiveUrl(Context ctx, String uuid){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url ="https://ping.ibeyonde.com/api/iot.php?view=live&quality=HINI&uuid=" + uuid;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String url) {
                        Log.i(TAG, "URL value ?? " + _url);
                        url = url.replaceAll("^\"|\"$", "");
                        url = url.replaceAll("\\\\", "");
                        Log.i(TAG, "Remote URL value set to " + _url);
                        _url = url;
                        _url_updated.postValue(true);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Live Request failed ," + error.getMessage());
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

    public void command(Context ctx, String cmd, String uuid){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        Camera c = DeviceViewModel.getCamera(uuid);
        String localUrl ="http://" + c._localIp + "/cmd?name=" + cmd;

        Log.i(TAG, localUrl);
        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, localUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "command " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "command Request failed ," + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }



}