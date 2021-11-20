package com.ibeyonde.cam.ui.device.setting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.ibeyonde.cam.databinding.FragmentDeviceSettingBinding;
import com.ibeyonde.cam.ui.device.lastalerts.DeviceViewModel;
import com.ibeyonde.cam.utils.Camera;

public class DeviceSettingFragment extends Fragment {
    private static final String TAG= DeviceSettingFragment.class.getCanonicalName();

    FragmentDeviceSettingBinding binding;
    private DeviceSettingViewModel mViewModel;
    public static String _cameraId;
    public static String _veil;
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

        mViewModel.getVeil(getContext(), _cameraId);
        mViewModel.getLatestVersion(getContext());

        Spinner frameSize = binding.frameSize;

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{ "Large", "Medium", "Small"});
        frameSize.setAdapter(adapter);

        Spinner dropdown = binding.timeZone;
        String[] items = TimeOffset._time_offset.keySet().toArray(new String[0]);

        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
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

        binding.vFlip.setAlpha(.5f);
        binding.vFlip.setEnabled(false);
        binding.hFlip.setAlpha(.5f);
        binding.hFlip.setEnabled(false);

        mViewModel._veil.observe(this.getActivity(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        if (s.length() == 0) {
                            Toast toast = Toast.makeText(getContext(), "Failed to load, retry !", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 20, 500);
                            toast.show();
                        }
                        else {
                            _veil = s;
                            mViewModel.getConfig(getContext(), _cameraId);
                        }
                    }
                });

        mViewModel._device_online.observe(this.getActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean h) {
                Log.i(TAG, h.toString() + " Device Online = " + _cameraId);
                Camera c = DeviceViewModel.getCamera(_cameraId);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(c._name  + " Settings ");
                if(!h)return;
                selectSpinnerItemByValue(binding.timeZone, mViewModel._cam_nv.get("timezone"));
                String cc = mViewModel._cam_nv.get("cloud");
                String ht = mViewModel._cam_nv.get("history");
                if ("true".equals(cc)) {
                    binding.cloudConnect.setChecked(true);
                }
                if ("true".equals(ht)) {
                    binding.motionHistory.setChecked(true);
                }
                binding.settingProgressBar.setVisibility(View.GONE);
                binding.settingText.setVisibility(View.GONE);
                binding.cloudConnect.setAlpha(1f);
                binding.cloudConnect.setEnabled(true);
                binding.motionHistory.setAlpha(1f);
                binding.motionHistory.setEnabled(true);
                binding.camName.setAlpha(1f);
                binding.camName.setEnabled(true);
                binding.version.setText(mViewModel._cam_nv.get("version"));
                binding.timeZone.setAlpha(1f);
                binding.timeZone.setEnabled(true);
                binding.camName.setText(mViewModel._cam_nv.get("name"));

                selectSpinnerItemByValue(binding.frameSize, getFrameSize(Integer.parseInt(mViewModel._cam_nv.get("framesize"))));
                binding.frameSize.setAlpha(1f);
                binding.frameSize.setEnabled(true);
                binding.vFlip.setChecked(mViewModel._cam_nv.get("vflip").equals("0") ? false : true);
                binding.vFlip.setAlpha(1f);
                binding.vFlip.setEnabled(true);
                binding.vFlip.setChecked(mViewModel._cam_nv.get("hmirror").equals("0") ? false : true);
                binding.hFlip.setAlpha(1f);
                binding.hFlip.setEnabled(true);

                Button cloudConnect = binding.cloudConnect;
                cloudConnect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewModel.applyDeviceConfig(getContext(), _cameraId, "cloud", binding.cloudConnect.isEnabled() ? "true" : "false");
                    }
                });

                Button motionHistory = binding.motionHistory;
                motionHistory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewModel.applyDeviceConfig(getContext(), _cameraId, "history", binding.cloudConnect.isEnabled() ? "true" : "false");
                    }
                });

                ImageButton nameSet = binding.camNameButton;
                nameSet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, binding.camName.getText().toString());
                        mViewModel.applyDeviceConfig(getContext(), _cameraId, "cn", binding.camName.getText().toString());
                    }
                });

                Spinner sprTz = binding.timeZone;
                sprTz.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(TAG, sprTz.getSelectedItem().toString());
                        mViewModel.applyDeviceConfig(getContext(), _cameraId, "timezone", sprTz.getSelectedItem().toString());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });


                Button vflip = binding.vFlip;
                vflip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewModel.applyCamConfig(getContext(), _cameraId, "vflip", binding.cloudConnect.isEnabled() ? "1" : "0");
                    }
                });

                Button hflip = binding.hFlip;
                hflip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewModel.applyCamConfig(getContext(), _cameraId, "hmirror", binding.cloudConnect.isEnabled() ? "1" : "0");
                    }
                });

                Spinner sprFs = binding.frameSize;
                sprFs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(TAG, sprFs.getSelectedItem().toString());
                        mViewModel.applyCamConfig(getContext(), _cameraId, "framesize", getFrameSize(sprFs.getSelectedItem().toString()) +"");
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

                int current_version = Integer.parseInt(mViewModel._cam_nv.get("version"));
                ImageButton upgrade = binding.upgradeButton;
                if (current_version < mViewModel._latest_version) {
                    upgrade.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mViewModel.command(getContext(), "upgrade", _cameraId);
                        }
                    });
                    upgrade.setVisibility(View.VISIBLE);
                    binding.upgradeAvailable.setVisibility(View.VISIBLE);
                }
                else {
                    upgrade.setVisibility(View.INVISIBLE);
                    binding.upgradeAvailable.setVisibility(View.INVISIBLE);
                }
            }
        });


        ImageButton deleteHistory = binding.deleteHistory;
        deleteHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.deleteHistory(getContext(), _cameraId);
            }
        });


        ImageButton reset = binding.resetButton;
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.command(getContext(), "reset", _cameraId);
            }
        });

        ImageButton restart = binding.restartButton;
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.command(getContext(), "restart", _cameraId);
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

    public String getFrameSize(int fs){
        if (fs == 8)
            return "Large";
        else if(fs == 5)
            return "Medium";
        else
            return "Small";
    }

    public int getFrameSize(String fs){
        if (fs.equals("Large"))
            return 8;
        else if(fs.equals("Medium"))
            return 5;
        else
            return 2;
    }
}