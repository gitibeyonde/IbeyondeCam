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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class DeviceViewModel extends ViewModel {
    private static final String TAG= DeviceViewModel.class.getCanonicalName();
    public static final Hashtable<String, Camera> _deviceList = new Hashtable<>();
    public final MutableLiveData<Short> _history = new MutableLiveData<>();

    public static Camera getCamera(String uuid){
        if (_deviceList.size() > 0) {
            return _deviceList.get(uuid);
        }
        else {
            return null;
        }
    }

    public boolean isAllCameraWithHistory(){
        Iterator<Camera> e = _deviceList.values().iterator();
        while (e.hasNext()) {
            if (!e.next().isHistorySet()) return true;
        }
        return true;
    }

    public void getAllHistory(Context ctx){
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
                                _deviceList.put(c._uuid, c);
                                getHistory_(ctx, c._uuid, null);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "deviceList Device List" + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "deviceList Request failed ," + error.getMessage());
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


    public void getHistory_(Context ctx, String uuid, String date_time){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        Log.d(TAG, "getHistory " + date_time);
        Date d = new Date();
        if (date_time != null) {
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//2021-09-27 09:12:48
            try {
                d = sdf.parse(date_time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "getHistory " + d.toString());
        }
        String url ="https://ping.ibeyonde.com/api/iot.php?view=lastalerts&uuid=" + uuid;
        //["https:\/\/s3-us-west-2.amazonaws.com\/e","22\/09\/2021 - 14:10:41"],
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String imageList) {
                        Log.d(TAG, "deviceList History img list " + imageList);
                        Camera c = _deviceList.get(uuid);
                        if (imageList.indexOf("No alerts found for this device") == -1) {
                            try {
                                c.setLastAlerts(imageList);
                                _history.postValue((short) 1);
                            } catch (JSONException e) {
                                Log.d(TAG, "getHistory JSON Exception ," + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                c.setLastAlerts("[[\"https://udp1.ibeyonde.com/img/no_signal.jpg\", \"22\\/09\\/2021 - 14:10:41\"]]");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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

