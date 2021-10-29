package com.ibeyonde.cam.ui.login;

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
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginViewModel extends ViewModel {
    private static final String TAG= LoginViewModel.class.getCanonicalName();
    public static final MutableLiveData<String> _login_token = new MutableLiveData<>();
    public static final MutableLiveData<String> _register_token = new MutableLiveData<>();
    public static final MutableLiveData<String> _reset_token = new MutableLiveData<>();
    public static String _email;
    public static String _pass;

    public static final String SUCCESS="SUCCESS";
    public static final String FAILURE="FAILURE";

    // A placeholder username validation check
    public boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
            //}
            //if (username.contains("@")) {
            //  return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    public boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    public void login(Context ctx, String username, String password){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url ="https://ping.ibeyonde.com/api/iot.php?view=login";

        // Request a string response from the provided URL.
        JsonObjectRequest stringRequest = new JsonObjectRequest(JsonObjectRequest.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Login response " + response.toString());
                            if (response.getString("message").equals("Success")){
                                LoginViewModel._login_token.postValue(SUCCESS);
                            }
                            else {
                                Log.d(TAG, "Login failed 1");
                                LoginViewModel._login_token.postValue(FAILURE);
                            }
                        } catch (JSONException e) {
                            Log.d(TAG, "Login failed 2 " + e.getMessage());
                            LoginViewModel._login_token.postValue(FAILURE);
                            throw new RuntimeException("Login failed");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Login failed 3," + error.getMessage());
                LoginViewModel._login_token.postValue("FAILED");
            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s",username,password);
                _email = username;
                _pass = password;
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }

        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    public void reset(Context ctx, String username){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url ="https://ping.ibeyonde.com/api/iot.php?view=reset";

        // Request a string response from the provided URL.
        JsonObjectRequest stringRequest = new JsonObjectRequest(JsonObjectRequest.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "RESET response " + response.toString());
                            if (response.getString("message").equals("Success")){
                                LoginViewModel._reset_token.postValue(SUCCESS);
                            }
                            else {
                                Log.d(TAG, "RESET failed 1");
                                LoginViewModel._reset_token.postValue(FAILURE);
                            }
                        } catch (JSONException e) {
                            Log.d(TAG, "RESET failed 2 " + e.getMessage());
                            LoginViewModel._reset_token.postValue(FAILURE);
                            throw new RuntimeException("Login failed");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "RESET failed 3," + error.getMessage());
                LoginViewModel._reset_token.postValue("FAILED");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }



    public void register(Context ctx, String username, String password, String email, String phone){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url ="https://ping.ibeyonde.com/api/iot.php?view=register";

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("user_name", username);
            jsonRequest.put("user_name", password);
            jsonRequest.put("user_email", email);
            jsonRequest.put("user_phone", phone);
            jsonRequest.put("user_password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Request a string response from the provided URL.
        JsonObjectRequest stringRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST, url, jsonRequest,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Register response " + response.toString());
                            if (response.getString("message").equals("Success")){
                                LoginViewModel._register_token.postValue(SUCCESS);
                            }
                            else {
                                Log.d(TAG, "Register failed 1");
                                LoginViewModel._register_token.postValue(FAILURE);
                            }
                        } catch (JSONException e) {
                            Log.d(TAG, "Register failed 2 " + e.getMessage());
                            LoginViewModel._register_token.postValue(FAILURE);
                            throw new RuntimeException("Register failed");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Register failed 3," + error.getMessage());
                LoginViewModel._register_token.postValue("FAILED");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
