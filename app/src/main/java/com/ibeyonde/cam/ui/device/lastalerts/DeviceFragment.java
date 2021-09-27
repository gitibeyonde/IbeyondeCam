package com.ibeyonde.cam.ui.device.lastalerts;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.ibeyonde.cam.R;
import com.ibeyonde.cam.databinding.FragmentDeviceListBinding;
import com.ibeyonde.cam.utils.Camera;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A fragment representing a list of Items.
 */
public class DeviceFragment extends Fragment {
    private static final String TAG= DeviceFragment.class.getCanonicalName();
    private DeviceViewModel deviceViewModel;

    public DeviceFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_list, container, false);

        deviceViewModel =
                new ViewModelProvider(this).get(DeviceViewModel.class);
        FragmentDeviceListBinding binding = FragmentDeviceListBinding.inflate(inflater, container, false);

        deviceViewModel.deviceList(getActivity().getApplicationContext());

        deviceViewModel._deviceList.observe(this.getActivity(), new Observer<Hashtable<String, Camera>>() {
            public void onChanged(@Nullable Hashtable<String, Camera> c) {
                Log.i(TAG, "Device List = " + c.size());
                Enumeration<String> e = c.keys();
                while (e.hasMoreElements()) {
                    String uuid = e.nextElement();
                    Activity activity = getActivity();
                    if (isAdded() && activity != null) {
                        deviceViewModel.getHistory(activity.getApplicationContext(), uuid, null);
                    }
                }
            }
        });


        deviceViewModel._update.observe(this.getActivity(), new Observer<Short>() {
            public void onChanged(@Nullable Short s) {
                Hashtable<String, Camera> ch = deviceViewModel._deviceList.getValue();
                if (ch != null) {
                    DeviceMotionContent.initialize(ch);
                    if (deviceViewModel.isAllCameraWithHistory()){
                        RecyclerView recyclerView = (RecyclerView) view;
                        recyclerView.setAdapter(new DeviceRecyclerViewAdapter(DeviceMotionContent._item_list));
                    }
                }
            }
        });


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}