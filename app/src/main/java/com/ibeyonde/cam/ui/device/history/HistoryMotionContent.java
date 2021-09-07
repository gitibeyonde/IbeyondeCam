package com.ibeyonde.cam.ui.device.history;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HistoryMotionContent {
    private static final String TAG= HistoryMotionContent.class.getCanonicalName();

    public static List<HistoryMotionContent.PlaceHolder> _item_list;

    public static void initialize(ArrayList<JSONObject> hl){
        _item_list = new ArrayList<HistoryMotionContent.PlaceHolder>();

        if (hl == null) return;
        Iterator<JSONObject> e = hl.iterator();
        while (e.hasNext()) {
            JSONObject h = e.next();
            HistoryMotionContent.PlaceHolder hi = null;
            try {
                hi = new HistoryMotionContent.PlaceHolder(h.getString("datetime"), h.getString("url"));
                _item_list.add(hi);
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
     * A placeholder item representing a piece of content.
     */
    public static class PlaceHolder {
        public final String timestamp;
        public final String url;

        public PlaceHolder(String timestamp, String url) {
            this.timestamp = timestamp;
            this.url = url;
        }

        @Override
        public String toString() {
            return timestamp;
        }
    }
}