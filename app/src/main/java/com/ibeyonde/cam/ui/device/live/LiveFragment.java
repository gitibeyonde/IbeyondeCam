package com.ibeyonde.cam.ui.device.live;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.ibeyonde.cam.databinding.FragmentLiveBinding;
import com.ibeyonde.cam.ui.device.lastalerts.DeviceViewModel;
import com.ibeyonde.cam.utils.Camera;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class LiveFragment extends Fragment {
    private static final String TAG= LiveFragment.class.getCanonicalName();

    public static volatile String _cameraId;

    private LiveViewModel liveViewModel;
    private FragmentLiveBinding binding;

    Handler handler;
    DirectLive directLive;
    MjpegCloud mjpegCloud;
    boolean _isDirect = false;
    boolean _isLocal = false;
    boolean _isCloud = false;
    Timer t;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLiveBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Log.i(TAG, "Live view created");

        t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                if (_isLocal == true){
                    directLive.stop();
                    Log.d(TAG, "Timer Local running, stopping direct");
                }
                else if (_isDirect == true && _isCloud == false){
                    mjpegCloud.stop();
                    Log.d(TAG, "Timer Direct running, stopping runner");
                }
                else if (_isDirect == true && directLive._isRunningWell == true){
                    mjpegCloud.stop();
                    Log.d(TAG, "Timer Direct running, stopping runner");
                }
                else {
                    Log.d(TAG, "Timer cloud running");
                }
                setStreamIndicator();
            }
        };
        t.scheduleAtFixedRate(tt, 2000, 5000);

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        liveViewModel = new ViewModelProvider(this).get(LiveViewModel.class);
        liveViewModel.getLiveUrl(getContext(), _cameraId);

        handler = new Handler(getContext().getMainLooper());

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
                Camera c = DeviceViewModel.getCamera(_cameraId);
                binding.cameraLabel.setText(c._name);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(c._name + " Live ");
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            directLive = new DirectLive(_cameraId, handler, getResources(), binding.cameraLive);
            Thread t = new Thread(directLive);
            t.start();
        } catch (Exception e) {
            if (directLive != null) directLive.stop();
            Log.e(TAG, "UDP streaming failed");
        }
        Camera c = DeviceViewModel.getCamera(_cameraId);
        binding.cameraLabel.setText(c._name);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(c._name + " Live ");
        Log.i(TAG, "on start ");
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "on resume ");
        if (mjpegCloud != null) mjpegCloud.resume();
        if (directLive != null) directLive.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "on pause ");
        if (mjpegCloud != null) mjpegCloud.pause();
        if (directLive != null) directLive.pause();
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "on onDestroyView ");
        if (mjpegCloud != null) mjpegCloud.stop();
        if (directLive != null) directLive.stop();
        if (t != null) t.cancel();
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