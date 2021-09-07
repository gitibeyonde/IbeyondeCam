package com.ibeyonde.cam.ui.device.history;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibeyonde.cam.databinding.FragmentHistoryItemBinding;
import com.ibeyonde.cam.ui.device.history.HistoryMotionContent.PlaceHolder;
import com.ibeyonde.cam.utils.ImageLoadTask;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceHolder}.
 * TODO: Replace the implementation with code for your data type.
 */
public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder> {
    private static final String TAG= HistoryRecyclerViewAdapter.class.getCanonicalName();

    private final List<HistoryMotionContent.PlaceHolder> _history_list;

    public HistoryRecyclerViewAdapter(List<HistoryMotionContent.PlaceHolder> items) {
        _history_list = items;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentHistoryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        PlaceHolder hi  = _history_list.get(position);
        Log.d(TAG, hi.toString());
        holder.timestamp.setText(hi.timestamp);
        new ImageLoadTask(hi.url, holder.picture).execute();
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