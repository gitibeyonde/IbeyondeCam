package com.ibeyonde.cam.ui.device.lastalerts;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibeyonde.cam.databinding.FragmentDeviceItemBinding;
import com.ibeyonde.cam.utils.ImageLoadTask;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceRecyclerViewAdapter extends RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder> {
    private static final String TAG= DeviceRecyclerViewAdapter.class.getCanonicalName();

    private final List<DeviceMotionContent.PlaceHolder> _history_list;

    public DeviceRecyclerViewAdapter(List<DeviceMotionContent.PlaceHolder> items) {
        _history_list = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentDeviceItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        DeviceMotionContent.PlaceHolder hi  = _history_list.get(position);
        Log.d(TAG, "onBindViewHolder  position = " + position + " hv size = " + _history_list.size());
        Log.d(TAG, "onBindViewHolder  id = " + hi._id + " uuid = " + hi._uuid );
        holder.uuid.setText(hi._name);
        holder.picture.setContentDescription(hi._uuid);
        holder.live.setContentDescription(hi._uuid);
        holder.history.setContentDescription(hi._uuid);
        holder.setting.setContentDescription(hi._uuid);

        TimerTask imgRefresh = new TimerTask()
        {
            @Override
            public void run() {
                Log.d(TAG, "onBindViewHolder  url = " + hi._lastalerts.getCurrentURL() );
                new ImageLoadTask(hi._lastalerts.getCurrentURL(), holder.picture).execute();
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
        public final ImageButton live;
        public final ImageButton history;
        public final ImageButton setting;

        public ViewHolder(FragmentDeviceItemBinding binding) {
            super(binding.getRoot());
            uuid = binding.deviceUuid;
            picture = binding.deviceView;
            live = binding.deviceLive;
            history = binding.deviceHistory;
            setting = binding.deviceSetting;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + uuid.getText() + "'";
        }
    }
}