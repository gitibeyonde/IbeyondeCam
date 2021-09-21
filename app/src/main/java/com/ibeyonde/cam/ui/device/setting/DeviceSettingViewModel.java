package com.ibeyonde.cam.ui.device.setting;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.util.MutableBoolean;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ibeyonde.cam.ui.device.history.HistoryViewModel;
import com.ibeyonde.cam.ui.device.lastalerts.DeviceViewModel;
import com.ibeyonde.cam.ui.login.LoginViewModel;
import com.ibeyonde.cam.utils.Camera;
import com.ibeyonde.cam.utils.History;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DeviceSettingViewModel extends ViewModel {
    private static final String TAG= DeviceSettingViewModel.class.getCanonicalName();

    public static final MutableLiveData<Boolean> _device_online = new MutableLiveData<>();
    static boolean _history, _cloud;
    static String _timezone, _name;

    public void getConfig(Context ctx, String uuid){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        Camera c = DeviceViewModel.getCamera(uuid);
        String localUrl ="http://" + c._localIp + ":81/getcfg";

        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, localUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "checking local url " + response);
                        _device_online.setValue(true);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "getConfig Request failed ," + error.getMessage());
                _device_online.setValue(false);
            }
        });
        queue.add(stringRequest);
    }

    public void cloudConnect(Context ctx, String uuid, Boolean enable){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        Camera c = DeviceViewModel.getCamera(uuid);
        String localUrl ="http://" + c._localIp + ":81/cfg?var=cloud&val=" + enable;

        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, localUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "cloudConnect " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "cloudConnect Request failed ," + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    public void motionHistory(Context ctx, String uuid, Boolean enable){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        Camera c = DeviceViewModel.getCamera(uuid);
        String localUrl ="http://" + c._localIp + ":81/cfg?var=history&val=" + enable;

        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, localUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "motionHistory " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "motionHistory Request failed ," + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }
    public void deleteHistory(Context ctx, String uuid){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url ="https://ping.ibeyonde.com/api/iot.php?view=delhist&uuid=" + uuid;//&date=" + date + "&hour=" + hour;

        JsonObjectRequest stringRequest = new JsonObjectRequest(JsonObjectRequest.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject json) {
                        Log.d(TAG, "History list json " + json.toString());
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

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(120000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }
}