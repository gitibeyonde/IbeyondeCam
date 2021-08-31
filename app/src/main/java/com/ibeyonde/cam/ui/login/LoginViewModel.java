package com.ibeyonde.cam.ui.login;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.AuthFailureError;
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
    public static final MutableLiveData<String> _token = new MutableLiveData<>();
    public static String _email;
    public static String _pass;

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

    public void login(Context ctx, String email, String password){
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
                                LoginViewModel._token.setValue("SUCCESS");
                            }
                            else {
                                Log.d(TAG, "Login failed 1");
                                LoginViewModel._token.setValue("FAILED");
                            }
                        } catch (JSONException e) {
                            Log.d(TAG, "Login failed 2 " + e.getMessage());
                            LoginViewModel._token.setValue("FAILED");
                            throw new RuntimeException("Login failed");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Login failed 3," + error.getMessage());
                LoginViewModel._token.setValue("FAILED");
            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s",email,password);
                _email = email;
                _pass = password;
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }

        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


}
