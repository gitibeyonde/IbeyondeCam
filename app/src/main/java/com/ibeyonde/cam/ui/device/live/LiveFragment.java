package com.ibeyonde.cam.ui.device.live;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.ibeyonde.cam.databinding.FragmentLiveBinding;
import com.ibeyonde.cam.ui.device.lastalerts.DeviceViewModel;
import com.ibeyonde.cam.utils.Camera;

import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class LiveFragment extends Fragment {
    private static final String TAG= LiveFragment.class.getCanonicalName();

    public static volatile String _cameraId;

    private LiveViewModel liveViewModel;
    private FragmentLiveBinding binding;

    Handler handler;
    static MjpegLive dlive;
    static MjpegRunner runner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        liveViewModel = new ViewModelProvider(this).get(LiveViewModel.class);

        handler = new Handler(getContext().getMainLooper());

        liveViewModel._url_updated.observe(this.getActivity(), new Observer<Boolean>() {
            public void onChanged(@Nullable Boolean url_updated) {
                if (url_updated && !dlive._isRunningWell){
                    binding.streamDirect.setTextColor(Color.TRANSPARENT);
                    binding.streamLocal.setTextColor(Color.TRANSPARENT);
                    binding.streamCloud.setTextColor(Color.TRANSPARENT);
                    dlive.stop();
                    String url = liveViewModel._url;
                    if (url.length() > 30){
                        binding.streamCloud.setTextColor(Color.GREEN);
                    }
                    else {
                        binding.streamLocal.setTextColor(Color.GREEN);
                    }
                    Log.i(TAG, url_updated + " Live URL = " + url);
                    try {
                        runner = new MjpegRunner(handler, binding.cameraLive, new URL(url));
                        Thread t = new Thread(runner);
                        t.start();
                    } catch (Exception e) {
                        if (runner != null) runner.stop();
                        Log.e(TAG, "Live URL streaming failed");
                    }
                    Camera c = DeviceViewModel.getCamera(_cameraId);
                    binding.cameraLabel.setText(c._name);
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(c._name + " Live ");
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLiveBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        liveViewModel.getLiveUrl(getContext(), _cameraId);
        Log.i(TAG, "Live view created");
        return root;
    }
    @Override
    public void onStart() {
        super.onStart();
        try {
            binding.streamCloud.setTextColor(Color.TRANSPARENT);
            binding.streamLocal.setTextColor(Color.TRANSPARENT);
            binding.streamDirect.setTextColor(Color.GREEN);
            dlive = new MjpegLive(_cameraId, handler, getResources(), binding.cameraLive);
            Thread t = new Thread(dlive);
            t.start();
        } catch (Exception e) {
            if (dlive != null) dlive.stop();
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
        if (runner != null)runner.resume();
        if (dlive != null)dlive.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "on pause ");
        if (runner != null)runner.pause();
        if (dlive != null)dlive.pause();
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "on onDestroyView ");
        if (runner != null)runner.stop();
        if (dlive != null)dlive.stop();
        super.onDestroyView();
    }

}