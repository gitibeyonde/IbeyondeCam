package com.ibeyonde.cam.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Camera {
    private static final String TAG= Camera.class.getCanonicalName();
    //{"uuid":"781d0d93","user_name":"aprateek","device_name":"781d0d93","type":"NORMAL","profile":"basic","profile_id":"-1",
    // "box_name":"default","timezone":"Asia\/Calcutta","capabilities":"CAMERA,MOTION,TEMPERATURE","version":"1.2.1",
    // "setting":"{ \"version\":\"1.2.1\",  \"time\":\"2021\/06\/10-11:40:27\", \"timezone\":\"Asia\/Calcutta\",
    // \"uptime\":\" 11:40:31 up 3 min,  0 users,  load average: 1.62, 1.02, 0.43\", \"camname\":\"781d0d93\",
    // \"hostname\":\"781d0d93\", \"httpport\":\"80\", \"sip_reg\":\"false\", \"hourly_snapshot\":\"\", \"zoom\":\"\",
    // \"rotate\":\"180\", \"vertical_flip\":\"\", \"horizontal_flip\":\"\", \"brightness\":\"\", \"grid_detect\":\"\",
    // \"face_detect\":\"\", \"snap_quality\":\"1\", \"motion_quality\":\"1\", \"face_min\":\"\", \"motion_tolerance\":\"5000\",
    // \"capturedelta\":\"\", \"video_mode\": \"\", \"public_key\":\"Not Instrumented\",
    // \"git_commit\":\"375eecdfd05bda687a0ef67ffd85850cc0f30c49: maintain a map of neighbours: TODO get a map on demand\" }",
    //  "email_alerts":"0","deviceip":"192.168.100.24","visibleip":"1.186.105.9","port":"455","created":"2021-06-10 11:51:49",
    //  "updated":"2021-08-25T09:33:50+00:00","token":"emgvrD9rz4","errors":[],"messages":[],"nat":"0","expiry":"1630327579",
    //  "ltoken":"hUoT8099Kr"}
    public static int _total=0;
    public int _index;
    public String _uuid;
    public String _name;
    public String _timezone;
    public String _capabilities;
    public String _localIp;
    public String _visibleIp;
    public String _port;
    public String _token;
    public History _history;

    public Camera(JSONObject json){
        try {
            _index = _total++;
            _uuid = json.getString("uuid");
            _name = json.getString("device_name");
            _timezone = json.getString("timezone");
            _capabilities = json.getString("capabilities");
            _localIp = json.getString("deviceip");
            _visibleIp = json.getString("visibleip");
            _port = json.getString("port");
            _token = json.getString("token");
        } catch (JSONException e) {
            Log.i(TAG, "Open Jobs Exception" + e.getMessage());
        }
    }

    public void setHistory(String imgList) throws JSONException {
        _history = new History(imgList);
    }
}
