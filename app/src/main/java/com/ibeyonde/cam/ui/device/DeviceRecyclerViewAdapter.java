package com.ibeyonde.cam.ui.device;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibeyonde.cam.databinding.FragmentDeviceBinding;
import com.ibeyonde.cam.ui.device.HistoryContent.HistoryItem;
import com.ibeyonde.cam.utils.ImageLoadTask;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceRecyclerViewAdapter extends RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder> {
    private static final String TAG= DeviceRecyclerViewAdapter.class.getCanonicalName();

    private final List<HistoryItem> _history_list;

    public DeviceRecyclerViewAdapter(List<HistoryItem> items) {
        _history_list = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentDeviceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        HistoryItem hi  = _history_list.get(position);
        Log.d(TAG, "onBindViewHolder  position = " + position + " hv size = " + _history_list.size());
        Log.d(TAG, "onBindViewHolder  id = " + hi._id + " uuid = " + hi._uuid );
        holder.uuid.setText(hi._uuid);
        holder.picture.setContentDescription(hi._uuid);

        TimerTask imgRefresh = new TimerTask()
        {
            @Override
            public void run() {
                new ImageLoadTask(hi._history.getCurrentURL(), holder.picture).execute();
            }
        };
        Timer t = new Timer();
        t.scheduleAtFixedRate(imgRefresh, 0, 1000);
    }

    @Override
    public int getItemCount() {
        return _history_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView uuid;
        public final ImageView picture;

        public ViewHolder(FragmentDeviceBinding binding) {
            super(binding.getRoot());
            uuid = binding.deviceUuid;
            picture = binding.deviceView;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + uuid.getText() + "'";
        }
    }
}