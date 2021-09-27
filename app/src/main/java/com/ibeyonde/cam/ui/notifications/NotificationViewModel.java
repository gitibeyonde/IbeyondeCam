package com.ibeyonde.cam.ui.notifications;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.lifecycle.LiveData;
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
import com.ibeyonde.cam.utils.Alerts;
import com.ibeyonde.cam.utils.History;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class NotificationViewModel extends ViewModel {
    private static final String TAG= NotificationViewModel.class.getCanonicalName();

    public static final MutableLiveData<Alerts> _alerts = new MutableLiveData<>();

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
                            _alerts.postValue(new Alerts(jsonArray));
                        } catch (JSONException e) {
                            Log.e(TAG, "getBellAlerts JSON format error");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "getBellAlerts Request failed ," + error.getMessage());
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