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

    public static final MutableLiveData<Boolean> _device_online = new MutableLiveData<>();
    public static final MutableLiveData<Boolean> _cam_config = new MutableLiveData<>();
    public static final Map<String, String> _dev_nv = new HashMap<>();
    public static final Map<String, String> _cam_nv= new HashMap<>();

    public void getConfig(Context ctx, String uuid){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        Camera c = DeviceViewModel.getCamera(uuid);
        String localUrl ="http://" + c._localIp + ":81/getcfg";

        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, localUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "checking local url " + response);
                        //history=true&cloud=true&tz=Asia/Calcutta&cn=CleverCam
                        String []nvls = response.split("&");
                        for (String nvs: nvls) {
                            String nv[] = nvs.split("=");
                            _dev_nv.put(nv[0], nv[1]);
                        }
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

    public void getCam(Context ctx, String uuid){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        Camera c = DeviceViewModel.getCamera(uuid);
        String localUrl ="http://" + c._localIp + ":81/getcam";

        JsonObjectRequest stringRequest = new JsonObjectRequest(JsonObjectRequest.Method.GET, localUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
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
                        //{"0xd3":8,"0x111":0,"0x132":9,"xclk":16,"pixformat":3,"framesize":8,"quality":10,"brightness":0,"contrast":0,"saturation":0,
                        // "sharpness":0,"special_effect":0,"wb_mode":0,"awb":1,"awb_gain":1,"aec":1,"aec2":0,"ae_level":0,
                        // "aec_value":168,"agc":1,"agc_gain":0,"gainceiling":0,"bpc":0,"wpc":1,"raw_gma":1,"lenc":1,"hmirror":0,"dcw":1,"colorbar":0}%

                        _cam_config.postValue(true);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "getCam Request failed ," + error.getMessage());
                _cam_config.postValue(false);
            }
        });
        queue.add(stringRequest);
    }

    public void cloudConnect(Context ctx, String uuid, String var, String val){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        Camera c = DeviceViewModel.getCamera(uuid);
        String localUrl ="http://" + c._localIp + ":81/cfg?var=" + var + "&val=" + val;

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

    public void camConnect(Context ctx, String uuid, String var, String val){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        Camera c = DeviceViewModel.getCamera(uuid);
        String localUrl ="http://" + c._localIp + ":81/cam?var=" + var + "&val=" + val;

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