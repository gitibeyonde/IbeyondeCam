package com.ibeyonde.cam.ui.notifications;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ibeyonde.cam.MainActivity;
import com.ibeyonde.cam.bt.SerialService;
import com.ibeyonde.cam.databinding.FragmentBellAlertBinding;
import com.ibeyonde.cam.ui.device.lastalerts.DeviceViewModel;
import com.ibeyonde.cam.ui.device.live.LiveViewModel;
import com.ibeyonde.cam.ui.device.live.MjpegRunner;
import com.ibeyonde.cam.ui.login.LoginActivity;
import com.ibeyonde.cam.ui.login.LoginViewModel;
import com.ibeyonde.cam.utils.AlertDetails;
import com.ibeyonde.cam.utils.Camera;
import com.ibeyonde.cam.utils.ImageLoadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

public class BellAlertFragment extends Fragment {
    private static final String TAG= BellAlertFragment.class.getCanonicalName();

    public static String _cameraId;
    public static String _dateTime;
    private LoginViewModel loginViewModel;
    private DeviceViewModel deviceViewModel;
    private NotificationViewModel notificationViewModel;
    private LiveViewModel liveViewModel;
    private FragmentBellAlertBinding binding;

    private Handler handler;
    private MjpegRunner rc;

    public static BellAlertFragment newInstance() {
        return new BellAlertFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        liveViewModel = new ViewModelProvider(this).get(LiveViewModel.class);
        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
        binding = FragmentBellAlertBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        handler = new Handler(getContext().getMainLooper());

        //LOGIN
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        File file = new File(getActivity().getApplicationContext().getFilesDir(), ".cred");
        Log.i(TAG, "Getting creds in " + file.getAbsoluteFile());

        // read cred from file
        try (BufferedReader fo = new BufferedReader(new FileReader(file))) {
            String cred = fo.readLine();
            if (cred != null && cred.contains("%%")) {
                String[] cv = cred.split("%%");
                binding.progressBar.setVisibility(View.VISIBLE);
                loginViewModel._email = cv[0];
                loginViewModel._pass = cv[1];
            }
            else {
                Log.i(TAG, "Failed reading cred file, no data ");
                Toast.makeText(getActivity().getApplicationContext(), "User not logged in, live boot failed", Toast.LENGTH_SHORT).show();
            }
        }
        catch(Exception e){
            e.printStackTrace();
            Log.i(TAG, "Failed reading cred file " + e.getMessage());
        }
        notificationViewModel.getBellAlertDetails(getContext(), _cameraId, _dateTime);
        //GET DEVICE LIST
        liveViewModel.getLiveUrl(getContext(), _cameraId);

        View.OnClickListener navClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton tb = getActivity().findViewById(v.getId());
                Bitmap bitmap = ((BitmapDrawable)tb.getDrawable()).getBitmap();
                binding.historyImage.setImageBitmap(bitmap);
            }
        };
        ImageButton navButtons[] = { binding.histNav0, binding.histNav1, binding.histNav2, binding.histNav3, binding.histNav4, binding.histNav5,
                binding.histNav6, binding.histNav7, binding.histNav8, binding.histNav9};

        notificationViewModel._alert_details.observe(this.getActivity(), new Observer<AlertDetails>() {
            public void onChanged(@Nullable AlertDetails ad) {
                if (ad == null) return;
                ArrayList<JSONObject> adlist = ad._alert_details;
                for (int i=0;i< adlist.size() && i < 10; i++){
                    try {
                        new ImageLoadTask(adlist.get(i).getString("url"), navButtons[i]).execute();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "Bell alert url loading " + ad.getCurrentURL());

                new ImageLoadTask(ad.getCurrentURL(), binding.historyImage).execute();

                TimerTask imgRefresh = new TimerTask()
                {
                    @Override
                    public void run() {
                        new ImageLoadTask(ad.getCurrentURL(), binding.historyImage).execute();
                    }
                };
                Timer t = new Timer();
                t.scheduleAtFixedRate(imgRefresh, 0, 1000);

                for (int i=0;i< 10; i++){
                    navButtons[i].setOnClickListener(navClickListener);
                }
            }
        });


        liveViewModel._url.observe(this.getActivity(), new Observer<String>() {
            public void onChanged(@Nullable String url) {
                Log.i(TAG, "Live URL = " + url);
                if (url.toString().length() > 10) {
                    try {
                        if (rc != null)rc.stop();
                        rc = new MjpegRunner(new URL(url), handler, binding.cameraLive);
                        Thread t = new Thread(rc);
                        t.start();
                    } catch (Exception e) {
                        Log.i(TAG, "Error in starting live = ", e);
                        if (rc != null)rc.stop();
                    }
                    binding.progressBar.setVisibility(View.GONE);
                }
                else {
                    if (rc != null)rc.stop();
                }
            }
        });

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(_cameraId  + " Bell Alert ");

        Log.i(TAG, "on create view ");
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        notificationViewModel._alert_details.postValue(null);
        if (rc != null)rc.stop();
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "on start ");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "on resume ");
    }

    @Override
    public void onStop() {
        Log.i(TAG, "on stop ");
        if (rc != null)rc.stop();
        super.onStop();
    }


    @Override
    public void onDetach() {
        Log.i(TAG, "on detach ");
        if (rc != null)rc.stop();
        super.onDetach();
    }


}