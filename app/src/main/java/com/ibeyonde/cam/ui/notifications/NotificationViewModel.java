package com.ibeyonde.cam.ui.notifications;

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
import com.ibeyonde.cam.ui.login.LoginViewModel;
import com.ibeyonde.cam.utils.AlertDetails;
import com.ibeyonde.cam.utils.Alerts;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NotificationViewModel extends ViewModel {
    private static final String TAG= NotificationViewModel.class.getCanonicalName();

    public static Alerts _alerts;
    public final MutableLiveData<Short> _Ralerts = new MutableLiveData<>();
    public static AlertDetails _alert_details;
    public final MutableLiveData<Short> _Ralert_details = new MutableLiveData<>();

    /**
     * [{"id":"529667","type":"bp","uuid":"3105613C","user_name":null,"image":"3105613C\/2021\/09\/14\/06_21_54\/eqpSso.jpg","value":"0.00","comment":"",
     * "created":"2021-09-14 06:23:30","subcategory":null,"errors":[],"messages":[],"ar":{"uuid":null,"email_mask":"000000000000000000000000","pns_mask":"000000000000000000000000","email":null,
     * "grid":"0000000000000000000000000","ping":0,"all_motion":0,"no_motion_hour":0,"motion_burst":0,"unrecog":0,"recog":0,"people_count":0,"subcategory":null,"classify":0,"license":0,
     * "temp_high":999,"temp_low":-99,"humid_high":999,"humid_low":-99,"no_repeat":1,"no_repeat_delta":10,"unusual":0,"updated":null,"errors":[],"messages":[]}},
     *
     * {"id":"529666","type":"bp","uuid":"3105613C","user_name":null,"image":"3105613C\/2021\/09\/14\/06_21_17\/8Ghu4a.jpg","value":"0.00","comment":"",
     * "created":"2021-09-14 06:21:47","subcategory":null,"errors":[],"messages":[],"ar":{"uuid":null,"email_mask":"000000000000000000000000","pns_mask":"000000000000000000000000","email":null,
     * "grid":"0000000000000000000000000","ping":0,"all_motion":0,"no_motion_hour":0,"motion_burst":0,"unrecog":0,"recog":0,"people_count":0,"subcategory":null,"classify":0,"license":0,
     * "temp_high":999,"temp_low":-99,"humid_high":999,"humid_low":-99,"no_repeat":1,"no_repeat_delta":10,"unusual":0,"updated":null,"errors":[],"messages":[]}},
     *
     * @param ctx
     */

    public void getBellAlerts(Context ctx){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url ="https://ping.ibeyonde.com/api/iot.php?view=lastalerts&type=bp";
        JsonArrayRequest stringRequest = new JsonArrayRequest(JsonObjectRequest.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        Log.d(TAG, "getBellAlerts alerts list "+ jsonArray.length());
                        try {
                            _alerts = new Alerts(jsonArray);
                            _Ralerts.postValue((short) 1);
                        } catch (JSONException e) {
                            Log.e(TAG, "getBellAlerts JSON format error");
                            _Ralerts.postValue((short) 0);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "getBellAlerts Request failed ," + error.getMessage());
                _Ralerts.postValue((short) 0);
            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s", LoginViewModel._username, LoginViewModel._pass);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }

        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    public void getBellAlertDetails(Context ctx, String uuid, String date_time){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        Log.d(TAG, "getBellAlertDetails " + date_time);
        Date d = new Date();
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//2021-09-27 09:12:48
        try {
            d = sdf.parse(date_time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getBellAlertDetails " + d.toString());

        sdf = new SimpleDateFormat("yyyy/MM/dd");
        String date= sdf.format(d);
        sdf = new SimpleDateFormat("HH");
        String hour= sdf.format(d);
        sdf = new SimpleDateFormat("mm");
        String minute= sdf.format(d);
        String url ="https://ping.ibeyonde.com/api/iot.php?view=bellalerts&uuid=" + uuid + "&date=" + date  + "&hour=" + hour + "&minute=" + minute; //format path = 2016/06/02; hour = 05
        //["https:\/\/s3-us-west-2.amazonaws.com\/e","22\/09\/2021 - 14:10:41"],
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String imageList) {
                        Log.d(TAG, "getBellAlertDetails img list " + imageList);
                        if (imageList.indexOf("No alerts found for this device") == -1) {
                            try {
                                _alert_details = new AlertDetails(imageList);
                                _Ralert_details.postValue((short) 1);
                            } catch (JSONException e) {
                                Log.d(TAG, "getBellAlertDetails JSON Exception ," + e.getMessage());
                                e.printStackTrace();
                                _Ralert_details.postValue((short) 0);
                            }
                        } else {
                            try {
                                _alert_details = new AlertDetails("[[\"https://udp1.ibeyonde.com/img/no_signal.jpg\", \"22\\/09\\/2021 - 14:10:41\"]]");
                                _Ralert_details.postValue((short) 1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                _Ralert_details.postValue((short) 0);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "getBellAlertDetails Request failed ," + error.getMessage());
                _Ralert_details.postValue((short) 0);
            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s", LoginViewModel._username, LoginViewModel._pass);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }

        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(120000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

}