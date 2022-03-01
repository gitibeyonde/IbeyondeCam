package com.ibeyonde.cam.ui.device.setting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.ibeyonde.cam.MainActivity;
import com.ibeyonde.cam.databinding.FragmentDeviceSettingBinding;
import com.ibeyonde.cam.ui.device.lastalerts.DeviceViewModel;
import com.ibeyonde.cam.ui.device.live.LiveViewModel;
import com.ibeyonde.cam.ui.login.LoginViewModel;
import com.ibeyonde.cam.ui.login.SplashActivity;
import com.ibeyonde.cam.utils.Camera;

public class DeviceSettingFragment extends Fragment {
    private static final String TAG= DeviceSettingFragment.class.getCanonicalName();

    FragmentDeviceSettingBinding binding;
    private DeviceSettingViewModel deviceSettingViewModel;
    public static String _cameraId;

    public static DeviceSettingFragment newInstance() {
        return new DeviceSettingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        deviceSettingViewModel = new ViewModelProvider(this).get(DeviceSettingViewModel.class);
        binding = FragmentDeviceSettingBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        Log.i(TAG, "CREATING DeviceSettingFragment");

        //check user logged in
        if (LoginViewModel._username == null || LoginViewModel._pass == null){
            Toast.makeText(getContext(), "Logging In.", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getContext(), SplashActivity.class);
            getActivity().finish();
            startActivity(i);
        }

        deviceSettingViewModel.getVeil(getContext(), _cameraId);

        Spinner frameSize = binding.frameSize;

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{ "Large", "Medium", "Small"});
        frameSize.setAdapter(adapter);

        Spinner dropdown = binding.timeZone;
        String[] items = TimeOffset._time_offset.keySet().toArray(new String[0]);

        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        binding.settingText.setVisibility(View.GONE);
        binding.settingProgressBar.setVisibility(View.VISIBLE);
        binding.motionHistory.setAlpha(.5f);
        binding.motionHistory.setEnabled(false);
        binding.camName.setAlpha(.5f);
        binding.camName.setEnabled(false);
        binding.timeZone.setAlpha(.5f);
        binding.timeZone.setEnabled(false);
        binding.frameSize.setAlpha(.5f);
        binding.frameSize.setEnabled(false);

        binding.vFlip.setAlpha(.5f);
        binding.vFlip.setEnabled(false);
        binding.hFlip.setAlpha(.5f);
        binding.hFlip.setEnabled(false);

        deviceSettingViewModel._device_online.observe(this.getActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean h) {
                Log.i(TAG, h.toString() + " Device Online = " + _cameraId);
                Camera c = DeviceViewModel.getCamera(_cameraId);
                if(!h)return;
                selectSpinnerItemByValue(binding.timeZone, deviceSettingViewModel._cam_nv.get("timezone"));
                String cc = deviceSettingViewModel._cam_nv.get("cloud");
                String ht = deviceSettingViewModel._cam_nv.get("history");
                if ("true".equals(ht)) {
                    binding.motionHistory.setChecked(true);
                }
                binding.settingProgressBar.setVisibility(View.GONE);
                binding.settingText.setVisibility(View.GONE);
                binding.motionHistory.setAlpha(1f);
                binding.motionHistory.setEnabled(true);
                binding.camName.setAlpha(1f);
                binding.camName.setEnabled(true);
                binding.version.setText(deviceSettingViewModel._cam_nv.get("version"));
                binding.timeZone.setAlpha(1f);
                binding.timeZone.setEnabled(true);
                binding.camName.setText(deviceSettingViewModel._cam_nv.get("name"));

                selectSpinnerItemByValue(binding.frameSize, getFrameSize(Integer.parseInt(deviceSettingViewModel._cam_nv.get("framesize"))));
                binding.frameSize.setAlpha(1f);
                binding.frameSize.setEnabled(true);
                binding.vFlip.setChecked(deviceSettingViewModel._cam_nv.get("vflip").equals("0") ? false : true);
                binding.vFlip.setAlpha(1f);
                binding.vFlip.setEnabled(true);
                binding.vFlip.setChecked(deviceSettingViewModel._cam_nv.get("hmirror").equals("0") ? false : true);
                binding.hFlip.setAlpha(1f);
                binding.hFlip.setEnabled(true);


                Button motionHistory = binding.motionHistory;
                motionHistory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Confirmation")
                                .setMessage("This will erase all the motion history, do you want to go ahead ?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        deviceSettingViewModel.applyDeviceConfig(getContext(), _cameraId, "history", binding.motionHistory.isChecked() ? "true" : "false");
                                        Toast.makeText(getActivity(), "Requested server to remove all history.", Toast.LENGTH_LONG).show();
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                });
                motionHistory.setOnTouchListener(new View.OnTouchListener() {

                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN: {
                                v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                                v.invalidate();
                                break;
                            }
                            case MotionEvent.ACTION_UP: {
                                v.getBackground().clearColorFilter();
                                v.invalidate();
                                break;
                            }
                        }
                        return false;
                    }
                });

                ImageButton nameSet = binding.camNameButton;
                nameSet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, binding.camName.getText().toString());
                        deviceSettingViewModel.applyDeviceConfig(getContext(), _cameraId, "name", binding.camName.getText().toString());
                    }
                });

                Spinner sprTz = binding.timeZone;
                sprTz.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (sprTz.getSelectedItem().toString().equals(deviceSettingViewModel._cam_nv.get("timezone")))return;
                        new AlertDialog.Builder(getContext())
                                .setTitle("Confirmation")
                                .setMessage("Please confirm timezone change ?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        deviceSettingViewModel.applyDeviceConfig(getContext(), _cameraId, "timezone", sprTz.getSelectedItem().toString());
                                        Toast.makeText(getActivity(), "Device will be offline for a minute.", Toast.LENGTH_LONG).show();
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });


                Button vflip = binding.vFlip;
                vflip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deviceSettingViewModel.applyCamConfig(getContext(), _cameraId, "vflip", binding.vFlip.isEnabled() ? "1" : "0");
                    }
                });

                Button hflip = binding.hFlip;
                hflip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deviceSettingViewModel.applyCamConfig(getContext(), _cameraId, "hmirror", binding.hFlip.isEnabled() ? "1" : "0");
                    }
                });

                Spinner sprFs = binding.frameSize;
                sprFs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if ( getFrameSize(sprFs.getSelectedItem().toString()) == Integer.parseInt(deviceSettingViewModel._cam_nv.get("framesize")))return;
                        new AlertDialog.Builder(getContext())
                                .setTitle("Confirmation")
                                .setMessage("Please confirm framesize change ?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        deviceSettingViewModel.applyCamConfig(getContext(), _cameraId, "framesize", getFrameSize(sprFs.getSelectedItem().toString()) +"");
                                        Toast.makeText(getActivity(), "Device will be offline for a minute.", Toast.LENGTH_LONG).show();
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

                int curver = Integer.parseInt(deviceSettingViewModel._cam_nv.get("version"));
                ImageButton upgrade = binding.upgradeButton;
                if (curver < deviceSettingViewModel._latest_version) {
                    upgrade.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            new AlertDialog.Builder(getContext())
                                    .setTitle("Confirmation")
                                    .setMessage("Confirm that you wish to upgrade device ?")
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            deviceSettingViewModel.command(getContext(), "upgrade", _cameraId);
                                            binding.upgradeAvailable.setText("Upgrading, please wait for a minute");
                                            upgrade.setVisibility(View.INVISIBLE);
                                            Toast.makeText(getActivity(), "Device will be offline for a minute.", Toast.LENGTH_LONG).show();
                                        }})
                                    .setNegativeButton(android.R.string.no, null).show();
                        }
                    });
                    upgrade.setVisibility(View.VISIBLE);
                    binding.upgradeAvailable.setVisibility(View.VISIBLE);
                    binding.upgradeAvailable.setText("Upgrade to ver-" + deviceSettingViewModel._latest_version);
                }
                else {
                    upgrade.setVisibility(View.INVISIBLE);
                    binding.upgradeAvailable.setVisibility(View.INVISIBLE);
                }
                if (getActivity() != null)
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(c._name  + " Settings ");
            }
        });


        ImageButton deleteHistory = binding.deleteHistory;
        deleteHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceSettingViewModel.deleteHistory(getContext(), _cameraId);
            }
        });


        ImageButton restart = binding.restartButton;
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Confirmation")
                        .setMessage("Are you sure you want to restart device ?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                deviceSettingViewModel.command(getContext(), "restart", _cameraId);
                                Toast.makeText(getActivity(), "Device will be offline for a minute.", Toast.LENGTH_LONG).show();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        ImageButton reset = binding.resetButton;
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Confirmation")
                        .setMessage("Are you sure you want to reset device ?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                deviceSettingViewModel.command(getContext(), "reset", _cameraId);
                                Toast.makeText(getActivity(), "Device will go to bluetooth mode. All configuration will be lost.", Toast.LENGTH_LONG).show();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
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