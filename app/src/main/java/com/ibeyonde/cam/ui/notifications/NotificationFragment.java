package com.ibeyonde.cam.ui.notifications;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ibeyonde.cam.R;
import com.ibeyonde.cam.databinding.FragmentNotificationListBinding;
import com.ibeyonde.cam.utils.Alerts;

public class NotificationFragment extends Fragment {
    private static final String TAG= NotificationFragment.class.getCanonicalName();


    private NotificationViewModel notificationViewModel;
    private FragmentNotificationListBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_list, container, false);
        notificationViewModel =
                new ViewModelProvider(this).get(NotificationViewModel.class);

        binding = FragmentNotificationListBinding.inflate(inflater, container, false);

        notificationViewModel.getBellAlerts(getContext());


        notificationViewModel._Ralerts.observe(this.getActivity(), new Observer<Short>() {
            @Override
            public void onChanged(Short s) {
                if (s == 1) {
                    Alerts al = notificationViewModel._alerts;
                    if (al == null) return;
                    Log.i(TAG, "Alerts List = " + al._total);
                    NotificationContent.initialize(al.getAlerts());
                    // Set the adapter
                    if (view instanceof RecyclerView) {
                        Context context = view.getContext();
                        RecyclerView recyclerView = (RecyclerView) view;
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));
                        recyclerView.setAdapter(new NotificationRecyclerViewAdapter(NotificationContent._alert_list));
                    }
                }
                else {
                    Toast toast = Toast.makeText(getContext(), "Failed to get notifications, retry !", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 20, 500);
                    toast.show();
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}