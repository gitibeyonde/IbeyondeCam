package com.ibeyonde.cam.ui.device.lastalerts;

import com.ibeyonde.cam.utils.Camera;
import com.ibeyonde.cam.utils.History;
import com.ibeyonde.cam.utils.LastAlerts;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class DeviceMotionContent {
    private static final String TAG= DeviceMotionContent.class.getCanonicalName();

    public static List<PlaceHolder> _item_list;

    public static void initialize(Hashtable<String, Camera> ch){
        _item_list = new ArrayList<PlaceHolder>();

        if (ch == null) return;
        Enumeration<String> e = ch.keys();
        while (e.hasMoreElements()) {
            String uuid = e.nextElement();
            Camera c = ch.get(uuid);
            LastAlerts h = c._lastalerts;
            if (h != null) {
                PlaceHolder hi = new PlaceHolder(c._index, uuid, h);
                _item_list.add(hi);
            }
        }
    }

    public static class PlaceHolder {
        public final String _id;
        public final String _uuid;
        public final LastAlerts _lastalerts;

        public PlaceHolder(int id, String uuid, LastAlerts lastalerts) {
            this._id = Integer.toString(id);
            this._uuid = uuid;
            this._lastalerts = lastalerts;
        }

        @Override
        public String toString() {
            return _uuid;
        }
    }
}