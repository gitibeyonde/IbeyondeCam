package com.ibeyonde.cam.ui.notifications;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationContent {
    private static final String TAG= NotificationContent.class.getCanonicalName();

    public static List<NotificationContent.PlaceHolder> _alert_list;

    public static void initialize(ArrayList<JSONObject> alr){
        _alert_list = new ArrayList<>();
        if (alr == null) return;
        Iterator<JSONObject> e = alr.iterator();
        while (e.hasNext()) {
            JSONObject h = e.next();
            NotificationContent.PlaceHolder hi = null;
            try {
                Log.d(TAG, h.toString());
                hi = new NotificationContent.PlaceHolder(h.getInt("id"), h.getString("uuid"), h.getString("uuid"), h.getString("created"), h.getString("image"));
                _alert_list.add(hi);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }


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
 */
    public static class PlaceHolder {
        public final int id;
        public final String uuid;
        public final String name;
        public final String timestamp;
        public final String url;

        public PlaceHolder(int id, String uuid, String name, String timestamp, String url) {
            this.id = id;
            this.uuid = uuid;
            this.name = name;
            this.timestamp = timestamp;
            this.url = url;
        }

        @Override
        public String toString() {
            return timestamp;
        }
    }
}