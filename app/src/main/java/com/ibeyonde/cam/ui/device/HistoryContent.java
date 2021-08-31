package com.ibeyonde.cam.ui.device;

import com.ibeyonde.cam.utils.Camera;
import com.ibeyonde.cam.utils.History;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class HistoryContent {
    private static final String TAG= HistoryContent.class.getCanonicalName();

    public static final List<HistoryItem> _item_list = new ArrayList<HistoryItem>();

    public static final Map<String, HistoryItem> _item_map = new HashMap<String, HistoryItem>();

    public static void initialize(Hashtable<String, Camera> ch){
        if (ch == null) return;
        Enumeration<String> e = ch.keys();
        while (e.hasMoreElements()) {
            String uuid = e.nextElement();
            Camera c = ch.get(uuid);
            History h = c._history;
            if (h != null) {
                HistoryItem hi = new HistoryItem(c._index, uuid, h);
                _item_list.add(hi);
                _item_map.put(uuid, hi);
            }
        }
    }

    public static class HistoryItem {
        public final String _id;
        public final String _uuid;
        public final History _history;

        public HistoryItem(int id, String uuid, History history) {
            this._id = Integer.toString(id);
            this._uuid = uuid;
            this._history = history;
        }

        @Override
        public String toString() {
            return _uuid;
        }
    }
}