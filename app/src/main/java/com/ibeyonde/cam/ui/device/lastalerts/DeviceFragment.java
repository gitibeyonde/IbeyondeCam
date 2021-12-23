package com.ibeyonde.cam.ui.device.lastalerts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.ibeyonde.cam.R;
import com.ibeyonde.cam.databinding.FragmentDeviceListBinding;
import com.ibeyonde.cam.ui.login.SplashActivity;
import com.ibeyonde.cam.utils.Camera;

import java.util.Hashtable;

/**
 * A fragment representing a list of Items.
 */
public class DeviceFragment extends Fragment {
    private static final String TAG= DeviceFragment.class.getCanonicalName();
    private DeviceViewModel deviceViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_list, container, false);
        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);

        FragmentDeviceListBinding binding = FragmentDeviceListBinding.inflate(inflater, container, false);

        deviceViewModel.getAllHistory(getActivity().getApplicationContext());


        deviceViewModel._history.observe(this.getActivity(), new Observer<Short>() {
            public void onChanged(@Nullable Short s) {
                Log.i(TAG, "history = " + s);
                if (s == 1) {
                    Hashtable<String, Camera> ch = deviceViewModel._deviceList;
                    if (ch != null && !ch.isEmpty()) {
                        DeviceMotionContent.initialize(ch);
                        if (deviceViewModel.isAllCameraWithHistory()) {
                            RecyclerView recyclerView = (RecyclerView) view;
                            recyclerView.setAdapter(new DeviceRecyclerViewAdapter(DeviceMotionContent._item_list));
                        }
                    }
                    else {
                        Log.i(TAG, "No device");
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("No device attached to this account. Please, configure the device that you want to view using Bluetooth!")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do things
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
                else {
                    Log.i(TAG, "Device List request failed retry !");
                    if (getContext() != null) {
                        Toast toast = Toast.makeText(getContext(), "Device List request failed, retry !", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 20, 500);
                        toast.show();
                    }
                    Intent i = new Intent(getContext(), SplashActivity.class);
                    getActivity().finish();
                    startActivity(i);
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