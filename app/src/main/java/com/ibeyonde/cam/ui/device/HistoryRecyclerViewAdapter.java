package com.ibeyonde.cam.ui.device;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibeyonde.cam.databinding.FragmentHistoryItemBinding;
import com.ibeyonde.cam.ui.device.placeholder.PlaceholderContent.PlaceholderItem;
import com.ibeyonde.cam.utils.History;
import com.ibeyonde.cam.utils.ImageLoadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder> {

    private final List<JSONObject> _history_list;

    public HistoryRecyclerViewAdapter(List<JSONObject> items) {
        _history_list = items;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentHistoryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        JSONObject hi  = _history_list.get(position);
        try {
            holder.timestamp.setText(hi.getString("datetime"));
            new ImageLoadTask(hi.getString("url"), holder.picture).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return _history_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView timestamp;
        public final ImageView picture;

        public ViewHolder(FragmentHistoryItemBinding binding) {
            super(binding.getRoot());
            timestamp = binding.historyTimestamp;
            picture = binding.historyImage;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + timestamp.getText() + "'";
        }
    }
}