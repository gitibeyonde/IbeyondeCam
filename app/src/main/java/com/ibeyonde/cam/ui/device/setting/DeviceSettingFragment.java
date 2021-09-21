package com.ibeyonde.cam.ui.device.setting;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.ibeyonde.cam.R;
import com.ibeyonde.cam.databinding.ActivitySplashBinding;
import com.ibeyonde.cam.databinding.FragmentDeviceSettingBinding;
import com.ibeyonde.cam.ui.device.history.HistoryFragment;
import com.ibeyonde.cam.ui.device.history.HistoryMotionContent;
import com.ibeyonde.cam.ui.device.history.HistoryRecyclerViewAdapter;
import com.ibeyonde.cam.ui.device.history.HistoryViewModel;
import com.ibeyonde.cam.utils.History;

public class DeviceSettingFragment extends Fragment {
    private static final String TAG= DeviceSettingFragment.class.getCanonicalName();

    FragmentDeviceSettingBinding binding;
    private DeviceSettingViewModel mViewModel;
    public static String _cameraId;
    Handler handler;

    public static DeviceSettingFragment newInstance() {
        return new DeviceSettingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(DeviceSettingViewModel.class);
        binding = FragmentDeviceSettingBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();

        mViewModel.getConfig(getContext(), _cameraId);

        binding.settingText.setVisibility(View.GONE);
        binding.settingProgressBar.setVisibility(View.VISIBLE);
        binding.cloudConnect.setAlpha(.5f);
        binding.cloudConnect.setEnabled(false);
        binding.motionHistory.setAlpha(.5f);
        binding.motionHistory.setEnabled(false);


        mViewModel._device_online.observe(this.getActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean h) {
                Log.i(TAG, h.toString() + " Device Online = " + _cameraId);
                handler = new Handler(getContext().getMainLooper());
                if (h){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            binding.settingProgressBar.setVisibility(View.GONE);
                            binding.settingText.setVisibility(View.GONE);
                            binding.cloudConnect.setAlpha(1f);
                            binding.cloudConnect.setEnabled(true);
                            binding.motionHistory.setAlpha(1f);
                            binding.motionHistory.setEnabled(true);
                            //history=true&cloud=true
                        }
                    });
                }
                else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            binding.settingProgressBar.setVisibility(View.GONE);
                            binding.settingText.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

        Button cloudConnect = binding.cloudConnect;
        cloudConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.cloudConnect(getContext(), _cameraId, binding.cloudConnect.isEnabled());
            }
        });

        Button motionHistory = binding.motionHistory;
        motionHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.motionHistory(getContext(), _cameraId, binding.cloudConnect.isEnabled());
            }
        });


        ImageButton deleteHistory = binding.deleteHistory;
        deleteHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.deleteHistory(getContext(), _cameraId);
            }
        });

        return root;

    }

}