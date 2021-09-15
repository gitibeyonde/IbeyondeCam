package com.ibeyonde.cam.ui.device.live;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

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
    private static final ArrayBlockingQueue<Bitmap> queue = new ArrayBlockingQueue<Bitmap>(100);

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

        SurfaceView viewer = binding.cameraLive;
        viewer.setZOrderOnTop(true);
        SurfaceHolder surface = viewer.getHolder();
        liveViewModel._url.observe(this.getActivity(), new Observer<String>() {
            public void onChanged(@Nullable String url) {
                Log.d(TAG, "Live URL = " + url);
                try {
                    MjpegRunner rc = new MjpegRunner(new URL(url), queue);
                    Thread t = new Thread(rc);
                    t.start();

                    new Thread() {
                        public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bmp = null;
                                try {
                                    Log.d(TAG, "Start Setting image view");
                                    bmp = queue.poll(5, TimeUnit.SECONDS);
                                    int i=0;
                                    while(bmp != null) {
                                        Log.d(TAG, "Set image to view " + i++);
                                        Canvas c = surface.lockCanvas();
                                        c.setBitmap(bmp);
                                        surface.unlockCanvasAndPost(c);
                                        bmp = queue.poll(5, TimeUnit.SECONDS);
                                    }
                                    Log.d(TAG, "Exiting while loop");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        }
                    }.start();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        Log.d(TAG, "Live view created");
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}