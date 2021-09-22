package com.ibeyonde.cam.ui.setting;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ibeyonde.cam.R;
import com.ibeyonde.cam.databinding.FragmentDeviceSettingBinding;
import com.ibeyonde.cam.databinding.FragmentSettingBinding;
import com.ibeyonde.cam.ui.device.setting.DeviceSettingFragment;
import com.ibeyonde.cam.ui.login.LoginActivity;
import com.ibeyonde.cam.ui.login.SplashActivity;

import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class SettingFragment extends Fragment {
    private static final String TAG= SettingFragment.class.getCanonicalName();

    private SettingViewModel mViewModel;
    private FragmentSettingBinding binding;
    Handler handler;

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        File file = new File(getActivity().getApplicationContext().getFilesDir(), ".cred");
        Log.i(TAG, "Getting creds in " + file.getAbsoluteFile());

        ImageButton logout = binding.logoutButton;

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler = new Handler(getContext().getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Logging out.", Toast.LENGTH_SHORT).show();
                    }
                });
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.exit(0);
                    }}, 4000);
                try (FileWriter fo = new FileWriter(file)) {
                    fo.write("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i(TAG, " Removing creds " + file.getAbsoluteFile());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        return root;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}