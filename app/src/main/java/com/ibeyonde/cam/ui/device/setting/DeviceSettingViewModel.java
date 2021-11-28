package com.ibeyonde.cam.ui.device.setting;

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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ibeyonde.cam.ui.device.lastalerts.DeviceViewModel;
import com.ibeyonde.cam.ui.login.LoginViewModel;
import com.ibeyonde.cam.utils.Camera;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DeviceSettingViewModel extends ViewModel {
    private static final String TAG= DeviceSettingViewModel.class.getCanonicalName();

    public static String _veil;
    public static final Map<String, String> _cam_nv= new HashMap<>();
    public static Integer _latest_version=0;
    public static final MutableLiveData<Boolean> _device_online = new MutableLiveData<>();


    public void getVeil(Context ctx, String uuid){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String localUrl ="https://ping.ibeyonde.com/api/iot.php?view=veil&uuid=" + uuid;

        Log.i(TAG, localUrl);
        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, localUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "getVeil " + response);
                        _veil = response.trim();
                        getLatestVersion(ctx, uuid);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "getVeil Request failed ," + error.getMessage());
                _device_online.setValue(false);
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
        queue.add(stringRequest);
    }

    public void getLatestVersion(Context ctx, String uuid){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String localUrl ="https://ping.ibeyonde.com/api/esp32_scb.php?uuid=" + uuid;

        Log.i(TAG, localUrl);
        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, localUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "getLatestVersion " + response);
                        String veil  = response.split("-")[0];
                        if (veil == _veil) {
                            _latest_version=Integer.parseInt(response.split("-")[0]);
                        }
                        else {
                            _latest_version=0;
                        }
                        getConfig(ctx, uuid);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "getLatestVersion Request failed ," + error.getMessage());
                _device_online.setValue(false);
            }
        });
        queue.add(stringRequest);
    }


    public void getConfig(Context ctx, String uuid){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        Camera c = DeviceViewModel.getCamera(uuid);
        String localUrl ="http://" + c._localIp + "/cmd?name=getconf&veil=" + _veil;

        Log.i(TAG, localUrl);
        JsonObjectRequest stringRequest = new JsonObjectRequest(JsonObjectRequest.Method.GET, localUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            _cam_nv.put("history", response.getString("history"));
                            _cam_nv.put("cloud", response.getString("cloud"));
                            _cam_nv.put("timezone", response.getString("timezone"));
                            _cam_nv.put("name", response.getString("name"));
                            _cam_nv.put("version", response.getString("version"));
                            Log.i(TAG, "quality " + response.getString("quality"));
                            _cam_nv.put("quality",  response.getString("quality"));
                            _cam_nv.put("framesize",  response.getString("framesize"));
                            _cam_nv.put("pixformat",  response.getString("pixformat"));
                            Log.i(TAG, "framesize " + response.getString("framesize"));
                            _cam_nv.put("brightness",  response.getString("brightness"));
                            _cam_nv.put("contrast",  response.getString("contrast"));
                            _cam_nv.put("saturation",  response.getString("saturation"));
                            _cam_nv.put("sharpness",  response.getString("sharpness"));
                            Log.i(TAG, "brightness " + response.getString("brightness"));
                            _cam_nv.put("agc_gain",  response.getString("agc_gain"));
                            _cam_nv.put("gainceiling",  response.getString("gainceiling"));
                            _cam_nv.put("hmirror",  response.getString("hmirror"));
                            _cam_nv.put("vflip",  response.getString("vflip"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //{"history": "true","cloud": "true","timezone": "Asia/Calcutta","name": "Test"}
                        _device_online.postValue(true);
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

    public void applyDeviceConfig(Context ctx, String uuid, String var, String val){
        if (_cam_nv.get(var).equals(val))return;

        RequestQueue queue = Volley.newRequestQueue(ctx);
        Camera c = DeviceViewModel.getCamera(uuid);
        String localUrl ="http://" + c._localIp + "/cmd?name=config&var=" + var + "&val=" + val+ "&veil=" + _veil;

        Log.i(TAG, localUrl);
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

    public void applyCamConfig(Context ctx, String uuid, String var, String val){
        if (_cam_nv.get(var).equals(val))return;
        RequestQueue queue = Volley.newRequestQueue(ctx);
        Camera c = DeviceViewModel.getCamera(uuid);
        String localUrl ="http://" + c._localIp + "/cmd?name=camconf&var=" + var + "&val=" + val + "&veil=" + _veil;

        Log.i(TAG, localUrl);
        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, localUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "camConnect " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "camConnect Request failed ," + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    public void command(Context ctx, String cmd, String uuid){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        Camera c = DeviceViewModel.getCamera(uuid);
        String localUrl ="http://" + c._localIp + "/cmd?name=" + cmd + "&veil=" + _veil;

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