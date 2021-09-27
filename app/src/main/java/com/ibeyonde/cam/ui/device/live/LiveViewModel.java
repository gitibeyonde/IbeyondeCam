package com.ibeyonde.cam.ui.device.live;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ibeyonde.cam.ui.device.lastalerts.DeviceViewModel;
import com.ibeyonde.cam.ui.login.LoginViewModel;
import com.ibeyonde.cam.utils.Camera;

import java.util.HashMap;
import java.util.Map;

public class LiveViewModel extends ViewModel {
    private static final String TAG= LiveViewModel.class.getCanonicalName();

    public static final MutableLiveData<String> _url = new MutableLiveData<>();

    public void getLocalLiveUrl(Context ctx, String uuid){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        Camera c = DeviceViewModel.getCamera(uuid);
        String localUrl ="http://" + c._localIp + ":81/stop";

        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, localUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "checking local url " + response);
                        if (response.contains("Ibeyonde")) {
                            _url.postValue("http://" + c._localIp + "/stream");
                        }
                        else {
                            getLiveUrl(ctx, uuid);
                        }
                        Log.i(TAG, "URL value set to " + _url.getValue());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Local Live Request failed ," + error.getMessage());
                getLiveUrl(ctx, uuid);
            }
        });
        queue.add(stringRequest);
    }

    public void getLiveUrl(Context ctx, String uuid){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url ="https://ping.ibeyonde.com/api/iot.php?view=live&quality=HINI&uuid=" + uuid;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String url) {
                        Log.i(TAG, "URL value ?? " + _url.getValue());
                        url = url.replaceAll("^\"|\"$", "");
                        url = url.replaceAll("\\\\", "");
                        _url.postValue(url);
                        Log.i(TAG, "URL value set to " + _url.getValue());
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

}