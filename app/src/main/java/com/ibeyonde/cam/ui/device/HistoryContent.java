package com.ibeyonde.cam.ui.device;

import com.ibeyonde.cam.utils.History;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryContent {
    private static final String TAG= HistoryContent.class.getCanonicalName();

    public static final List<HistoryItem> _history = new ArrayList<HistoryItem>();

    public static final Map<String, HistoryItem> _history_map = new HashMap<String, HistoryItem>();

    private static void addItem(HistoryItem item) {
        _history.add(item);
        _history_map.put(item.id, item);
    }

    public static HistoryItem createPlaceholderItem(int position, String uuid, History h) {
        return new HistoryItem(String.valueOf(position), uuid, h);
    }

    public static class HistoryItem {
        public final String id;
        public final String content;
        public final History _history;

        public HistoryItem(String id, String content, History history) {
            this.id = id;
            this.content = content;
            this._history = history;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}