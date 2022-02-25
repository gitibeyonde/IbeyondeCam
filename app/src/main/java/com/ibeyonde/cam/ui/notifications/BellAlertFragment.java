package com.ibeyonde.cam.ui.notifications;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.ImageButton;
import android.widget.Toast;

import com.ibeyonde.cam.databinding.FragmentBellAlertBinding;
import com.ibeyonde.cam.ui.device.lastalerts.DeviceViewModel;
import com.ibeyonde.cam.ui.device.live.LiveViewModel;
import com.ibeyonde.cam.ui.device.live.DirectLive;
import com.ibeyonde.cam.ui.device.live.MjpegCloud;
import com.ibeyonde.cam.ui.login.LoginViewModel;
import com.ibeyonde.cam.utils.AlertDetails;
import com.ibeyonde.cam.utils.Camera;
import com.ibeyonde.cam.utils.ImageLoadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class BellAlertFragment extends Fragment {
    private static final String TAG= BellAlertFragment.class.getCanonicalName();

    public static volatile String _cameraId;
    public static volatile String _dateTime;
    private LoginViewModel loginViewModel;
    private DeviceViewModel deviceViewModel;
    private NotificationViewModel notificationViewModel;
    private LiveViewModel liveViewModel;
    private FragmentBellAlertBinding binding;

    private Handler handler;
    private MjpegCloud mjpegCloud;
    static DirectLive directLive;
    boolean _isDirect = false;
    boolean _isLocal = false;
    boolean _isCloud = false;
    Timer _hist_time;
    Timer _live_check;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
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
                loginViewModel._username = cv[0];
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
                _hist_time.cancel();
            }
        };
        ImageButton navButtons[] = { binding.histNav0, binding.histNav1, binding.histNav2, binding.histNav3, binding.histNav4, binding.histNav5,
                binding.histNav6, binding.histNav7, binding.histNav8, binding.histNav9};

        notificationViewModel._Ralert_details.observe(this.getActivity(), new Observer<Short>() {
            public void onChanged(@Nullable Short s) {
                if (s == 1) {
                    binding.progressBar.setVisibility(View.GONE);
                    AlertDetails ad = notificationViewModel._alert_details;
                    if (ad == null) return;

                    new ImageLoadTask(ad.getCurrentURL(), binding.historyImage).execute();

                    ArrayList<JSONObject> adlist = ad._alert_details;
                    for (int i = 0; i < adlist.size() && i < 10; i++) {
                        try {
                            new ImageLoadTask(adlist.get(i).getString("url"), navButtons[i]).execute();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(TAG, "Bell alert url loading " + ad.getCurrentURL());

                    _hist_time = new Timer();
                    TimerTask imgRefresh = new TimerTask() {
                        @Override
                        public void run() {
                            new ImageLoadTask(ad.getCurrentURL(), binding.historyImage).execute();
                        }
                    };
                    _hist_time.scheduleAtFixedRate(imgRefresh, 0, 1000);


                    for (int i = 0; i < 10; i++) {
                        navButtons[i].setOnClickListener(navClickListener);
                    }
                }
                else {
                    Toast toast = Toast.makeText(getContext(), "Alert Failed to load, retry !", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 20, 500);
                    toast.show();
                }
            }
        });

        _live_check = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                if (_isLocal == true){
                    directLive.stop();
                    Log.d(TAG, "Timer Local running, stopping direct");
                    if (_live_check != null) _live_check.cancel();
                }
                else if (_isDirect == true && _isCloud == false){
                    mjpegCloud.stop();
                    Log.d(TAG, "Timer Direct running, stopping runner");
                    if (_live_check != null) _live_check.cancel();
                }
                else if (_isDirect == true && directLive._isRunningWell == true){
                    mjpegCloud.stop();
                    Log.d(TAG, "Timer Direct running, stopping runner");
                    if (_live_check != null) _live_check.cancel();
                }
                else {
                    Log.d(TAG, "Timer cloud running");
                }
                setStreamIndicator();
            }
        };
        _live_check.scheduleAtFixedRate(tt, 2000, 5000);

        Log.i(TAG, "on create view ");
        return root;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        liveViewModel = new ViewModelProvider(this).get(LiveViewModel.class);
        Log.d(TAG, "onCreate");

        liveViewModel._url_updated.observe(this.getActivity(), new Observer<Integer>() {
            public void onChanged(@Nullable Integer stream_url) {
                if (stream_url == 1){ // local url available
                    directLive.stop();
                    _isCloud = false;
                    _isDirect = false;
                    _isLocal = true;
                    setStreamIndicator();
                }
                else if (stream_url == 2){ // cloud url
                    _isCloud = true;
                    _isDirect = true;
                    _isLocal = false;
                    setStreamIndicator();
                }
                else {
                    return;
                }
                String url = liveViewModel._url;
                Log.i(TAG, stream_url + " Live URL = " + url);
                try {
                    mjpegCloud = new MjpegCloud(handler, binding.cameraLive, new URL(url));
                    Thread t = new Thread(mjpegCloud);
                    t.start();
                } catch (Exception e) {
                    if (mjpegCloud != null) mjpegCloud.stop();
                    Log.e(TAG, "Live URL streaming failed");
                }

                if (getActivity() != null) {
                    Camera c = DeviceViewModel.getCamera(_cameraId);
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(c._name + "@" + _dateTime);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!_isLocal && !_isCloud) {
            try {
                directLive = new DirectLive(_cameraId, handler, getResources(), binding.cameraLive);
                Thread t = new Thread(directLive);
                t.start();
            } catch (Exception e) {
                if (directLive != null) directLive.stop();
                Log.e(TAG, "UDP streaming failed");
            }
        }
        Log.i(TAG, "on start ");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "on resume ");
        if (mjpegCloud != null) mjpegCloud.resume();
        if (directLive != null) directLive.resume();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "on pause ");
        if (mjpegCloud != null) mjpegCloud.pause();
        if (directLive != null) directLive.pause();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "on onDestroyView ");
        if (mjpegCloud != null) mjpegCloud.stop();
        if (directLive != null) directLive.stop();
        if (_live_check != null) _live_check.cancel();
        super.onDestroyView();
    }

    private void setStreamIndicator(){
        if (binding == null)
            return;
        binding.streamCloud.setTextColor(Color.TRANSPARENT);
        binding.streamLocal.setTextColor(Color.TRANSPARENT);
        binding.streamDirect.setTextColor(Color.TRANSPARENT);
        if (_isLocal){
            binding.streamLocal.setTextColor(Color.GREEN);
        }
        else if (_isDirect){
            binding.streamDirect.setTextColor(Color.GREEN);
        }
        else {
            binding.streamCloud.setTextColor(Color.GREEN);
        }
    }


}