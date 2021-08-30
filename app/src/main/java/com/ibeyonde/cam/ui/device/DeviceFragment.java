package com.ibeyonde.cam.ui.device;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ibeyonde.cam.R;
import com.ibeyonde.cam.databinding.FragmentDeviceBinding;
import com.ibeyonde.cam.utils.Camera;
import com.ibeyonde.cam.utils.History;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A fragment representing a list of Items.
 */
public class DeviceFragment extends Fragment {
    private static final String TAG= DeviceFragment.class.getCanonicalName();

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mDeviceCount = 1;

    private DeviceViewModel deviceViewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DeviceFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static DeviceFragment newInstance(int columnCount) {
        DeviceFragment fragment = new DeviceFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (getArguments() != null) {
            mDeviceCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_list, container, false);

        deviceViewModel =
                new ViewModelProvider(this).get(DeviceViewModel.class);
        com.ibeyonde.cam.databinding.FragmentDeviceBinding binding = FragmentDeviceBinding.inflate(inflater, container, false);


        deviceViewModel.deviceList(getActivity().getApplicationContext());

        Log.d(TAG, "onCreateView");

        deviceViewModel._deviceList.observe(this.getActivity(), new Observer<Hashtable<String, Camera>>() {
            public void onChanged(@Nullable Hashtable<String, Camera> c) {
                Log.i(TAG, "Device List = " + c.size());
                Enumeration<String> e = c.keys();
                while (e.hasMoreElements()) {
                    String uuid = e.nextElement();
                    Activity activity = getActivity();
                    if (isAdded() && activity != null) {
                        deviceViewModel.getHistory(activity.getApplicationContext(), uuid);
                    }
                }
            }
        });

        deviceViewModel._update.observe(this.getActivity(), new Observer<Short>() {
            public void onChanged(@Nullable Short s) {
                Hashtable<String, Camera> ch = deviceViewModel._deviceList.getValue();
                if (ch == null) return;
                Enumeration<String> e = ch.keys();
                while (e.hasMoreElements()) {
                    String uuid = e.nextElement();
                    Camera c = ch.get(uuid);
                    History h = c._history;
                    if (h != null) {
                        /**try {

                            historyViews[c._index].setContentDescription(uuid);
                            new ImageLoadTask(h.getCurrentURL(), historyViews[c._index]).execute();
                        } catch (JSONException jsonException) {
                            Log.w(TAG, "Image load failed " + jsonException.getMessage());
                        }**/
                    }
                }
            }
        });
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mDeviceCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mDeviceCount));
            }
            recyclerView.setAdapter(new DeviceRecyclerViewAdapter(PlaceholderContent.ITEMS));
        }

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}