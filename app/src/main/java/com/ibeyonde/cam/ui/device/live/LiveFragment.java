package com.ibeyonde.cam.ui.device.live;

import android.graphics.Bitmap;
import android.graphics.Canvas;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.ibeyonde.cam.databinding.FragmentLiveBinding;

import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class LiveFragment extends Fragment {
    private static final String TAG= LiveFragment.class.getCanonicalName();

    public static volatile String _cameraId;

    private LiveViewModel liveViewModel;
    private FragmentLiveBinding binding;

    Handler handler;
    static MjpegRunner rc;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        liveViewModel =
                new ViewModelProvider(this).get(LiveViewModel.class);
        binding = FragmentLiveBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.cameraLabel.setText(_cameraId);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        liveViewModel._url_updated.observe(this.getActivity(), new Observer<Boolean>() {
            public void onChanged(@Nullable Boolean url_updated) {
                Log.i(TAG, "Live URL = " + liveViewModel._url);
                if (liveViewModel._url.length() > 10) {
                    try {
                        handler = new Handler(getContext().getMainLooper());
                        rc = new MjpegRunner(new URL(liveViewModel._url), handler, binding.cameraLive);
                        Thread t = new Thread(rc);
                        t.start();
                    } catch (Exception e) {
                        Log.e(TAG,"Live URL streaming failed");
                        if (rc != null)rc.stop();
                    }
                }
                else {
                    if (rc != null)rc.stop();
                }
            }
        });
        Log.i(TAG, "Live view created");

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        liveViewModel._url = "";
        if (rc != null)rc.stop();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "on start ");
        liveViewModel.getLiveUrl(getContext(), _cameraId);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "on resume ");
    }

    @Override
    public void onStop() {
        Log.i(TAG, "on stop ");
        liveViewModel._url = "";
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