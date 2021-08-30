package com.ibeyonde.cam.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class History {
    private static final String TAG= History.class.getCanonicalName();
    public ArrayList<JSONArray> _history = new ArrayList<>();
    public int _total;
    public int _current;

    //[["https:\/\/s3-us-west-2.amazonaws.com\/data.ibeyonde\/781d0d93\/2021\/08\/25\/13_46_04\/4JKL2G.jpg?X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Security-Token=IQoJb3JpZ2luX2VjEAoaCmFwLXNvdXRoLTEiRzBFAiEAk5ZmFP7V%2Bo6JgDcl3y96pz0DicSzgi4fXEHmZUTNaOACIHqAkQvBDBOW20uF%2Bn%2FNViDv2HD460Pr%2BvwbCbyb4%2B6MKvwDCEMQABoMNTc0NDUxNDQxMjg4Igy172uQ%2FIlTzWpXvDoq2QO5WiSqVLgYTtTJ90JTDbqRIPWINrujDzNBkwaAvbGinvdYpC7jddd1d1iX9YvZh8YkEMbiYahW6U5pIVyiUjTcYCwZd3pjyAdWTvmocOcHDvOvYH2aJhWRZ2o783Cc7OtvjsV9SjdbMkVKI2pIwqyNjPMIEsp0olJUR3ChW97VsyZH54jEFaY9sPFKUPXKbn4VRbPgiCv7%2BjH0PWCtZw4IR%2BIzInNJfugDm7O77Beari8B7Nv3iiO39U2x%2BZQnwmy5V32uzLwUdROLiYrjVgsVa9wU%2FIVNiAcK1F9VXTV8icjHKiqABdnrkPAaTMfCbHb%2FFkJuGyrMfv7uP60dDz8u0OXAdwYUh4MqwGHiwVEklnVbggPa%2Bn%2FMXz161Cj9fA8pWvEsuGS37de4zWGm8E3Sh2AMgYUV5a7k2Y%2Fkp2KLOl7MxFgxy07MgCfOkDYMFz9fOiiLdT4lQRekKRs1Dt1TRcVx%2FlqXpzN8%2BFTNboMU7vrMqaF%2FrbFBrjBqftQ%2BgBett7n9ir4mjiry%2FnYiNspwe3dXq%2BpyCK5zwDfrrS5amSNuvgzC0JFcnuWLc2iSatUIFqd9Ti2Shdtx2ZoFHirEOZ%2BgzSss0dnlAxf3F5Fqm1Qpxj%2BgatbMkTCiopiJBjqlAQW%2FCOOaWEqRdC2U8WunArILrkOrLmLsj8f%2FgLJD539g5blogUlgfGJHnriI2zXtGPTNIz8t2edropGZvO13Abf72BSEn3jwmn9V9edWbdUQUwPT%2BFjBFf%2BWWA4jSsGyDNMioRqH0PRQSZmY9nNmx8RTyD9zNnfyHZVaYi2KnXjzo2csfKh%2FU4%2F04O1IaMDECiBs5N3nd6NuSTF1Jw04R3TFnSMiLw%3D%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ASIAYLP7ZL2EAE3A77KN%2F20210825%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20210825T095905Z&X-Amz-SignedHeaders=host&X-Amz-Expires=14400&X-Amz-Signature=3ed01cf71a3f196eed289912d13d96dbffdf45c491d3373bc328faa329632501","25\/08\/2021 - 13:46:04"],
    // ["https:\/\/s3-us-west-2.amazonaws.com\/data.ibeyonde\/781d0d93\/2021\/08\/25\/13_46_57\/xA69bz.jpg?X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Security-Token=IQoJb3JpZ2luX2VjEAoaCmFwLXNvdXRoLTEiRzBFAiEAk5ZmFP7V%2Bo6JgDcl3y96pz0DicSzgi4fXEHmZUTNaOACIHqAkQvBDBOW20uF%2Bn%2FNViDv2HD460Pr%2BvwbCbyb4%2B6MKvwDCEMQABoMNTc0NDUxNDQxMjg4Igy172uQ%2FIlTzWpXvDoq2QO5WiSqVLgYTtTJ90JTDbqRIPWINrujDzNBkwaAvbGinvdYpC7jddd1d1iX9YvZh8YkEMbiYahW6U5pIVyiUjTcYCwZd3pjyAdWTvmocOcHDvOvYH2aJhWRZ2o783Cc7OtvjsV9SjdbMkVKI2pIwqyNjPMIEsp0olJUR3ChW97VsyZH54jEFaY9sPFKUPXKbn4VRbPgiCv7%2BjH0PWCtZw4IR%2BIzInNJfugDm7O77Beari8B7Nv3iiO39U2x%2BZQnwmy5V32uzLwUdROLiYrjVgsVa9wU%2FIVNiAcK1F9VXTV8icjHKiqABdnrkPAaTMfCbHb%2FFkJuGyrMfv7uP60dDz8u0OXAdwYUh4MqwGHiwVEklnVbggPa%2Bn%2FMXz161Cj9fA8pWvEsuGS37de4zWGm8E3Sh2AMgYUV5a7k2Y%2Fkp2KLOl7MxFgxy07MgCfOkDYMFz9fOiiLdT4lQRekKRs1Dt1TRcVx%2FlqXpzN8%2BFTNboMU7vrMqaF%2FrbFBrjBqftQ%2BgBett7n9ir4mjiry%2FnYiNspwe3dXq%2BpyCK5zwDfrrS5amSNuvgzC0JFcnuWLc2iSatUIFqd9Ti2Shdtx2ZoFHirEOZ%2BgzSss0dnlAxf3F5Fqm1Qpxj%2BgatbMkTCiopiJBjqlAQW%2FCOOaWEqRdC2U8WunArILrkOrLmLsj8f%2FgLJD539g5blogUlgfGJHnriI2zXtGPTNIz8t2edropGZvO13Abf72BSEn3jwmn9V9edWbdUQUwPT%2BFjBFf%2BWWA4jSsGyDNMioRqH0PRQSZmY9nNmx8RTyD9zNnfyHZVaYi2KnXjzo2csfKh%2FU4%2F04O1IaMDECiBs5N3nd6NuSTF1Jw04R3TFnSMiLw%3D%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ASIAYLP7ZL2EAE3A77KN%2F20210825%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20210825T095905Z&X-Amz-SignedHeaders=host&X-Amz-Expires=14400&X-Amz-Signature=cf026c785304779e778ed327fdb50bfbdf2bd329e3d959ce881ae2d879e66db5","25\/08\/2021 - 13:46:57"],
    // ["https:\/\/s3-us-west-2.amazonaws.com\/data.ibeyonde\/781d0d93\/2021\/08\/25\/13_48_05\/3W9ztW.jpg?X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Security-Token=IQoJb3JpZ2luX2VjEAoaCmFwLXNvdXRoLTEiRzBFAiEAk5ZmFP7V%2Bo6JgDcl3y96pz0DicSzgi4fXEHmZUTNaOACIHqAkQvBDBOW20uF%2Bn%2FNViDv2HD460Pr%2BvwbCbyb4%2B6MKvwDCEMQABoMNTc0NDUxNDQxMjg4Igy172uQ%2FIlTzWpXvDoq2QO5WiSqVLgYTtTJ90JTDbqRIPWINrujDzNBkwaAvbGinvdYpC7jddd1d1iX9YvZh8YkEMbiYahW6U5pIVyiUjTcYCwZd3pjyAdWTvmocOcHDvOvYH2aJhWRZ2o783Cc7OtvjsV9SjdbMkVKI2pIwqyNjPMIEsp0olJUR3ChW97VsyZH54jEFaY9sPFKUPXKbn4VRbPgiCv7%2BjH0PWCtZw4IR%2BIzInNJfugDm7O77Beari8B7Nv3iiO39U2x%2BZQnwmy5V32uzLwUdROLiYrjVgsVa9wU%2FIVNiAcK1F9VXTV8icjHKiqABdnrkPAaTMfCbHb%2FFkJuGyrMfv7uP60dDz8u0OXAdwYUh4MqwGHiwVEklnVbggPa%2Bn%2FMXz161Cj9fA8pWvEsuGS37de4zWGm8E3Sh2AMgYUV5a7k2Y%2Fkp2KLOl7MxFgxy07MgCfOkDYMFz9fOiiLdT4lQRekKRs1Dt1TRcVx%2FlqXpzN8%2BFTNboMU7vrMqaF%2FrbFBrj


    public History(String imgList) throws JSONException {
        JSONArray jr = new JSONArray(imgList);
        Log.d(TAG, jr.toString());
        _total = jr.length();
        _current = 0;
        for(int i=0;i< jr.length();i++){
            JSONArray jo = jr.getJSONArray(i);
            Log.d(TAG, jo.getString(0));
            Log.d(TAG, jo.getString(1));
            _history.add(jo);
        }
    }

    public String getCurrentURL() throws JSONException {
        JSONArray curr = _history.get(_current++);
        if (_current == _total)_current = 0;
        return curr.getString(0);
    }
}
