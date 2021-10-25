package com.ibeyonde.cam.ui.notifications;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ibeyonde.cam.databinding.FragmentNotificationItemBinding;
import com.ibeyonde.cam.utils.ImageLoadTask;

import java.util.List;

public class NotificationRecyclerViewAdapter  extends RecyclerView.Adapter<NotificationRecyclerViewAdapter.ViewHolder> {
    private static final String TAG= NotificationRecyclerViewAdapter.class.getCanonicalName();

    private final List<NotificationContent.PlaceHolder> _alerts;

    public NotificationRecyclerViewAdapter(List<NotificationContent.PlaceHolder> items) {
        _alerts = items;
    }
    @Override
    public NotificationRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NotificationRecyclerViewAdapter.ViewHolder(FragmentNotificationItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final NotificationRecyclerViewAdapter.ViewHolder holder, int position) {
        NotificationContent.PlaceHolder hi  = _alerts.get(position);
        Log.d(TAG, hi.toString());
        holder.timestamp.setText(hi.timestamp);
        Log.d(TAG, hi.id + " id");
        holder.id.setText(hi.name + "-" + hi.id);
        holder.picture.setContentDescription(hi.uuid + "%%" + hi.timestamp);
        new ImageLoadTask(hi.url, holder.picture).execute();
    }

    @Override
    public int getItemCount() {
        return _alerts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView id;
        public final TextView timestamp;
        public final ImageView picture;

        public ViewHolder(FragmentNotificationItemBinding binding) {
            super(binding.getRoot());
            timestamp = binding.notificationTimestamp;
            picture = binding.notificationImage;
            id = binding.notificationId;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + timestamp.getText() + "'";
        }
    }
}
