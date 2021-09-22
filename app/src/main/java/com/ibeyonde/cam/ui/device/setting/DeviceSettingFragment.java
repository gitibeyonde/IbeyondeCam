package com.ibeyonde.cam.ui.device.setting;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.icu.util.TimeZone;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.ibeyonde.cam.databinding.FragmentDeviceSettingBinding;

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
        //mViewModel.getCam(getContext(), _cameraId);

        Spinner dropdown = binding.timeZone;
        String[] items = TimeZone.getAvailableIDs();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        binding.settingText.setVisibility(View.GONE);
        binding.settingProgressBar.setVisibility(View.VISIBLE);
        binding.cloudConnect.setAlpha(.5f);
        binding.cloudConnect.setEnabled(false);
        binding.motionHistory.setAlpha(.5f);
        binding.motionHistory.setEnabled(false);
        binding.camName.setAlpha(.5f);
        binding.camName.setEnabled(false);
        binding.timeZone.setAlpha(.5f);
        binding.timeZone.setEnabled(false);

        mViewModel._device_online.observe(this.getActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean h) {
                Log.i(TAG, h.toString() + " Device Online = " + _cameraId);
                handler = new Handler(getContext().getMainLooper());
                if (h){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String cc = mViewModel._dev_nv.get("cloud");
                            String ht = mViewModel._dev_nv.get("history");
                            if ("true".equals(cc)) {
                                binding.cloudConnect.setChecked(true);
                            }
                            if ("true".equals(ht)) {
                                binding.motionHistory.setChecked(true);
                            }
                            selectSpinnerItemByValue(binding.timeZone, mViewModel._dev_nv.get("tz"));
                            binding.settingProgressBar.setVisibility(View.GONE);
                            binding.settingText.setVisibility(View.GONE);
                            binding.cloudConnect.setAlpha(1f);
                            binding.cloudConnect.setEnabled(true);
                            binding.motionHistory.setAlpha(1f);
                            binding.motionHistory.setEnabled(true);
                            binding.camName.setAlpha(1f);
                            binding.camName.setEnabled(true);
                            binding.timeZone.setAlpha(1f);
                            binding.timeZone.setEnabled(true);
                            binding.camName.setText(mViewModel._dev_nv.get("cn"));
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
                mViewModel.cloudConnect(getContext(), _cameraId, "cloud", binding.cloudConnect.isEnabled() ? "true" : "false");
            }
        });

        Button motionHistory = binding.motionHistory;
        motionHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.cloudConnect(getContext(), _cameraId, "hist", binding.cloudConnect.isEnabled() ? "true" : "false");
            }
        });

        ImageButton nameSet = binding.camButton;
        nameSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, binding.camName.getText().toString());
            }
        });

        Spinner spr = binding.timeZone;
        spr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, spr.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

    public static void selectSpinnerItemByValue(Spinner spnr, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spnr.getAdapter();
        for (int position = 0; position < adapter.getCount(); position++) {
            if(adapter.getItem(position).equals(value)) {
                spnr.setSelection(position);
                return;
            }
        }
    }
}