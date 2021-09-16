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

    public static String _cameraId;

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
        liveViewModel.getLocalLiveUrl(getContext(), _cameraId);

        liveViewModel._url.observe(this.getActivity(), new Observer<String>() {
            public void onChanged(@Nullable String url) {
                Log.d(TAG, "Live URL = " + url);
                if (url.toString().length() > 10) {
                    try {
                        handler = new Handler(getContext().getMainLooper());
                        rc = new MjpegRunner(new URL(url), handler, binding);
                        Thread t = new Thread(rc);
                        t.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    if (rc != null)rc.stop();
                }
            }
        });
        Log.d(TAG, "Live view created");

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        liveViewModel._url.setValue("");
        if (rc != null)rc.stop();
    }

}